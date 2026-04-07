package com.pharmacy.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PrescriptionItemDTO {

    @NotNull(message = "药品不能为空")
    private Long drugId;

    @NotNull(message = "数量不能为空")
    private Integer quantity;

    private String dosage;

    private String usageNote;
}
