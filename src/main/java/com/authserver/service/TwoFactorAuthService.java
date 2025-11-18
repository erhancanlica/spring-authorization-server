package com.authserver.service;

import com.authserver.dto.TwoFactorSetupResponse;
import com.authserver.exception.CustomException;
import com.authserver.model.User;
import com.authserver.repository.UserRepository;
import com.authserver.util.QRCodeUtil;
import com.google.zxing.WriterException;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TwoFactorAuthService {

    private final UserRepository userRepository;
    private final QRCodeUtil qrCodeUtil;
    private final GoogleAuthenticator googleAuthenticator = new GoogleAuthenticator();

    @Value("${two-factor.issuer}")
    private String issuer;

    @Transactional
    public TwoFactorSetupResponse setupTwoFactor(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException("User not found"));

        if (user.isTwoFactorEnabled()) {
            throw new CustomException("Two-factor authentication is already enabled");
        }

        GoogleAuthenticatorKey key = googleAuthenticator.createCredentials();
        String secret = key.getKey();

        user.setTwoFactorSecret(secret);
        userRepository.save(user);

        String accountName = user.getEmail() != null ? user.getEmail() : user.getPhoneNumber();
        String otpAuthUrl = GoogleAuthenticatorQRGenerator.getOtpAuthTotpURL(
            issuer, accountName, key
        );

        try {
            String qrCodeBase64 = qrCodeUtil.generateQRCodeBase64(otpAuthUrl);
            
            return TwoFactorSetupResponse.builder()
                .secret(secret)
                .qrCodeUrl("data:image/png;base64," + qrCodeBase64)
                .manualEntryKey(secret)
                .build();
        } catch (WriterException | IOException e) {
            log.error("Failed to generate QR code", e);
            throw new CustomException("Failed to generate QR code");
        }
    }

    @Transactional
    public void enableTwoFactor(UUID userId, String verificationCode) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException("User not found"));

        if (user.getTwoFactorSecret() == null) {
            throw new CustomException("Two-factor authentication not set up. Please set up first.");
        }

        if (!verifyTwoFactorCode(user.getTwoFactorSecret(), verificationCode)) {
            throw new CustomException("Invalid verification code");
        }

        user.setTwoFactorEnabled(true);
        userRepository.save(user);
        log.info("Two-factor authentication enabled for user: {}", userId);
    }

    @Transactional
    public void disableTwoFactor(UUID userId, String verificationCode) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException("User not found"));

        if (!user.isTwoFactorEnabled()) {
            throw new CustomException("Two-factor authentication is not enabled");
        }

        if (!verifyTwoFactorCode(user.getTwoFactorSecret(), verificationCode)) {
            throw new CustomException("Invalid verification code");
        }

        user.setTwoFactorEnabled(false);
        user.setTwoFactorSecret(null);
        userRepository.save(user);
        log.info("Two-factor authentication disabled for user: {}", userId);
    }

    public boolean verifyTwoFactorCode(String secret, String code) {
        try {
            int codeInt = Integer.parseInt(code);
            return googleAuthenticator.authorize(secret, codeInt);
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
