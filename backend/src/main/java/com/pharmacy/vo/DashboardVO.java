package com.pharmacy.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class DashboardVO {

    private long drugCount;
    private long supplierCount;
    private long lowStockCount;
    private long expiryAlertCount;
    private BigDecimal totalSalesAmount;
    private BigDecimal totalPurchaseAmount;

    /** daily/weekly/monthly sales data for chart rendering */
    private List<Map<String, Object>> salesTrend;

    /** drug category distribution for pie/bar chart */
    private List<Map<String, Object>> categoryDistribution;
}
