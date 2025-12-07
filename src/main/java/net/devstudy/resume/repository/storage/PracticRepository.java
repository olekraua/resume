package net.devstudy.resume.repository.storage;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import net.devstudy.resume.entity.Practic;

public interface PracticRepository extends JpaRepository<Practic, Long> {
    List<Practic> findByProfileIdOrderByFinishDateDesc(Long profileId);

    void deleteByProfileId(Long profileId);
}
