package com.pharmacy.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DrugDTO {

    private String drugCode;

    @NotBlank(message = "通用名不能为空")
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
}
