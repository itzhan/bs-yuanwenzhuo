package com.pharmacy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pharmacy.common.PageResult;
import com.pharmacy.entity.InventoryLog;
import com.pharmacy.vo.InventoryLogVO;

public interface InventoryLogService extends IService<InventoryLog> {

    PageResult<InventoryLogVO> listLogs(int page, int size, Long drugId, Integer type);
}
