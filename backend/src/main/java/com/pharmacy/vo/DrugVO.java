package com.pharmacy.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DrugVO {

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
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    /** joined from drug_category table */
    private String categoryName;
}
