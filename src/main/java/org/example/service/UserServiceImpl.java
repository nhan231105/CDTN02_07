package org.example.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.entity.User;
import org.example.entity.UserRole;
import org.example.entity.UserStatus;
import org.example.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public RegisterResponse register(RegisterRequest request) {
        log.info("Registering new user with email: {}, phone: {}", request.getEmail(), request.getPhone());

        // Validate email uniqueness
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed: Email already exists - {}", request.getEmail());
            throw new EmailAlreadyExistsException("Email nay da duoc dang ki");
        }

        // Validate phone uniqueness
        if (userRepository.existsByPhone(request.getPhone())) {
            log.warn("Registration failed: Phone already exists - {}", request.getPhone());
            throw new PhoneAlreadyExistsException("So dien thoai nay da duoc dang ki");
        }

        // Convert role string to UserRole enum
        UserRole role;
        try {
            role = UserRole.valueOf(request.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            log.error("Invalid role provided: {}", request.getRole());
            throw new IllegalArgumentException("Invalid role. Must be STUDENT or EMPLOYER");
        }

        // Create new user
        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRole(role);
        user.setStatus(UserStatus.ACTIVE);

        // Hash password using BCrypt
        String hashedPassword = passwordEncoder.encode(request.getPassword());
        user.setPasswordHash(hashedPassword);

        // Save user to database
        User savedUser = userRepository.save(user);
        log.info("User registered successfully: ID={}, Email={}, Phone={}", 
                 savedUser.getId(), savedUser.getEmail(), savedUser.getPhone());

        // Return response
        RegisterResponse response = new RegisterResponse();
        response.setId(savedUser.getId());
        response.setFullName(savedUser.getFullName());
        response.setEmail(savedUser.getEmail());
        response.setPhone(savedUser.getPhone());
        response.setRole(savedUser.getRole().name());
        response.setMessage("Dang ki thanh cong");

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        log.info("Login attempt with identifier: {}", request.getIdentifier());

        // Find user by email or phone
        User user = userRepository.findByEmailOrPhone(request.getIdentifier())
                .orElseThrow(() -> {
                    log.warn("Login failed: User not found - {}", request.getIdentifier());
                    return new UserNotFoundException("Ten dang nhap hoac mat khau chua dung");
                });

        // Check if user is locked
        if (user.getStatus() == UserStatus.LOCKED) {
            log.warn("Login failed: User account locked - ID={}", user.getId());
            throw new RuntimeException("Tai khoan nay da bi khoa. Ly do: " + user.getLockReason());
        }

        // Verify password using BCrypt
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("Login failed: Invalid password - Email={}", user.getEmail());
            throw new UserNotFoundException("Ten dang nhap hoac mat khau chua dung");
        }

        log.info("User logged in successfully: ID={}, Email={}", user.getId(), user.getEmail());

        // Return login response
        LoginResponse response = new LoginResponse();
        response.setId(user.getId());
        response.setFullName(user.getFullName());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setRole(user.getRole().name());
        response.setStatus(user.getStatus().name());

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        log.debug("Fetching user by ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User not found: ID={}", id);
                    return new UserNotFoundException("Khong tim thay nguoi dung");
                });

        return mapToUserResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse findByEmail(String email) {
        log.debug("Fetching user by email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("User not found with email: {}", email);
                    return new UserNotFoundException("Khong tim thay nguoi dung");
                });

        return mapToUserResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse findByPhone(String phone) {
        log.debug("Fetching user by phone: {}", phone);

        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> {
                    log.warn("User not found with phone: {}", phone);
                    return new UserNotFoundException("Khong tim thay nguoi dung");
                });

        return mapToUserResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> searchUsers(String keyword) {
        log.info("Searching users with keyword: {}", keyword);

        List<User> users = userRepository.searchByKeyword(keyword);
        log.debug("Found {} users matching keyword: {}", users.size(), keyword);

        return users.stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getUsersByRole(UserRole role) {
        log.info("Fetching users by role: {}", role);

        List<User> users = userRepository.findByRole(role);
        log.debug("Found {} users with role: {}", users.size(), role);

        return users.stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getUsersByStatus(UserStatus status) {
        log.info("Fetching users by status: {}", status);

        List<User> users = userRepository.findByStatus(status);
        log.debug("Found {} users with status: {}", users.size(), status);

        return users.stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long countByRole(UserRole role) {
        log.debug("Counting users by role: {}", role);
        return userRepository.countByRole(role);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByStatus(UserStatus status) {
        log.debug("Counting users by status: {}", status);
        return userRepository.countByStatus(status);
    }

    private UserResponse mapToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setFullName(user.getFullName());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setRole(user.getRole().name());
        response.setStatus(user.getStatus().name());
        return response;
    }
}
