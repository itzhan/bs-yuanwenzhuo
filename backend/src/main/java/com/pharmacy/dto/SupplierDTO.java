package com.pharmacy.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SupplierDTO {

    @NotBlank(message = "供应商名称不能为空")
    private String name;

    private String contactPerson;
    private String phone;
    private String email;
    private String address;
    private Integer status;
}
