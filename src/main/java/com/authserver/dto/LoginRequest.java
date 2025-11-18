package com.authserver.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "Email or phone number is required")
    private String identifier; // Can be email or phone number

    @NotBlank(message = "Password is required")
    private String password;

    private String twoFactorCode; // Optional, for 2FA
}
