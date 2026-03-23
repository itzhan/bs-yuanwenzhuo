package com.pharmacy.controller;

import com.pharmacy.common.PageResult;
import com.pharmacy.common.Result;
import com.pharmacy.dto.InventoryDTO;
import com.pharmacy.service.InventoryService;
import com.pharmacy.vo.InventoryVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    public Result<PageResult<InventoryVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long drugId) {
        return Result.success(inventoryService.listInventory(page, size, keyword, drugId));
    }

    @PostMapping
    public Result<Void> add(@RequestBody @Valid InventoryDTO inventoryDTO) {
        inventoryService.addInventory(inventoryDTO);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody @Valid InventoryDTO inventoryDTO) {
        inventoryService.updateInventory(id, inventoryDTO);
        return Result.success();
    }

    @PostMapping("/{id}/adjust")
    public Result<Void> adjustStock(@PathVariable Long id,
                                    @RequestParam int quantity,
                                    @RequestParam String reason) {
        inventoryService.adjustStock(id, quantity, reason, getCurrentUserId());
        return Result.success();
    }

    @GetMapping("/low-stock")
    public Result<List<InventoryVO>> lowStock() {
        return Result.success(inventoryService.getLowStockList());
    }

    @GetMapping("/expiring")
    public Result<List<InventoryVO>> expiring(@RequestParam(defaultValue = "90") int days) {
        return Result.success(inventoryService.getExpiringList(days));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        inventoryService.deleteInventory(id);
        return Result.success();
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getDetails() instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> details = (Map<String, Object>) auth.getDetails();
            Object uid = details.get("userId");
            if (uid instanceof Long) return (Long) uid;
            if (uid instanceof Integer) return ((Integer) uid).longValue();
        }
        return null;
    }
}
