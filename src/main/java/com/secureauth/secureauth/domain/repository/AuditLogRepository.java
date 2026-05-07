package com.secureauth.secureauth.domain.repository;

import com.secureauth.secureauth.domain.entity.AuditLog;
import com.secureauth.secureauth.domain.enums.AuditAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long>,
        JpaSpecificationExecutor<AuditLog> {

    List<AuditLog> findByUsernameOrderByCreatedAtDesc(String username);

    List<AuditLog> findByAction(AuditAction action);

    List<AuditLog> findByIpAddress(String ipAddress);
}