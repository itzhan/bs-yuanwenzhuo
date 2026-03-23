package com.pharmacy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("expiry_alert")
public class ExpiryAlert {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long drugId;

    private Long inventoryId;

    private String batchNumber;

    private LocalDate expiryDate;

    private Integer alertLevel;

    private Integer status;

    private String handlerNote;

    private LocalDateTime handleTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
