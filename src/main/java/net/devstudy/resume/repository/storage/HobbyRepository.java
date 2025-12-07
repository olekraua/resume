package net.devstudy.resume.repository.storage;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import net.devstudy.resume.entity.Hobby;

public interface HobbyRepository extends JpaRepository<Hobby, Long> {
    List<Hobby> findByProfileIdOrderByNameAsc(Long profileId);

    void deleteByProfileId(Long profileId);
}
