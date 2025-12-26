package com.taobaodemo.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    private Integer userType; // 1:运营商, 2:店铺, 3:顾客, 4:浏览者
}

