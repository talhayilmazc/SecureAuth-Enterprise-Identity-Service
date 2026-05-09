package com.secureauth.secureauth.controller;

import com.secureauth.secureauth.domain.enums.Role;
import com.secureauth.secureauth.dto.response.UserResponse;
import com.secureauth.secureauth.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Kullanıcı yönetimi")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Mevcut kullanıcı bilgileri")
    public ResponseEntity<UserResponse> getCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userService.getByUsername(userDetails.getUsername()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Kullanıcı getir")
    public ResponseEntity<UserResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Tüm kullanıcıları listele")
    public ResponseEntity<List<UserResponse>> getAll() {
        return ResponseEntity.ok(userService.getAll());
    }

    @PostMapping("/{id}/roles/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Rol ekle")
    public ResponseEntity<UserResponse> addRole(
            @PathVariable Long id,
            @PathVariable Role role) {
        return ResponseEntity.ok(userService.addRole(id, role));
    }

    @DeleteMapping("/{id}/roles/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Rol kaldır")
    public ResponseEntity<UserResponse> removeRole(
            @PathVariable Long id,
            @PathVariable Role role) {
        return ResponseEntity.ok(userService.removeRole(id, role));
    }

    @PostMapping("/{id}/lock")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Hesap kilitle")
    public ResponseEntity<UserResponse> lockAccount(@PathVariable Long id) {
        return ResponseEntity.ok(userService.lockAccount(id));
    }

    @PostMapping("/{id}/unlock")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Hesap kilidini kaldır")
    public ResponseEntity<UserResponse> unlockAccount(@PathVariable Long id) {
        return ResponseEntity.ok(userService.unlockAccount(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Kullanıcı sil")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}