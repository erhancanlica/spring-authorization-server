package com.authserver.controller;

import com.authserver.dto.ApiResponse;
import com.authserver.dto.TwoFactorSetupResponse;
import com.authserver.service.TwoFactorAuthService;
import com.authserver.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/auth/2fa")
@RequiredArgsConstructor
public class TwoFactorAuthController {

    private final TwoFactorAuthService twoFactorAuthService;
    private final JwtUtil jwtUtil;

    @PostMapping("/enable")
    public ResponseEntity<ApiResponse<TwoFactorSetupResponse>> enable(
            @RequestHeader("Authorization") String authHeader) {
        UUID userId = extractUserIdFromToken(authHeader);
        TwoFactorSetupResponse response = twoFactorAuthService.setupTwoFactor(userId);
        return ResponseEntity.ok(ApiResponse.success("Two-factor authentication setup initiated", response));
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<Void>> verify(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam String code) {
        UUID userId = extractUserIdFromToken(authHeader);
        twoFactorAuthService.enableTwoFactor(userId, code);
        return ResponseEntity.ok(ApiResponse.success("Two-factor authentication enabled successfully", null));
    }

    @PostMapping("/disable")
    public ResponseEntity<ApiResponse<Void>> disable(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam String code) {
        UUID userId = extractUserIdFromToken(authHeader);
        twoFactorAuthService.disableTwoFactor(userId, code);
        return ResponseEntity.ok(ApiResponse.success("Two-factor authentication disabled successfully", null));
    }

    @GetMapping("/qrcode")
    public ResponseEntity<ApiResponse<TwoFactorSetupResponse>> getQrCode(
            @RequestHeader("Authorization") String authHeader) {
        UUID userId = extractUserIdFromToken(authHeader);
        TwoFactorSetupResponse response = twoFactorAuthService.setupTwoFactor(userId);
        return ResponseEntity.ok(ApiResponse.success("QR code generated", response));
    }

    private UUID extractUserIdFromToken(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String userIdStr = jwtUtil.extractUserId(token);
        return UUID.fromString(userIdStr);
    }
}
