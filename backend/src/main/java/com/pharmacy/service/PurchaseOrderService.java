package com.pharmacy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pharmacy.common.PageResult;
import com.pharmacy.dto.PurchaseOrderDTO;
import com.pharmacy.entity.PurchaseOrder;
import com.pharmacy.vo.PurchaseOrderVO;

public interface PurchaseOrderService extends IService<PurchaseOrder> {

    PageResult<PurchaseOrderVO> listOrders(int page, int size, Integer status, String keyword);

    PurchaseOrderVO getOrderDetail(Long id);

    void createOrder(PurchaseOrderDTO dto, Long creatorId);

    void approveOrder(Long id, Long approverId);

    void receiveOrder(Long id, Long operatorId);

    void cancelOrder(Long id);

    void deleteOrder(Long id);
}
