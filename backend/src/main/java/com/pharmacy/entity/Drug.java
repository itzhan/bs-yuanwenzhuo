package com.pharmacy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("drug")
public class Drug {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String drugCode;

    private String genericName;

    private String tradeName;

    private String dosageForm;

    private String specification;

    private String manufacturer;

    private String approvalNumber;

    private String barcode;

    private Long categoryId;

    private String unit;

    private BigDecimal purchasePrice;

    private BigDecimal retailPrice;

    private String description;

    private String image;

    private Integer status;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
