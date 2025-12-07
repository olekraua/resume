package net.devstudy.resume.repository.storage;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import net.devstudy.resume.entity.Education;

public interface EducationRepository extends JpaRepository<Education, Long> {
    List<Education> findByProfileIdOrderByFinishYearDescBeginYearDescIdDesc(Long profileId);

    void deleteByProfileId(Long profileId);
}
