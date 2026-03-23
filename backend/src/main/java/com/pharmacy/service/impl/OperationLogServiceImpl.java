package com.pharmacy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pharmacy.common.PageResult;
import com.pharmacy.entity.OperationLog;
import com.pharmacy.mapper.OperationLogMapper;
import com.pharmacy.service.OperationLogService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLog>
        implements OperationLogService {

    @Override
    public PageResult<OperationLog> listLogs(int page, int size, String module, String username) {
        Page<OperationLog> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasText(module), OperationLog::getModule, module);
        wrapper.like(StringUtils.hasText(username), OperationLog::getUsername, username);
        wrapper.orderByDesc(OperationLog::getCreateTime);

        Page<OperationLog> result = page(pageParam, wrapper);
        return PageResult.of(result.getRecords(), result.getTotal(), page, size);
    }

    @Override
    public void log(Long userId, String username, String module, String action,
                    String description, String ip) {
        OperationLog log = new OperationLog();
        log.setUserId(userId);
        log.setUsername(username);
        log.setModule(module);
        log.setAction(action);
        log.setDescription(description);
        log.setIp(ip);
        save(log);
    }
}
