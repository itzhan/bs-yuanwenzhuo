package com.pharmacy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pharmacy.common.PageResult;
import com.pharmacy.dto.ExpiryAlertHandleDTO;
import com.pharmacy.entity.ExpiryAlert;
import com.pharmacy.vo.ExpiryAlertVO;

public interface ExpiryAlertService extends IService<ExpiryAlert> {

    PageResult<ExpiryAlertVO> listAlerts(int page, int size, Integer alertLevel, Integer status);

    void handleAlert(Long id, ExpiryAlertHandleDTO dto);

    void generateAlerts();

    long countPendingAlerts();

    void deleteAlert(Long id);
}
