package net.devstudy.resume.repository.storage;

import org.springframework.data.jpa.repository.JpaRepository;

import net.devstudy.resume.entity.Hobby;

public interface HobbyRepository extends JpaRepository<Hobby, Long> {
}
