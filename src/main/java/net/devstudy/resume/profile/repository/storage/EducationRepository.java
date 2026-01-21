package net.devstudy.resume.profile.repository.storage;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import net.devstudy.resume.profile.entity.Education;

public interface EducationRepository extends JpaRepository<Education, Long> {
    List<Education> findByProfileIdOrderByFinishYearDescBeginYearDescIdDesc(Long profileId);

    void deleteByProfileId(Long profileId);
}
