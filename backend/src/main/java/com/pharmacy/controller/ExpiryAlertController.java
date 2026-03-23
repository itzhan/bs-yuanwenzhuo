package com.pharmacy.controller;

import com.pharmacy.common.PageResult;
import com.pharmacy.common.Result;
import com.pharmacy.dto.ExpiryAlertHandleDTO;
import com.pharmacy.service.ExpiryAlertService;
import com.pharmacy.vo.ExpiryAlertVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/expiry-alerts")
@RequiredArgsConstructor
public class ExpiryAlertController {

    private final ExpiryAlertService expiryAlertService;

    @GetMapping
    public Result<PageResult<ExpiryAlertVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer alertLevel,
            @RequestParam(required = false) Integer status) {
        return Result.success(expiryAlertService.listAlerts(page, size, alertLevel, status));
    }

    @PutMapping("/{id}/handle")
    public Result<Void> handle(@PathVariable Long id, @RequestBody @Valid ExpiryAlertHandleDTO dto) {
        expiryAlertService.handleAlert(id, dto);
        return Result.success();
    }

    @PostMapping("/generate")
    public Result<Void> generate() {
        expiryAlertService.generateAlerts();
        return Result.success();
    }

    @GetMapping("/count")
    public Result<Long> countPending() {
        return Result.success(expiryAlertService.countPendingAlerts());
    }
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        expiryAlertService.deleteAlert(id);
        return Result.success();
    }
}
