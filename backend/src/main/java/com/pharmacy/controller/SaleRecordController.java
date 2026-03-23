package com.pharmacy.controller;

import com.pharmacy.common.PageResult;
import com.pharmacy.common.Result;
import com.pharmacy.dto.SaleRecordDTO;
import com.pharmacy.service.SaleRecordService;
import com.pharmacy.vo.SaleRecordVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
public class SaleRecordController {

    private final SaleRecordService saleRecordService;

    @GetMapping
    public Result<PageResult<SaleRecordVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long drugId) {
        return Result.success(saleRecordService.listSales(page, size, keyword, drugId));
    }

    @GetMapping("/{id}")
    public Result<SaleRecordVO> getById(@PathVariable Long id) {
        return Result.success(saleRecordService.getSaleDetail(id));
    }

    @PostMapping
    public Result<Void> create(@RequestBody @Valid SaleRecordDTO dto) {
        saleRecordService.createSale(dto, getCurrentUserId());
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        saleRecordService.deleteSale(id);
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
