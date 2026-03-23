package com.pharmacy.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class InventoryDTO {

    @NotNull(message = "药品ID不能为空")
    private Long drugId;

    private String batchNumber;

    @NotNull(message = "数量不能为空")
    private Integer quantity;

    private Integer warningThreshold;
    private LocalDate productionDate;
    private LocalDate expiryDate;
    private String location;
}
