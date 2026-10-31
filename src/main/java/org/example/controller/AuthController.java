package org.example.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.common.ApiResponse;
import org.example.entity.UserRole;
import org.example.entity.UserStatus;
import org.example.service.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/auth")
@AllArgsConstructor
@Slf4j
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(userService.register(request), "Đăng ký thành công"));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success(userService.login(request), "Đăng nhập thành công"));
    }

    @GetMapping("/admin/search")
    public ResponseEntity<ApiResponse<List<UserResponse>>> searchUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status) {
        log.info("Received admin search request - keyword: {}, role: {}, status: {}", 
                 keyword, role, status);

        try {
            List<UserResponse> result;

            // If keyword is provided, search by keyword
            if (keyword != null && !keyword.trim().isEmpty()) {
                result = userService.searchUsers(keyword);
                log.info("Found {} users matching keyword: {}", result.size(), keyword);
            }
            // If role is provided, filter by role
            else if (role != null && !role.trim().isEmpty()) {
                try {
                    UserRole userRole = UserRole.valueOf(role.toUpperCase());
                    result = userService.getUsersByRole(userRole);
                    log.info("Found {} users with role: {}", result.size(), role);
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid role provided: {}", role);
                    return ResponseEntity
                            .status(HttpStatus.BAD_REQUEST)
                            .body(ApiResponse.error("Invalid role. Must be STUDENT, EMPLOYER, or ADMIN"));
                }
            }
            // If status is provided, filter by status
            else if (status != null && !status.trim().isEmpty()) {
                try {
                    UserStatus userStatus = UserStatus.valueOf(status.toUpperCase());
                    result = userService.getUsersByStatus(userStatus);
                    log.info("Found {} users with status: {}", result.size(), status);
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid status provided: {}", status);
                    return ResponseEntity
                            .status(HttpStatus.BAD_REQUEST)
                            .body(ApiResponse.error("Invalid status. Must be ACTIVE or LOCKED"));
                }
            }
            // If no filter, return empty list
            else {
                result = List.of();
                log.info("No search parameters provided");
            }

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(ApiResponse.success(result, "Tim kiem thanh cong. Co " + result.size() + " ket qua"));
        } catch (Exception e) {
            log.error("Admin search failed with unexpected error", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Co loi xay ra trong qua trinh tim kiem"));
        }
    }

    @GetMapping("/admin/stats")
    public ResponseEntity<ApiResponse<AdminStatsResponse>> getAdminStats() {
        log.info("Received admin stats request");

        try {
            long totalStudents = userService.countByRole(UserRole.STUDENT);
            long totalEmployers = userService.countByRole(UserRole.EMPLOYER);
            long activeUsers = userService.countByStatus(UserStatus.ACTIVE);
            long lockedUsers = userService.countByStatus(UserStatus.LOCKED);

            AdminStatsResponse stats = new AdminStatsResponse();
            stats.setTotalStudents(totalStudents);
            stats.setTotalEmployers(totalEmployers);
            stats.setActiveUsers(activeUsers);
            stats.setLockedUsers(lockedUsers);
            stats.setTotalUsers(activeUsers + lockedUsers);

            log.info("Admin stats retrieved - Students: {}, Employers: {}", 
                     totalStudents, totalEmployers);

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(ApiResponse.success(stats, "Thong ke thanh cong"));
        } catch (Exception e) {
            log.error("Admin stats retrieval failed", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Co loi xay ra trong qua trinh lay thong ke"));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Void>> health() {
        log.debug("Health check endpoint called");
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Auth service is running"));
    }
}
