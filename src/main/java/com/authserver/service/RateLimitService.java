package com.authserver.service;

import com.authserver.exception.RateLimitExceededException;
import com.authserver.model.RateLimitTracking;
import com.authserver.repository.RateLimitTrackingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final RateLimitTrackingRepository rateLimitRepository;

    @Value("${rate-limit.login-attempts}")
    private Integer loginAttempts;

    @Value("${rate-limit.window-minutes}")
    private Integer windowMinutes;

    @Value("${rate-limit.sms-attempts}")
    private Integer smsAttempts;

    @Value("${rate-limit.window-minutes-sms}")
    private Integer windowMinutesSms;

    @Value("${rate-limit.api-requests}")
    private Integer apiRequests;

    @Value("${rate-limit.window-minutes-api}")
    private Integer windowMinutesApi;

    @Transactional
    public void checkRateLimit(String identifier, String actionType) {
        int maxAttempts;
        int windowMinutes;

        switch (actionType) {
            case "LOGIN":
                maxAttempts = loginAttempts;
                windowMinutes = this.windowMinutes;
                break;
            case "SMS_OTP":
                maxAttempts = smsAttempts;
                windowMinutes = windowMinutesSms;
                break;
            case "API_REQUEST":
                maxAttempts = apiRequests;
                windowMinutes = windowMinutesApi;
                break;
            default:
                maxAttempts = 100;
                windowMinutes = 1;
        }

        LocalDateTime windowStart = LocalDateTime.now().minusMinutes(windowMinutes);
        
        var tracking = rateLimitRepository.findByIdentifierAndActionTypeAndWindowStartAfter(
            identifier, actionType, windowStart
        ).orElse(null);

        if (tracking != null) {
            if (tracking.getAttemptCount() >= maxAttempts) {
                log.warn("Rate limit exceeded for identifier: {}, action: {}", identifier, actionType);
                throw new RateLimitExceededException(
                    String.format("Too many attempts. Please try again after %d minutes.", windowMinutes)
                );
            }
            tracking.setAttemptCount(tracking.getAttemptCount() + 1);
            rateLimitRepository.save(tracking);
        } else {
            RateLimitTracking newTracking = RateLimitTracking.builder()
                .identifier(identifier)
                .actionType(actionType)
                .attemptCount(1)
                .windowStart(LocalDateTime.now())
                .build();
            rateLimitRepository.save(newTracking);
        }
    }

    @Transactional
    public void resetRateLimit(String identifier, String actionType) {
        LocalDateTime windowStart = LocalDateTime.now().minusHours(24);
        rateLimitRepository.findByIdentifierAndActionTypeAndWindowStartAfter(
            identifier, actionType, windowStart
        ).ifPresent(rateLimitRepository::delete);
    }

    @Scheduled(cron = "0 0 * * * *") // Every hour
    @Transactional
    public void cleanupExpiredRateLimits() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
        rateLimitRepository.deleteByWindowStartBefore(cutoff);
        log.info("Cleaned up expired rate limit records");
    }
}
