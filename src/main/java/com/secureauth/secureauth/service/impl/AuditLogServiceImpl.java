package com.secureauth.secureauth.service.impl;

import com.secureauth.secureauth.domain.entity.AuditLog;
import com.secureauth.secureauth.domain.enums.AuditAction;
import com.secureauth.secureauth.domain.repository.AuditLogRepository;
import com.secureauth.secureauth.dto.response.AuditLogResponse;
import com.secureauth.secureauth.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Async
    @Override
    @Transactional
    public void log(String username, AuditAction action, String resource,
                    String ipAddress, String userAgent, String details, boolean success) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .username(username)
                    .action(action)
                    .resource(resource)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .details(details)
                    .success(success)
                    .build();
            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("Failed to save audit log", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogResponse> getByUsername(String username) {
        return auditLogRepository.findByUsernameOrderByCreatedAtDesc(username)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogResponse> getByAction(AuditAction action) {
        return auditLogRepository.findByAction(action)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    private AuditLogResponse toResponse(AuditLog log) {
        return AuditLogResponse.builder()
                .id(log.getId())
                .username(log.getUsername())
                .action(log.getAction())
                .resource(log.getResource())
                .ipAddress(log.getIpAddress())
                .details(log.getDetails())
                .success(log.getSuccess())
                .createdAt(log.getCreatedAt())
                .build();
    }
}