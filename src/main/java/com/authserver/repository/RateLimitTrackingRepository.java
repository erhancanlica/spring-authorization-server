package com.authserver.repository;

import com.authserver.model.RateLimitTracking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RateLimitTrackingRepository extends JpaRepository<RateLimitTracking, UUID> {

    Optional<RateLimitTracking> findByIdentifierAndActionTypeAndWindowStartAfter(
        String identifier, 
        String actionType, 
        LocalDateTime windowStart
    );

    void deleteByWindowStartBefore(LocalDateTime date);
}
