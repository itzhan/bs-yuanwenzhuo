package com.pharmacy.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class PurchaseOrderDTO {

    @NotNull(message = "供应商不能为空")
    private Long supplierId;

    private String remark;

    @NotEmpty(message = "采购明细不能为空")
    private List<PurchaseOrderItemDTO> items;
}
