package com.pharmacy.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InventoryLogVO {

    private Long id;
    private Long drugId;
    private String type;
    private Integer quantity;
    private Integer beforeQuantity;
    private Integer afterQuantity;
    private String reason;
    private String batchNumber;
    private Long relatedOrderId;
    private Long operatorId;
    private String remark;
    private LocalDateTime createTime;

    /** joined fields */
    private String drugName;
    private String operatorName;
}
