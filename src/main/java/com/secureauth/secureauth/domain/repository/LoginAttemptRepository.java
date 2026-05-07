package com.secureauth.secureauth.domain.repository;

import com.secureauth.secureauth.domain.entity.LoginAttempt;
import com.secureauth.secureauth.domain.enums.LoginResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, Long> {

    List<LoginAttempt> findByUsernameOrderByCreatedAtDesc(String username);

    @Query("SELECT COUNT(la) FROM LoginAttempt la WHERE la.username = :username " +
           "AND la.result != 'SUCCESS' AND la.createdAt > :since")
    long countFailedAttemptsSince(
            @Param("username") String username,
            @Param("since") LocalDateTime since);

    @Query("SELECT COUNT(la) FROM LoginAttempt la WHERE la.ipAddress = :ip " +
           "AND la.result != 'SUCCESS' AND la.createdAt > :since")
    long countFailedAttemptsByIpSince(
            @Param("ip") String ipAddress,
            @Param("since") LocalDateTime since);
}