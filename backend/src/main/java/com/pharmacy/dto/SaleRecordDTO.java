package com.pharmacy.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SaleRecordDTO {

    @NotNull(message = "药品ID不能为空")
    private Long drugId;

    private String batchNumber;

    @NotNull(message = "数量不能为空")
    private Integer quantity;

    @NotNull(message = "单价不能为空")
    private BigDecimal unitPrice;

    private String customerName;
}
