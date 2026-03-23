package com.pharmacy.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ExpiryAlertVO {

    private Long id;
    private Long drugId;
    private String batchNumber;
    private LocalDate expiryDate;
    private Integer alertLevel;
    private Integer status;
    private Long handlerId;
    private String handlerNote;
    private LocalDateTime handleTime;
    private LocalDateTime createTime;

    /** joined from drug table */
    private String drugName;
    private String specification;
}
