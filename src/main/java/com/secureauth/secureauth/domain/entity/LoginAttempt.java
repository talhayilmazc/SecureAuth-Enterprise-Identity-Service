package com.secureauth.secureauth.domain.entity;

import com.secureauth.secureauth.domain.enums.LoginResult;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "login_attempts")
public class LoginAttempt extends BaseEntity {

    @Column(nullable = false, length = 50)
    private String username;

    @Column(length = 45)
    private String ipAddress;

    @Column(length = 200)
    private String userAgent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LoginResult result;

    @Column(length = 500)
    private String failureReason;
}