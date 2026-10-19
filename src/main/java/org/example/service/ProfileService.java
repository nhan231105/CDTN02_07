package org.example.service;

import org.example.dto.StudentProfileDTO;
import org.example.dto.EmployerProfileDTO;
import org.example.dto.UpdateStudentProfileRequest;
import org.example.dto.UpdateEmployerProfileRequest;

public interface ProfileService {
    
    StudentProfileDTO getStudentProfile(Long studentId);
    
    StudentProfileDTO updateStudentProfile(Long studentId, UpdateStudentProfileRequest request);
    
    EmployerProfileDTO getEmployerProfile(Long employerId);
    
    EmployerProfileDTO updateEmployerProfile(Long employerId, UpdateEmployerProfileRequest request);
}
