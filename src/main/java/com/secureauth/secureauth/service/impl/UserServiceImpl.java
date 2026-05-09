package com.secureauth.secureauth.service.impl;

import com.secureauth.secureauth.domain.entity.User;
import com.secureauth.secureauth.domain.enums.AuditAction;
import com.secureauth.secureauth.domain.enums.Role;
import com.secureauth.secureauth.domain.repository.UserRepository;
import com.secureauth.secureauth.dto.response.UserResponse;
import com.secureauth.secureauth.exception.ResourceNotFoundException;
import com.secureauth.secureauth.service.AuditLogService;
import com.secureauth.secureauth.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    @Override
    @Transactional(readOnly = true)
    public UserResponse getByUsername(String username) {
        return toResponse(findByUsername(username));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getById(Long id) {
        return toResponse(userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı", id)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAll() {
        return userRepository.findAll()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserResponse addRole(Long userId, Role role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı", userId));
        user.getRoles().add(role);
        User saved = userRepository.save(user);
        auditLogService.log(user.getUsername(), AuditAction.ROLE_CHANGE,
                "USER", null, null, "Role added: " + role, true);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public UserResponse removeRole(Long userId, Role role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı", userId));
        user.getRoles().remove(role);
        User saved = userRepository.save(user);
        auditLogService.log(user.getUsername(), AuditAction.ROLE_CHANGE,
                "USER", null, null, "Role removed: " + role, true);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public UserResponse lockAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı", userId));
        user.setLockedUntil(LocalDateTime.now().plusYears(100));
        user.setActive(false);
        User saved = userRepository.save(user);
        auditLogService.log(user.getUsername(), AuditAction.ACCOUNT_LOCK,
                "USER", null, null, "Account locked by admin", true);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public UserResponse unlockAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı", userId));
        user.setLockedUntil(null);
        user.setFailedLoginAttempts(0);
        user.setActive(true);
        User saved = userRepository.save(user);
        auditLogService.log(user.getUsername(), AuditAction.ACCOUNT_UNLOCK,
                "USER", null, null, "Account unlocked by admin", true);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı", userId));
        user.setDeleted(true);
        user.setActive(false);
        userRepository.save(user);
    }

    private User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Kullanıcı bulunamadı: " + username));
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .roles(user.getRoles())
                .active(user.getActive())
                .emailVerified(user.getEmailVerified())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .build();
    }
}