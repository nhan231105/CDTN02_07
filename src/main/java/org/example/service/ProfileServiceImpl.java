package org.example.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.StudentProfileDTO;
import org.example.dto.EmployerProfileDTO;
import org.example.dto.UpdateStudentProfileRequest;
import org.example.dto.UpdateEmployerProfileRequest;
import org.example.entity.StudentProfile;
import org.example.entity.EmployerProfile;
import org.example.entity.User;
import org.example.repository.StudentProfileRepository;
import org.example.repository.EmployerProfileRepository;
import org.example.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Slf4j
public class ProfileServiceImpl implements ProfileService {

    private final StudentProfileRepository studentProfileRepository;
    private final EmployerProfileRepository employerProfileRepository;
    private final UserRepository userRepository;
    private final FileStorageService files;

    @Override
    public StudentProfileDTO getStudentProfile(Long studentId) {
        org.example.security.CurrentUser.requireId(studentId);
        org.example.security.CurrentUser.requireRole(org.example.entity.UserRole.STUDENT);
        StudentProfile profile = studentProfileRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Thong tin sinh vien khong ton tai"));

        return new StudentProfileDTO(
            profile.getUserId(),
            profile.getAvatarRef(),
            profile.getBio(),
            profile.getCvFileRef(),
            profile.getCvOriginalName()
        );
    }

    @Override
    @Transactional
    public StudentProfileDTO updateStudentProfile(Long studentId, UpdateStudentProfileRequest request) {
        org.example.security.CurrentUser.requireId(studentId);
        org.example.security.CurrentUser.requireRole(org.example.entity.UserRole.STUDENT);
        StudentProfile profile = studentProfileRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Thong tin sinh vien khong ton tai"));

        if (request.getAvatarRef() != null) {
            files.requireOwned(request.getAvatarRef(), studentId);
            profile.setAvatarRef(request.getAvatarRef());
        }
        if (request.getBio() != null) {
            profile.setBio(request.getBio());
        }
        if (request.getCvFileRef() != null) {
            var cv = files.requireOwnedCv(request.getCvFileRef(), studentId);
            profile.setCvOriginalName(cv.getOriginalName());
            profile.setCvFileRef(request.getCvFileRef());
        }

        StudentProfile updated = studentProfileRepository.save(profile);
        log.info("Student profile {} updated", studentId);

        return new StudentProfileDTO(
            updated.getUserId(),
            updated.getAvatarRef(),
            updated.getBio(),
            updated.getCvFileRef(),
            updated.getCvOriginalName()
        );
    }

    @Override
    public EmployerProfileDTO getEmployerProfile(Long employerId) {
        org.example.security.CurrentUser.requireId(employerId);
        org.example.security.CurrentUser.requireRole(org.example.entity.UserRole.EMPLOYER);
        EmployerProfile profile = employerProfileRepository.findById(employerId)
                .orElseThrow(() -> new RuntimeException("Thong tin nha tuyen dung khong ton tai"));

        return new EmployerProfileDTO(
            profile.getUserId(),
            profile.getOrgName(),
            profile.getAddress(),
            profile.getDescription(),
            profile.getLogoRef()
        );
    }

    @Override
    @Transactional
    public EmployerProfileDTO updateEmployerProfile(Long employerId, UpdateEmployerProfileRequest request) {
        org.example.security.CurrentUser.requireId(employerId);
        org.example.security.CurrentUser.requireRole(org.example.entity.UserRole.EMPLOYER);
        EmployerProfile profile = employerProfileRepository.findById(employerId)
                .orElseThrow(() -> new RuntimeException("Thong tin nha tuyen dung khong ton tai"));

        if (request.getOrgName() != null) {
            profile.setOrgName(request.getOrgName());
        }
        if (request.getAddress() != null) {
            profile.setAddress(request.getAddress());
        }
        if (request.getDescription() != null) {
            profile.setDescription(request.getDescription());
        }
        if (request.getLogoRef() != null) {
            profile.setLogoRef(request.getLogoRef());
        }

        EmployerProfile updated = employerProfileRepository.save(profile);
        log.info("Employer profile {} updated", employerId);

        return new EmployerProfileDTO(
            updated.getUserId(),
            updated.getOrgName(),
            updated.getAddress(),
            updated.getDescription(),
            updated.getLogoRef()
        );
    }
}
