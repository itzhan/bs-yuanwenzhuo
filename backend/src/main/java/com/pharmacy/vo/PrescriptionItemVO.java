package com.pharmacy.vo;

import lombok.Data;

@Data
public class PrescriptionItemVO {

    private Long id;
    private Long prescriptionId;
    private Long drugId;
    private Integer quantity;
    private String dosage;
    private String usageNote;

    /** joined fields */
    private String drugName;
    private String specification;
    private String unit;
    private String manufacturer;
}
