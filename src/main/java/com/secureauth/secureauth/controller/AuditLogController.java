package com.secureauth.secureauth.controller;

import com.secureauth.secureauth.domain.enums.AuditAction;
import com.secureauth.secureauth.dto.response.AuditLogResponse;
import com.secureauth.secureauth.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/audit-logs")
@RequiredArgsConstructor
@Tag(name = "Audit Logs", description = "Güvenlik denetim kayıtları")
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping("/user/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Kullanıcıya göre denetim kayıtları")
    public ResponseEntity<List<AuditLogResponse>> getByUsername(
            @PathVariable String username) {
        return ResponseEntity.ok(auditLogService.getByUsername(username));
    }

    @GetMapping("/action/{action}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "İşleme göre denetim kayıtları")
    public ResponseEntity<List<AuditLogResponse>> getByAction(
            @PathVariable AuditAction action) {
        return ResponseEntity.ok(auditLogService.getByAction(action));
    }
}