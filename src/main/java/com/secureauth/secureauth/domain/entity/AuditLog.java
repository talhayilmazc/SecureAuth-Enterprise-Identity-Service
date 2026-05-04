package com.secureauth.secureauth.domain.entity;

import com.secureauth.secureauth.domain.enums.AuditAction;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "audit_logs")
public class AuditLog extends BaseEntity {

    @Column(nullable = false, length = 50)
    private String username;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AuditAction action;

    @Column(length = 100)
    private String resource;

    @Column(length = 45)
    private String ipAddress;

    @Column(length = 200)
    private String userAgent;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(nullable = false)
    @Builder.Default
    private Boolean success = true;
}