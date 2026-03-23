package com.pharmacy.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ExpiryAlertHandleDTO {

    @NotBlank(message = "处理说明不能为空")
    private String handlerNote;
}
