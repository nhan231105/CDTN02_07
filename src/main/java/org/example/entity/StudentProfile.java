package org.example.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "student_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentProfile {

    @Id
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(length = 255)
    private String avatarRef;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(length = 255)
    private String cvFileRef;

    @Column(length = 255)
    private String cvOriginalName;

    @Column(length = 150)
    private String school;

    @Column(length = 150)
    private String major;

    private Integer studyYear;

    @Column(length = 500)
    private String skills;

    // Comma separated JobShift names the student can work
    @Column(length = 100)
    private String availableShifts;

    @Column(length = 100)
    private String preferredArea;

    // Sinh viên chủ động bật thì mới xuất hiện trong kho ứng viên
    @Column(nullable = false)
    private boolean openToWork;

    private Double lat;

    private Double lng;

    // Chế độ mùa thi: bật để tạm ẩn/giảm ưu tiên gợi ý việc làm mới
    @Column(nullable = false)
    private boolean examMode;

    private java.time.LocalDate examStartDate;

    private java.time.LocalDate examEndDate;
}
