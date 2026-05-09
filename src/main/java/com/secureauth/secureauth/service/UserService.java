package com.secureauth.secureauth.service;

import com.secureauth.secureauth.dto.response.UserResponse;
import com.secureauth.secureauth.domain.enums.Role;

import java.util.List;

public interface UserService {
    UserResponse getByUsername(String username);
    UserResponse getById(Long id);
    List<UserResponse> getAll();
    UserResponse addRole(Long userId, Role role);
    UserResponse removeRole(Long userId, Role role);
    UserResponse lockAccount(Long userId);
    UserResponse unlockAccount(Long userId);
    void deleteUser(Long userId);
}