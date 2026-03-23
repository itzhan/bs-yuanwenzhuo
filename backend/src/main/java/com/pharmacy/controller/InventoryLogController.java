package com.pharmacy.controller;

import com.pharmacy.common.PageResult;
import com.pharmacy.common.Result;
import com.pharmacy.entity.InventoryLog;
import com.pharmacy.service.InventoryLogService;
import com.pharmacy.vo.InventoryLogVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory-logs")
@RequiredArgsConstructor
public class InventoryLogController {

    private final InventoryLogService inventoryLogService;

    @GetMapping
    public Result<PageResult<InventoryLogVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long drugId,
            @RequestParam(required = false) Integer type) {
        return Result.success(inventoryLogService.listLogs(page, size, drugId, type));
    }

    @PostMapping
    public Result<Void> create(@RequestBody InventoryLog log) {
        inventoryLogService.save(log);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        inventoryLogService.removeById(id);
        return Result.success(null);
    }
}
