package com.authserver.service;

import com.authserver.dto.*;
import com.authserver.exception.CustomException;
import com.authserver.exception.UnauthorizedException;
import com.authserver.model.User;
import com.authserver.model.VerificationToken;
import com.authserver.repository.UserRepository;
import com.authserver.repository.VerificationTokenRepository;
import com.authserver.util.JwtUtil;
import com.authserver.util.OtpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final VerificationTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final OtpUtil otpUtil;
    private final EmailService emailService;
    private final SmsService smsService;
    private final RateLimitService rateLimitService;
    private final TwoFactorAuthService twoFactorAuthService;

    @Transactional
    public ApiResponse<Void> registerWithEmail(RegisterRequest request) {
        if (request.getEmail() == null || request.getEmail().isEmpty()) {
            throw new CustomException("Email is required");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new CustomException("Email already registered");
        }

        User user = User.builder()
            .email(request.getEmail())
            .passwordHash(passwordEncoder.encode(request.getPassword()))
            .emailVerified(false)
            .build();

        userRepository.save(user);

        // Send verification email
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = VerificationToken.builder()
            .user(user)
            .token(token)
            .type(VerificationToken.TokenType.EMAIL_VERIFICATION)
            .expiresAt(LocalDateTime.now().plusHours(24))
            .build();
        tokenRepository.save(verificationToken);

        emailService.sendVerificationEmail(user.getEmail(), token);

        log.info("User registered with email: {}", user.getEmail());
        return ApiResponse.success("Registration successful. Please check your email to verify your account.", null);
    }

    @Transactional
    public ApiResponse<Void> registerWithPhone(RegisterRequest request) {
        if (request.getPhoneNumber() == null || request.getPhoneNumber().isEmpty()) {
            throw new CustomException("Phone number is required");
        }

        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new CustomException("Phone number already registered");
        }

        User user = User.builder()
            .phoneNumber(request.getPhoneNumber())
            .passwordHash(passwordEncoder.encode(request.getPassword()))
            .phoneVerified(false)
            .build();

        userRepository.save(user);

        log.info("User registered with phone: {}", user.getPhoneNumber());
        return ApiResponse.success("Registration successful. Please verify your phone number.", null);
    }

    @Transactional
    public ApiResponse<TokenResponse> login(LoginRequest request, String clientIp) {
        rateLimitService.checkRateLimit(clientIp, "LOGIN");

        User user = findUserByIdentifier(request.getIdentifier());

        if (user.isAccountLocked()) {
            throw new UnauthorizedException("Account is locked. Please contact support.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            handleFailedLogin(user);
            throw new UnauthorizedException("Invalid credentials");
        }

        // Check if email or phone is verified
        if (user.getEmail() != null && !user.isEmailVerified()) {
            throw new UnauthorizedException("Please verify your email before logging in");
        }

        if (user.getPhoneNumber() != null && user.getEmail() == null && !user.isPhoneVerified()) {
            throw new UnauthorizedException("Please verify your phone number before logging in");
        }

        // Check 2FA
        if (user.isTwoFactorEnabled()) {
            if (request.getTwoFactorCode() == null || request.getTwoFactorCode().isEmpty()) {
                throw new UnauthorizedException("Two-factor authentication code required");
            }

            if (!twoFactorAuthService.verifyTwoFactorCode(user.getTwoFactorSecret(), request.getTwoFactorCode())) {
                throw new UnauthorizedException("Invalid two-factor authentication code");
            }
        }

        handleSuccessfulLogin(user);
        rateLimitService.resetRateLimit(clientIp, "LOGIN");

        String identifier = user.getEmail() != null ? user.getEmail() : user.getPhoneNumber();
        TokenResponse tokenResponse = TokenResponse.builder()
            .accessToken(jwtUtil.generateAccessToken(identifier, user.getId().toString()))
            .refreshToken(jwtUtil.generateRefreshToken(identifier, user.getId().toString()))
            .tokenType("Bearer")
            .expiresIn(3600L)
            .scope("openid profile email")
            .build();

        log.info("User logged in: {}", identifier);
        return ApiResponse.success("Login successful", tokenResponse);
    }

    @Transactional
    public ApiResponse<TokenResponse> refreshToken(String refreshToken) {
        try {
            String username = jwtUtil.extractUsername(refreshToken);
            String userId = jwtUtil.extractUserId(refreshToken);
            String tokenType = jwtUtil.extractTokenType(refreshToken);

            if (!"refresh".equals(tokenType)) {
                throw new UnauthorizedException("Invalid token type");
            }

            if (jwtUtil.isTokenExpired(refreshToken)) {
                throw new UnauthorizedException("Refresh token expired");
            }

            TokenResponse tokenResponse = TokenResponse.builder()
                .accessToken(jwtUtil.generateAccessToken(username, userId))
                .refreshToken(jwtUtil.generateRefreshToken(username, userId))
                .tokenType("Bearer")
                .expiresIn(3600L)
                .scope("openid profile email")
                .build();

            return ApiResponse.success("Token refreshed successfully", tokenResponse);
        } catch (Exception e) {
            throw new UnauthorizedException("Invalid refresh token");
        }
    }

    @Transactional
    public ApiResponse<Void> verifyEmail(String token) {
        VerificationToken verificationToken = tokenRepository.findByToken(token)
            .orElseThrow(() -> new CustomException("Invalid or expired token"));

        if (verificationToken.isUsed()) {
            throw new CustomException("Token already used");
        }

        if (verificationToken.isExpired()) {
            throw new CustomException("Token expired");
        }

        if (verificationToken.getType() != VerificationToken.TokenType.EMAIL_VERIFICATION) {
            throw new CustomException("Invalid token type");
        }

        User user = verificationToken.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);

        verificationToken.setUsed(true);
        tokenRepository.save(verificationToken);

        log.info("Email verified for user: {}", user.getEmail());
        return ApiResponse.success("Email verified successfully", null);
    }

    @Transactional
    public ApiResponse<Void> resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new CustomException("User not found"));

        if (user.isEmailVerified()) {
            throw new CustomException("Email already verified");
        }

        // Delete old tokens
        tokenRepository.deleteByUserAndType(user, VerificationToken.TokenType.EMAIL_VERIFICATION);

        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = VerificationToken.builder()
            .user(user)
            .token(token)
            .type(VerificationToken.TokenType.EMAIL_VERIFICATION)
            .expiresAt(LocalDateTime.now().plusHours(24))
            .build();
        tokenRepository.save(verificationToken);

        emailService.sendVerificationEmail(user.getEmail(), token);

        log.info("Verification email resent to: {}", email);
        return ApiResponse.success("Verification email sent", null);
    }

    @Transactional
    public ApiResponse<Void> sendOtp(SendOtpRequest request, String clientIp) {
        rateLimitService.checkRateLimit(clientIp, "SMS_OTP");

        User user = userRepository.findByPhoneNumber(request.getPhoneNumber())
            .orElseThrow(() -> new CustomException("User not found"));

        if (user.isPhoneVerified()) {
            throw new CustomException("Phone number already verified");
        }

        // Delete old OTP tokens
        tokenRepository.deleteByUserAndType(user, VerificationToken.TokenType.SMS_OTP);

        String otp = otpUtil.generateOtp();
        VerificationToken otpToken = VerificationToken.builder()
            .user(user)
            .token(otp)
            .type(VerificationToken.TokenType.SMS_OTP)
            .expiresAt(LocalDateTime.now().plusMinutes(10))
            .build();
        tokenRepository.save(otpToken);

        smsService.sendOtp(user.getPhoneNumber(), otp);

        log.info("OTP sent to: {}", request.getPhoneNumber());
        return ApiResponse.success("OTP sent successfully", null);
    }

    @Transactional
    public ApiResponse<Void> verifyOtp(VerifyOtpRequest request) {
        User user = userRepository.findByPhoneNumber(request.getPhoneNumber())
            .orElseThrow(() -> new CustomException("User not found"));

        VerificationToken otpToken = tokenRepository.findByToken(request.getOtpCode())
            .orElseThrow(() -> new CustomException("Invalid OTP"));

        if (otpToken.isUsed()) {
            throw new CustomException("OTP already used");
        }

        if (otpToken.isExpired()) {
            throw new CustomException("OTP expired");
        }

        if (otpToken.getType() != VerificationToken.TokenType.SMS_OTP) {
            throw new CustomException("Invalid token type");
        }

        if (!otpToken.getUser().getId().equals(user.getId())) {
            throw new CustomException("Invalid OTP");
        }

        user.setPhoneVerified(true);
        userRepository.save(user);

        otpToken.setUsed(true);
        tokenRepository.save(otpToken);

        log.info("Phone verified for user: {}", user.getPhoneNumber());
        return ApiResponse.success("Phone number verified successfully", null);
    }

    @Transactional
    public ApiResponse<Void> forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new CustomException("User not found"));

        // Delete old password reset tokens
        tokenRepository.deleteByUserAndType(user, VerificationToken.TokenType.PASSWORD_RESET);

        String token = UUID.randomUUID().toString();
        VerificationToken resetToken = VerificationToken.builder()
            .user(user)
            .token(token)
            .type(VerificationToken.TokenType.PASSWORD_RESET)
            .expiresAt(LocalDateTime.now().plusHours(1))
            .build();
        tokenRepository.save(resetToken);

        emailService.sendPasswordResetEmail(user.getEmail(), token);

        log.info("Password reset email sent to: {}", request.getEmail());
        return ApiResponse.success("Password reset link sent to your email", null);
    }

    @Transactional
    public ApiResponse<Void> resetPassword(ResetPasswordRequest request) {
        VerificationToken resetToken = tokenRepository.findByToken(request.getToken())
            .orElseThrow(() -> new CustomException("Invalid or expired token"));

        if (resetToken.isUsed()) {
            throw new CustomException("Token already used");
        }

        if (resetToken.isExpired()) {
            throw new CustomException("Token expired");
        }

        if (resetToken.getType() != VerificationToken.TokenType.PASSWORD_RESET) {
            throw new CustomException("Invalid token type");
        }

        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setFailedLoginAttempts(0);
        user.setAccountLocked(false);
        userRepository.save(user);

        resetToken.setUsed(true);
        tokenRepository.save(resetToken);

        log.info("Password reset successful for user: {}", user.getEmail());
        return ApiResponse.success("Password reset successful", null);
    }

    private User findUserByIdentifier(String identifier) {
        return userRepository.findByEmail(identifier)
            .or(() -> userRepository.findByPhoneNumber(identifier))
            .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));
    }

    private void handleFailedLogin(User user) {
        user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
        
        if (user.getFailedLoginAttempts() >= 5) {
            user.setAccountLocked(true);
            log.warn("Account locked due to too many failed login attempts: {}", 
                user.getEmail() != null ? user.getEmail() : user.getPhoneNumber());
        }
        
        userRepository.save(user);
    }

    private void handleSuccessfulLogin(User user) {
        user.setFailedLoginAttempts(0);
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
    }

    @Scheduled(cron = "0 0 0 * * *") // Daily at midnight
    @Transactional
    public void cleanupExpiredTokens() {
        tokenRepository.deleteByExpiresAtBefore(LocalDateTime.now());
        log.info("Cleaned up expired verification tokens");
    }
}
