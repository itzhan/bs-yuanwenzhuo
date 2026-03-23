package com.pharmacy.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class InventoryVO {

    private Long id;
    private Long drugId;
    private String batchNumber;
    private Integer quantity;
    private Integer warningThreshold;
    private LocalDate productionDate;
    private LocalDate expiryDate;
    private String location;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    /** joined from drug table */
    private String drugName;
    private String drugCode;
    private String specification;
    private String unit;
}
