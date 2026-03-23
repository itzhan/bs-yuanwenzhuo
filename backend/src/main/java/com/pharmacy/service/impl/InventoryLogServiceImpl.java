package com.pharmacy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pharmacy.common.PageResult;
import com.pharmacy.entity.Drug;
import com.pharmacy.entity.InventoryLog;
import com.pharmacy.entity.User;
import com.pharmacy.mapper.DrugMapper;
import com.pharmacy.mapper.InventoryLogMapper;
import com.pharmacy.mapper.UserMapper;
import com.pharmacy.service.InventoryLogService;
import com.pharmacy.vo.InventoryLogVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class InventoryLogServiceImpl extends ServiceImpl<InventoryLogMapper, InventoryLog>
        implements InventoryLogService {

    @Autowired
    private DrugMapper drugMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public PageResult<InventoryLogVO> listLogs(int page, int size, Long drugId, Integer type) {
        Page<InventoryLog> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<InventoryLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(drugId != null, InventoryLog::getDrugId, drugId);
        wrapper.eq(type != null, InventoryLog::getType, type);
        wrapper.orderByDesc(InventoryLog::getCreateTime);

        Page<InventoryLog> result = page(pageParam, wrapper);

        Set<Long> drugIds = result.getRecords().stream()
                .map(InventoryLog::getDrugId).filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<Long> operatorIds = result.getRecords().stream()
                .map(InventoryLog::getOperatorId).filter(Objects::nonNull)
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
        List<InventoryLogVO> voList = result.getRecords().stream().map(log -> {
            InventoryLogVO vo = new InventoryLogVO();
            vo.setId(log.getId());
            vo.setDrugId(log.getDrugId());
            vo.setType(log.getType() != null ? (log.getType() == 0 ? "入库" : "出库") : null);
            vo.setQuantity(log.getQuantity());
            vo.setBeforeQuantity(log.getBeforeQuantity());
            vo.setAfterQuantity(log.getAfterQuantity());
            vo.setReason(log.getReason());
            vo.setRemark(log.getReason());
            vo.setOperatorId(log.getOperatorId());
            vo.setCreateTime(log.getCreateTime());
            vo.setDrugName(finalDrugMap.get(log.getDrugId()));
            vo.setOperatorName(log.getOperatorId() != null ? finalUserMap.get(log.getOperatorId()) : null);
            return vo;
        }).collect(Collectors.toList());

        return PageResult.of(voList, result.getTotal(), page, size);
    }
}
