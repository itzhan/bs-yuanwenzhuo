package com.pharmacy.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PrescriptionVO {

    private Long id;
    private String prescriptionNo;
    private Long doctorId;
    private String patientName;
    private Integer patientGender;
    private Integer patientAge;
    private String patientPhone;
    private String diagnosis;
    private String remark;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    /** joined fields */
    private String doctorName;
    private Integer itemCount;

    private List<PrescriptionItemVO> items;
}
