package com.pharmacy.dto;

import lombok.Data;

@Data
public class UserDTO {

    private String username;
    private String password;
    private String realName;
    private String phone;
    private String email;
    private Integer role;
    private Integer status;
}
