package com.pharmacy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pharmacy.common.PageResult;
import com.pharmacy.common.exception.BusinessException;
import com.pharmacy.dto.ExpiryAlertHandleDTO;
import com.pharmacy.entity.Drug;
import com.pharmacy.entity.ExpiryAlert;
import com.pharmacy.entity.Inventory;
import com.pharmacy.mapper.DrugMapper;
import com.pharmacy.mapper.ExpiryAlertMapper;
import com.pharmacy.mapper.InventoryMapper;
import com.pharmacy.service.ExpiryAlertService;
import com.pharmacy.vo.ExpiryAlertVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExpiryAlertServiceImpl extends ServiceImpl<ExpiryAlertMapper, ExpiryAlert>
        implements ExpiryAlertService {

    @Autowired
    private DrugMapper drugMapper;

    @Autowired
    private InventoryMapper inventoryMapper;

    @Override
    public PageResult<ExpiryAlertVO> listAlerts(int page, int size, Integer alertLevel, Integer status) {
        Page<ExpiryAlert> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<ExpiryAlert> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(alertLevel != null, ExpiryAlert::getAlertLevel, alertLevel);
        wrapper.eq(status != null, ExpiryAlert::getStatus, status);
        wrapper.orderByDesc(ExpiryAlert::getCreateTime);

        Page<ExpiryAlert> result = page(pageParam, wrapper);

        Set<Long> drugIds = result.getRecords().stream()
                .map(ExpiryAlert::getDrugId).filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, Drug> drugMap = Map.of();
        if (!drugIds.isEmpty()) {
            drugMap = drugMapper.selectBatchIds(drugIds).stream()
                    .collect(Collectors.toMap(Drug::getId, d -> d));
        }

        Map<Long, Drug> finalDrugMap = drugMap;
        List<ExpiryAlertVO> voList = result.getRecords().stream().map(alert -> {
            ExpiryAlertVO vo = new ExpiryAlertVO();
            BeanUtils.copyProperties(alert, vo);
            Drug drug = finalDrugMap.get(alert.getDrugId());
            if (drug != null) {
                vo.setDrugName(drug.getGenericName());
                vo.setSpecification(drug.getSpecification());
            }
            return vo;
        }).collect(Collectors.toList());

        return PageResult.of(voList, result.getTotal(), page, size);
    }

    @Override
    public void handleAlert(Long id, ExpiryAlertHandleDTO dto) {
        ExpiryAlert alert = getById(id);
        if (alert == null) {
            throw new BusinessException("预警记录不存在");
        }
        alert.setStatus(1);
        alert.setHandlerNote(dto.getHandlerNote());
        alert.setHandleTime(LocalDateTime.now());
        updateById(alert);
    }

    @Override
    public void generateAlerts() {
        LocalDate now = LocalDate.now();
        LocalDate sixMonthsLater = now.plusMonths(6);

        List<Inventory> inventories = inventoryMapper.selectList(
                new LambdaQueryWrapper<Inventory>()
                        .le(Inventory::getExpiryDate, sixMonthsLater)
                        .gt(Inventory::getQuantity, 0));

        for (Inventory inv : inventories) {
            long exists = count(new LambdaQueryWrapper<ExpiryAlert>()
                    .eq(ExpiryAlert::getInventoryId, inv.getId())
                    .eq(ExpiryAlert::getStatus, 0));
            if (exists > 0) continue;

            long daysUntilExpiry = ChronoUnit.DAYS.between(now, inv.getExpiryDate());

            ExpiryAlert alert = new ExpiryAlert();
            alert.setDrugId(inv.getDrugId());
            alert.setInventoryId(inv.getId());
            alert.setBatchNumber(inv.getBatchNumber());
            alert.setExpiryDate(inv.getExpiryDate());
            alert.setStatus(0);

            if (daysUntilExpiry <= 30) {
                alert.setAlertLevel(3);
            } else if (daysUntilExpiry <= 90) {
                alert.setAlertLevel(2);
            } else {
                alert.setAlertLevel(1);
            }
            save(alert);
        }
    }

    @Override
    public long countPendingAlerts() {
        return count(new LambdaQueryWrapper<ExpiryAlert>()
                .eq(ExpiryAlert::getStatus, 0));
    }

    @Override
    public void deleteAlert(Long id) {
        removeById(id);
    }
}
