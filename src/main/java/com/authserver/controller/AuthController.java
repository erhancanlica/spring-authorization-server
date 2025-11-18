package com.authserver.controller;

import com.authserver.dto.*;
import com.authserver.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> registerWithEmail(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(userService.registerWithEmail(request));
    }

    @PostMapping("/register/phone")
    public ResponseEntity<ApiResponse<Void>> registerWithPhone(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(userService.registerWithPhone(request));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        String clientIp = getClientIp(httpRequest);
        return ResponseEntity.ok(userService.login(request, clientIp));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(@RequestParam String refreshToken) {
        return ResponseEntity.ok(userService.refreshToken(refreshToken));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        return ResponseEntity.ok(userService.verifyEmail(request.getToken()));
    }

    @GetMapping("/verify-email")
    public ResponseEntity<ApiResponse<Void>> verifyEmailByGet(@RequestParam String token) {
        return ResponseEntity.ok(userService.verifyEmail(token));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<ApiResponse<Void>> resendVerification(@RequestParam String email) {
        return ResponseEntity.ok(userService.resendVerificationEmail(email));
    }

    @PostMapping("/send-otp")
    public ResponseEntity<ApiResponse<Void>> sendOtp(
            @Valid @RequestBody SendOtpRequest request,
            HttpServletRequest httpRequest) {
        String clientIp = getClientIp(httpRequest);
        return ResponseEntity.ok(userService.sendOtp(request, clientIp));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<Void>> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        return ResponseEntity.ok(userService.verifyOtp(request));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return ResponseEntity.ok(userService.forgotPassword(request));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return ResponseEntity.ok(userService.resetPassword(request));
    }

    @GetMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPasswordByGet(
            @RequestParam String token,
            @RequestParam String newPassword) {
        ResetPasswordRequest request = ResetPasswordRequest.builder()
            .token(token)
            .newPassword(newPassword)
            .build();
        return ResponseEntity.ok(userService.resetPassword(request));
    }

    @GetMapping("/oauth2/success")
    public ResponseEntity<ApiResponse<String>> oauth2Success() {
        return ResponseEntity.ok(ApiResponse.success("OAuth2 login successful", null));
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}
