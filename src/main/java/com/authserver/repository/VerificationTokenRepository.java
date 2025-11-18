package com.authserver.repository;

import com.authserver.model.User;
import com.authserver.model.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, UUID> {

    Optional<VerificationToken> findByToken(String token);

    List<VerificationToken> findByUserAndType(User user, VerificationToken.TokenType type);

    void deleteByExpiresAtBefore(LocalDateTime date);

    void deleteByUserAndType(User user, VerificationToken.TokenType type);
}
