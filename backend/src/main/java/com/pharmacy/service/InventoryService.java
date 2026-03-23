package com.pharmacy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pharmacy.common.PageResult;
import com.pharmacy.dto.InventoryDTO;
import com.pharmacy.entity.Inventory;
import com.pharmacy.vo.InventoryVO;

import java.util.List;

public interface InventoryService extends IService<Inventory> {

    PageResult<InventoryVO> listInventory(int page, int size, String keyword, Long drugId);

    void addInventory(InventoryDTO dto);

    void updateInventory(Long id, InventoryDTO dto);

    void adjustStock(Long id, int adjustQuantity, String reason, Long operatorId);

    List<InventoryVO> getLowStockList();

    List<InventoryVO> getExpiringList(int days);

    void deleteInventory(Long id);
}
