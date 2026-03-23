package com.pharmacy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pharmacy.common.PageResult;
import com.pharmacy.common.exception.BusinessException;
import com.pharmacy.dto.SaleRecordDTO;
import com.pharmacy.entity.*;
import com.pharmacy.mapper.*;
import com.pharmacy.service.SaleRecordService;
import com.pharmacy.vo.SaleRecordVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SaleRecordServiceImpl extends ServiceImpl<SaleRecordMapper, SaleRecord>
        implements SaleRecordService {

    @Autowired
    private DrugMapper drugMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private InventoryMapper inventoryMapper;

    @Autowired
    private InventoryLogMapper inventoryLogMapper;

    @Override
    public PageResult<SaleRecordVO> listSales(int page, int size, String keyword, Long drugId) {
        Page<SaleRecord> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<SaleRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(drugId != null, SaleRecord::getDrugId, drugId);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like(SaleRecord::getOrderNo, keyword)
                    .or().like(SaleRecord::getCustomerName, keyword));
        }
        wrapper.orderByDesc(SaleRecord::getCreateTime);

        Page<SaleRecord> result = page(pageParam, wrapper);

        Set<Long> drugIds = result.getRecords().stream()
                .map(SaleRecord::getDrugId).filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<Long> operatorIds = result.getRecords().stream()
                .map(SaleRecord::getOperatorId).filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, String> drugNameMap = Map.of();
        if (!drugIds.isEmpty()) {
            drugNameMap = drugMapper.selectBatchIds(drugIds).stream()
                    .collect(Collectors.toMap(Drug::getId, Drug::getGenericName));
        }

        Map<Long, String> userNameMap = Map.of();
        if (!operatorIds.isEmpty()) {
            userNameMap = userMapper.selectBatchIds(operatorIds).stream()
                    .collect(Collectors.toMap(User::getId, User::getRealName));
        }

        Map<Long, String> finalDrugMap = drugNameMap;
        Map<Long, String> finalUserMap = userNameMap;
        List<SaleRecordVO> voList = result.getRecords().stream()
                .map(sale -> toVO(sale, finalDrugMap, finalUserMap))
                .collect(Collectors.toList());

        return PageResult.of(voList, result.getTotal(), page, size);
    }

    @Override
    @Transactional
    public void createSale(SaleRecordDTO dto, Long operatorId) {
        Inventory inventory = inventoryMapper.selectOne(
                new LambdaQueryWrapper<Inventory>()
                        .eq(Inventory::getDrugId, dto.getDrugId())
                        .gt(Inventory::getQuantity, 0)
                        .last("LIMIT 1"));
        if (inventory == null) {
            throw new BusinessException("该药品无可用库存");
        }
        if (inventory.getQuantity() < dto.getQuantity()) {
            throw new BusinessException("库存不足，当前库存：" + inventory.getQuantity());
        }

        int beforeQty = inventory.getQuantity();
        int afterQty = beforeQty - dto.getQuantity();
        inventory.setQuantity(afterQty);
        inventoryMapper.updateById(inventory);

        String orderNo = "SO" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%04d", new Random().nextInt(10000));

        SaleRecord sale = new SaleRecord();
        sale.setOrderNo(orderNo);
        sale.setDrugId(dto.getDrugId());
        sale.setBatchNumber(dto.getBatchNumber());
        sale.setQuantity(dto.getQuantity());
        sale.setUnitPrice(dto.getUnitPrice());
        sale.setAmount(dto.getUnitPrice().multiply(BigDecimal.valueOf(dto.getQuantity())));
        sale.setCustomerName(dto.getCustomerName());
        sale.setOperatorId(operatorId);
        save(sale);

        InventoryLog log = new InventoryLog();
        log.setDrugId(dto.getDrugId());
        log.setInventoryId(inventory.getId());
        log.setType(1);
        log.setQuantity(dto.getQuantity());
        log.setBeforeQuantity(beforeQty);
        log.setAfterQuantity(afterQty);
        log.setReason("销售出库 - 单号：" + orderNo);
        log.setOperatorId(operatorId);
        inventoryLogMapper.insert(log);
    }

    @Override
    public SaleRecordVO getSaleDetail(Long id) {
        SaleRecord sale = getById(id);
        if (sale == null) {
            throw new BusinessException("销售记录不存在");
        }

        Map<Long, String> drugNameMap = new HashMap<>();
        Map<Long, String> userNameMap = new HashMap<>();
        if (sale.getDrugId() != null) {
            Drug drug = drugMapper.selectById(sale.getDrugId());
            if (drug != null) drugNameMap.put(drug.getId(), drug.getGenericName());
        }
        if (sale.getOperatorId() != null) {
            User user = userMapper.selectById(sale.getOperatorId());
            if (user != null) userNameMap.put(user.getId(), user.getRealName());
        }
        return toVO(sale, drugNameMap, userNameMap);
    }

    private SaleRecordVO toVO(SaleRecord sale,
                              Map<Long, String> drugNameMap,
                              Map<Long, String> userNameMap) {
        SaleRecordVO vo = new SaleRecordVO();
        vo.setId(sale.getId());
        vo.setSaleNo(sale.getOrderNo());
        vo.setDrugId(sale.getDrugId());
        vo.setBatchNumber(sale.getBatchNumber());
        vo.setQuantity(sale.getQuantity());
        vo.setUnitPrice(sale.getUnitPrice());
        vo.setTotalPrice(sale.getAmount());
        vo.setOperatorId(sale.getOperatorId());
        vo.setCustomerName(sale.getCustomerName());
        vo.setSaleTime(sale.getCreateTime());
        vo.setCreateTime(sale.getCreateTime());
        vo.setDrugName(drugNameMap.get(sale.getDrugId()));
        vo.setOperatorName(sale.getOperatorId() != null ? userNameMap.get(sale.getOperatorId()) : null);
        return vo;
    }

    @Override
    public void deleteSale(Long id) {
        removeById(id);
    }
}
