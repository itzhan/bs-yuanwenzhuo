package com.pharmacy.controller;

import com.pharmacy.common.Result;
import com.pharmacy.service.DashboardService;
import com.pharmacy.vo.DashboardVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public Result<DashboardVO> getData() {
        return Result.success(dashboardService.getDashboardData());
    }
}
