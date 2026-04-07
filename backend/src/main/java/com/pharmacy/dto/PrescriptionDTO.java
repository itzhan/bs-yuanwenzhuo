package com.pharmacy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class PrescriptionDTO {

    @NotBlank(message = "患者姓名不能为空")
    private String patientName;

    private Integer patientGender;

    private Integer patientAge;

    private String patientPhone;

    private String diagnosis;

    private String remark;

    @NotEmpty(message = "处方药品不能为空")
    private List<PrescriptionItemDTO> items;
}
