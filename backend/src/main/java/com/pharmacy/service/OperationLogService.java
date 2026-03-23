package com.pharmacy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pharmacy.common.PageResult;
import com.pharmacy.entity.OperationLog;

public interface OperationLogService extends IService<OperationLog> {

    PageResult<OperationLog> listLogs(int page, int size, String module, String username);

    void log(Long userId, String username, String module, String action, String description, String ip);
}
