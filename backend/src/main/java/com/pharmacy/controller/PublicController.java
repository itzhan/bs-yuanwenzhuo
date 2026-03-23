package com.pharmacy.controller;

import com.pharmacy.common.PageResult;
import com.pharmacy.common.Result;
import com.pharmacy.service.DrugCategoryService;
import com.pharmacy.service.DrugService;
import com.pharmacy.vo.DrugVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicController {

    private final DrugService drugService;
    private final DrugCategoryService drugCategoryService;
    private final com.pharmacy.service.DashboardService dashboardService;

    /**
     * 公开药品列表（仅显示上架药品）
     */
    @GetMapping("/drugs")
    public Result<PageResult<DrugVO>> listDrugs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId) {
        return Result.success(drugService.listDrugs(page, size, keyword, categoryId, 1));
    }

    /**
     * 公开药品详情
     */
    @GetMapping("/drugs/{id}")
    public Result<DrugVO> getDrugDetail(@PathVariable Long id) {
        return Result.success(drugService.getDrugDetail(id));
    }

    /**
     * 公开分类树
     */
    @GetMapping("/categories")
    public Result<List<Map<String, Object>>> getCategoryTree() {
        return Result.success(drugCategoryService.listTree());
    }

    /**
     * 公开统计数据（精简版）
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getPublicStats() {
        var dashboard = dashboardService.getDashboardData();
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("drugCount", dashboard.getDrugCount());
        stats.put("categoryCount", drugCategoryService.listAll().size());
        stats.put("supplierCount", dashboard.getSupplierCount());
        stats.put("categoryDistribution", dashboard.getCategoryDistribution());
        return Result.success(stats);
    }
}
