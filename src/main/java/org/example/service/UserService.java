package org.example.service;

import org.example.entity.User;
import org.example.entity.UserRole;
import org.example.entity.UserStatus;

import java.util.List;

public interface UserService {

    RegisterResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    UserResponse getUserById(Long id);

    UserResponse findByEmail(String email);

    UserResponse findByPhone(String phone);

    List<UserResponse> searchUsers(String keyword);

    List<UserResponse> getUsersByRole(UserRole role);

    List<UserResponse> getUsersByStatus(UserStatus status);

    long countByRole(UserRole role);

    long countByStatus(UserStatus status);
}
