package com.pharmacy.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PurchaseOrderItemVO {

    private Long id;
    private Long orderId;
    private Long drugId;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    private LocalDateTime createTime;

    /** joined from drug table */
    private String drugName;
    private String specification;
    private String unit;
}
