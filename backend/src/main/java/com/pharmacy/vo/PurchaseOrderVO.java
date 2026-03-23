package com.pharmacy.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PurchaseOrderVO {

    private Long id;
    private String orderNo;
    private Long supplierId;
    private BigDecimal totalAmount;
    private Integer status;
    private Long creatorId;
    private Long approverId;
    private LocalDateTime approveTime;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    /** joined fields */
    private String supplierName;
    private String creatorName;
    private String approverName;

    private List<PurchaseOrderItemVO> items;
}
