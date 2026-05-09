package com.secureauth.secureauth.service;

import com.secureauth.secureauth.domain.enums.AuditAction;
import com.secureauth.secureauth.dto.response.AuditLogResponse;

import java.util.List;

public interface AuditLogService {
    void log(String username, AuditAction action, String resource,
             String ipAddress, String userAgent, String details, boolean success);
    List<AuditLogResponse> getByUsername(String username);
    List<AuditLogResponse> getByAction(AuditAction action);
}