package com.pharmacy.controller;

import com.pharmacy.common.PageResult;
import com.pharmacy.common.Result;
import com.pharmacy.dto.PurchaseOrderDTO;
import com.pharmacy.service.PurchaseOrderService;
import com.pharmacy.vo.PurchaseOrderVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/purchase-orders")
@RequiredArgsConstructor
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    @GetMapping
    public Result<PageResult<PurchaseOrderVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        return Result.success(purchaseOrderService.listOrders(page, size, status != null ? status : null, keyword));
    }

    @GetMapping("/{id}")
    public Result<PurchaseOrderVO> getById(@PathVariable Long id) {
        return Result.success(purchaseOrderService.getOrderDetail(id));
    }

    @PostMapping
    public Result<Void> create(@RequestBody @Valid PurchaseOrderDTO dto) {
        purchaseOrderService.createOrder(dto, getCurrentUserId());
        return Result.success();
    }

    @PutMapping("/{id}/approve")
    public Result<Void> approve(@PathVariable Long id) {
        purchaseOrderService.approveOrder(id, getCurrentUserId());
        return Result.success();
    }

    @PutMapping("/{id}/receive")
    public Result<Void> receive(@PathVariable Long id) {
        purchaseOrderService.receiveOrder(id, getCurrentUserId());
        return Result.success();
    }

    @PutMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id) {
        purchaseOrderService.cancelOrder(id);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        purchaseOrderService.deleteOrder(id);
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
