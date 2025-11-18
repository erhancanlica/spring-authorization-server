package com.authserver.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SmsService {

    @Value("${sms.account-sid}")
    private String accountSid;

    @Value("${sms.auth-token}")
    private String authToken;

    @Value("${sms.from-number}")
    private String fromNumber;

    @PostConstruct
    public void init() {
        if (accountSid != null && !accountSid.isEmpty() && 
            authToken != null && !authToken.isEmpty()) {
            Twilio.init(accountSid, authToken);
            log.info("Twilio SMS service initialized");
        } else {
            log.warn("Twilio credentials not configured. SMS functionality will be disabled.");
        }
    }

    public void sendOtp(String toPhoneNumber, String otp) {
        try {
            if (accountSid == null || accountSid.isEmpty()) {
                log.warn("SMS service not configured. OTP would be: {}", otp);
                return;
            }

            String messageBody = String.format(
                "Your verification code is: %s\n\nThis code will expire in 10 minutes.\n\n" +
                "If you didn't request this code, please ignore this message.",
                otp
            );

            Message message = Message.creator(
                new PhoneNumber(toPhoneNumber),
                new PhoneNumber(fromNumber),
                messageBody
            ).create();

            log.info("SMS sent successfully to: {} with SID: {}", toPhoneNumber, message.getSid());
        } catch (Exception e) {
            log.error("Failed to send SMS to: {}", toPhoneNumber, e);
            throw new RuntimeException("Failed to send SMS: " + e.getMessage());
        }
    }

    public void sendSms(String toPhoneNumber, String messageBody) {
        try {
            if (accountSid == null || accountSid.isEmpty()) {
                log.warn("SMS service not configured. Message would be: {}", messageBody);
                return;
            }

            Message message = Message.creator(
                new PhoneNumber(toPhoneNumber),
                new PhoneNumber(fromNumber),
                messageBody
            ).create();

            log.info("SMS sent successfully to: {} with SID: {}", toPhoneNumber, message.getSid());
        } catch (Exception e) {
            log.error("Failed to send SMS to: {}", toPhoneNumber, e);
            throw new RuntimeException("Failed to send SMS: " + e.getMessage());
        }
    }
}
