package com.pharmacy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("sale_record")
public class SaleRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;

    private Long drugId;

    private String batchNumber;

    private Integer quantity;

    private BigDecimal unitPrice;

    private BigDecimal amount;

    private String customerName;

    private Long operatorId;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
