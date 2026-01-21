package net.devstudy.resume.profile.repository.storage;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import net.devstudy.resume.profile.entity.Practic;

public interface PracticRepository extends JpaRepository<Practic, Long> {
    List<Practic> findByProfileIdOrderByFinishDateDesc(Long profileId);

    void deleteByProfileId(Long profileId);
}
