package com.pharmacy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("prescription")
public class Prescription {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String prescriptionNo;

    private Long doctorId;

    private String patientName;

    private Integer patientGender;

    private Integer patientAge;

    private String patientPhone;

    private String diagnosis;

    private String remark;

    /** 0-作废 1-有效 */
    private Integer status;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
