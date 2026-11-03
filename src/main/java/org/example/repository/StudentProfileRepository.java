package org.example.repository;

import org.example.entity.StudentProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentProfileRepository extends JpaRepository<StudentProfile, Long> {
    String TALENT_FILTER = " from StudentProfile p join p.user u where p.openToWork = true"
            + " and u.status = org.example.entity.UserStatus.ACTIVE"
            + " and coalesce(p.availableShifts,'') like concat('%',:shift,'%')"
            + " and lower(coalesce(p.preferredArea,'')) like lower(concat('%',:area,'%'))"
            + " and lower(concat(coalesce(p.skills,''),' ',coalesce(p.major,''),' ',coalesce(p.school,''),' ',u.fullName))"
            + " like lower(concat('%',:keyword,'%'))";

    @Query(value = "select p" + TALENT_FILTER + " order by p.userId desc", countQuery = "select count(p)" + TALENT_FILTER)
    Page<StudentProfile> searchTalent(@Param("keyword") String keyword, @Param("shift") String shift,
                                      @Param("area") String area, Pageable pageable);

    @Query("select p.availableShifts from StudentProfile p join p.user u where p.openToWork = true and u.status = org.example.entity.UserStatus.ACTIVE")
    java.util.List<String> openToWorkShifts();
}
