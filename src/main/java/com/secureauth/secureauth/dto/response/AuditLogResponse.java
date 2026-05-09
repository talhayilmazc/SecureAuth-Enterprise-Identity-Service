package com.secureauth.secureauth.dto.response;

import com.secureauth.secureauth.domain.enums.AuditAction;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLogResponse {
    private Long id;
    private String username;
    private AuditAction action;
    private String resource;
    private String ipAddress;
    private String details;
    private Boolean success;
    private LocalDateTime createdAt;
}