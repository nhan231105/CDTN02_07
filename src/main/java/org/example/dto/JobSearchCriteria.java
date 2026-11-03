package org.example.dto;

import java.math.BigDecimal;

public record JobSearchCriteria(String keyword, String category, String location, BigDecimal minSalary, BigDecimal maxSalary,
        String workSchedule, String jobType, String payType, String shift, Boolean noExperience, Boolean studentFriendly,
        Integer maxHoursPerWeek, String sortBy) {
    public static JobSearchCriteria basic(String keyword, String category, String location, BigDecimal minSalary,
            BigDecimal maxSalary, String workSchedule) {
        return new JobSearchCriteria(keyword, category, location, minSalary, maxSalary, workSchedule, null, null, null, null, null, null, null);
    }
}
