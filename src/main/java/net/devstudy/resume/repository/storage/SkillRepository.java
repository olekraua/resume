package net.devstudy.resume.repository.storage;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import net.devstudy.resume.entity.Skill;

public interface SkillRepository extends JpaRepository<Skill, Long> {
    List<Skill> findByProfileIdOrderByCategoryAsc(Long profileId);

    void deleteByProfileId(Long profileId);
}
