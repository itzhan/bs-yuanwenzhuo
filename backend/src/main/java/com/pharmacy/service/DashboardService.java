package com.pharmacy.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pharmacy.entity.*;
import com.pharmacy.mapper.*;
import com.pharmacy.vo.DashboardVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    @Autowired
    private DrugMapper drugMapper;

    @Autowired
    private SupplierMapper supplierMapper;

    @Autowired
    private InventoryMapper inventoryMapper;

    @Autowired
    private ExpiryAlertMapper expiryAlertMapper;

    @Autowired
    private SaleRecordMapper saleRecordMapper;

    @Autowired
    private PurchaseOrderMapper purchaseOrderMapper;

    @Autowired
    private DrugCategoryMapper drugCategoryMapper;

    public DashboardVO getDashboardData() {
        DashboardVO vo = new DashboardVO();

        vo.setDrugCount(drugMapper.selectCount(new LambdaQueryWrapper<>()));
        vo.setSupplierCount(supplierMapper.selectCount(new LambdaQueryWrapper<>()));

        vo.setLowStockCount(inventoryMapper.selectCount(
                new QueryWrapper<Inventory>().apply("quantity <= warning_threshold")));

        vo.setExpiryAlertCount(expiryAlertMapper.selectCount(
                new LambdaQueryWrapper<ExpiryAlert>().eq(ExpiryAlert::getStatus, 0)));

        List<SaleRecord> allSales = saleRecordMapper.selectList(new LambdaQueryWrapper<>());
        BigDecimal totalSales = allSales.stream()
                .map(SaleRecord::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        vo.setTotalSalesAmount(totalSales);

        List<PurchaseOrder> completedOrders = purchaseOrderMapper.selectList(
                new LambdaQueryWrapper<PurchaseOrder>().eq(PurchaseOrder::getStatus, 2));
        BigDecimal totalPurchase = completedOrders.stream()
                .map(PurchaseOrder::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        vo.setTotalPurchaseAmount(totalPurchase);

        DateTimeFormatter monthFmt = DateTimeFormatter.ofPattern("yyyy-MM");
        LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6)
                .withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);

        Map<String, BigDecimal> monthlyMap = new LinkedHashMap<>();
        for (int i = 5; i >= 0; i--) {
            monthlyMap.put(LocalDateTime.now().minusMonths(i).format(monthFmt), BigDecimal.ZERO);
        }

        allSales.stream()
                .filter(s -> s.getCreateTime() != null && s.getCreateTime().isAfter(sixMonthsAgo))
                .forEach(s -> {
                    String key = s.getCreateTime().format(monthFmt);
                    monthlyMap.computeIfPresent(key, (k, v) ->
                            v.add(s.getAmount() != null ? s.getAmount() : BigDecimal.ZERO));
                });

        List<Map<String, Object>> salesTrend = new ArrayList<>();
        monthlyMap.forEach((month, amount) -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("month", month);
            item.put("amount", amount);
            salesTrend.add(item);
        });
        vo.setSalesTrend(salesTrend);

        List<Drug> allDrugs = drugMapper.selectList(new LambdaQueryWrapper<>());
        Map<Long, Long> countByCategory = allDrugs.stream()
                .filter(d -> d.getCategoryId() != null)
                .collect(Collectors.groupingBy(Drug::getCategoryId, Collectors.counting()));

        Map<Long, String> categoryNameMap = Map.of();
        if (!countByCategory.isEmpty()) {
            categoryNameMap = drugCategoryMapper.selectBatchIds(countByCategory.keySet()).stream()
                    .collect(Collectors.toMap(DrugCategory::getId, DrugCategory::getName));
        }

        Map<Long, String> finalCatMap = categoryNameMap;
        List<Map<String, Object>> categoryDistribution = new ArrayList<>();
        countByCategory.forEach((catId, cnt) -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("categoryId", catId);
            item.put("categoryName", finalCatMap.getOrDefault(catId, "未分类"));
            item.put("count", cnt);
            categoryDistribution.add(item);
        });
        vo.setCategoryDistribution(categoryDistribution);

        return vo;
    }
}
