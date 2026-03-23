package com.pharmacy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pharmacy.common.PageResult;
import com.pharmacy.common.exception.BusinessException;
import com.pharmacy.dto.InventoryDTO;
import com.pharmacy.entity.Drug;
import com.pharmacy.entity.Inventory;
import com.pharmacy.entity.InventoryLog;
import com.pharmacy.mapper.DrugMapper;
import com.pharmacy.mapper.InventoryLogMapper;
import com.pharmacy.mapper.InventoryMapper;
import com.pharmacy.service.InventoryService;
import com.pharmacy.vo.InventoryVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class InventoryServiceImpl extends ServiceImpl<InventoryMapper, Inventory> implements InventoryService {

    @Autowired
    private DrugMapper drugMapper;

    @Autowired
    private InventoryLogMapper inventoryLogMapper;

    @Override
    public PageResult<InventoryVO> listInventory(int page, int size, String keyword, Long drugId) {
        Set<Long> matchDrugIds = null;
        if (StringUtils.hasText(keyword)) {
            LambdaQueryWrapper<Drug> drugWrapper = new LambdaQueryWrapper<>();
            drugWrapper.like(Drug::getGenericName, keyword)
                    .or().like(Drug::getDrugCode, keyword);
            List<Drug> matchedDrugs = drugMapper.selectList(drugWrapper);
            matchDrugIds = matchedDrugs.stream().map(Drug::getId).collect(Collectors.toSet());
            if (matchDrugIds.isEmpty()) {
                return PageResult.of(Collections.emptyList(), 0, page, size);
            }
        }

        Page<Inventory> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Inventory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(drugId != null, Inventory::getDrugId, drugId);
        if (matchDrugIds != null) {
            wrapper.in(Inventory::getDrugId, matchDrugIds);
        }
        wrapper.orderByDesc(Inventory::getCreateTime);

        Page<Inventory> result = page(pageParam, wrapper);
        List<InventoryVO> voList = toVOList(result.getRecords());
        return PageResult.of(voList, result.getTotal(), page, size);
    }

    @Override
    @Transactional
    public void addInventory(InventoryDTO dto) {
        Inventory inventory = new Inventory();
        BeanUtils.copyProperties(dto, inventory);
        save(inventory);

        InventoryLog log = new InventoryLog();
        log.setDrugId(dto.getDrugId());
        log.setInventoryId(inventory.getId());
        log.setType(0);
        log.setQuantity(dto.getQuantity());
        log.setBeforeQuantity(0);
        log.setAfterQuantity(dto.getQuantity());
        log.setReason("新增入库");
        inventoryLogMapper.insert(log);
    }

    @Override
    public void updateInventory(Long id, InventoryDTO dto) {
        Inventory inventory = getById(id);
        if (inventory == null) {
            throw new BusinessException("库存记录不存在");
        }
        BeanUtils.copyProperties(dto, inventory);
        inventory.setId(id);
        updateById(inventory);
    }

    @Override
    @Transactional
    public void adjustStock(Long id, int adjustQuantity, String reason, Long operatorId) {
        Inventory inventory = getById(id);
        if (inventory == null) {
            throw new BusinessException("库存记录不存在");
        }

        int beforeQty = inventory.getQuantity();
        int afterQty = beforeQty + adjustQuantity;
        if (afterQty < 0) {
            throw new BusinessException("库存不足，当前库存：" + beforeQty);
        }

        inventory.setQuantity(afterQty);
        updateById(inventory);

        InventoryLog log = new InventoryLog();
        log.setDrugId(inventory.getDrugId());
        log.setInventoryId(id);
        log.setType(adjustQuantity > 0 ? 0 : 1);
        log.setQuantity(Math.abs(adjustQuantity));
        log.setBeforeQuantity(beforeQty);
        log.setAfterQuantity(afterQty);
        log.setReason(reason);
        log.setOperatorId(operatorId);
        inventoryLogMapper.insert(log);
    }

    @Override
    public List<InventoryVO> getLowStockList() {
        LambdaQueryWrapper<Inventory> wrapper = new LambdaQueryWrapper<>();
        wrapper.apply("quantity <= warning_threshold");
        List<Inventory> list = list(wrapper);
        return toVOList(list);
    }

    @Override
    public List<InventoryVO> getExpiringList(int days) {
        LocalDate deadline = LocalDate.now().plusDays(days);
        LambdaQueryWrapper<Inventory> wrapper = new LambdaQueryWrapper<>();
        wrapper.le(Inventory::getExpiryDate, deadline);
        wrapper.gt(Inventory::getQuantity, 0);
        List<Inventory> list = list(wrapper);
        return toVOList(list);
    }

    private List<InventoryVO> toVOList(List<Inventory> inventories) {
        if (inventories.isEmpty()) {
            return Collections.emptyList();
        }
        Set<Long> drugIds = inventories.stream()
                .map(Inventory::getDrugId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, Drug> drugMap = Map.of();
        if (!drugIds.isEmpty()) {
            List<Drug> drugs = drugMapper.selectBatchIds(drugIds);
            drugMap = drugs.stream().collect(Collectors.toMap(Drug::getId, d -> d));
        }

        Map<Long, Drug> finalDrugMap = drugMap;
        return inventories.stream().map(inv -> {
            InventoryVO vo = new InventoryVO();
            BeanUtils.copyProperties(inv, vo);
            Drug drug = finalDrugMap.get(inv.getDrugId());
            if (drug != null) {
                vo.setDrugName(drug.getGenericName());
                vo.setDrugCode(drug.getDrugCode());
                vo.setSpecification(drug.getSpecification());
                vo.setUnit(drug.getUnit());
            }
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public void deleteInventory(Long id) {
        removeById(id);
    }
}
