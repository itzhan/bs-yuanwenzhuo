package com.pharmacy.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SaleRecordVO {

    private Long id;
    private String saleNo;
    private Long drugId;
    private String batchNumber;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    private Long operatorId;
    private String customerName;
    private LocalDateTime saleTime;
    private LocalDateTime createTime;

    /** joined fields */
    private String drugName;
    private String operatorName;
}
