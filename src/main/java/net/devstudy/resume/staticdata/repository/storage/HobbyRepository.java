package net.devstudy.resume.staticdata.repository.storage;

import org.springframework.data.jpa.repository.JpaRepository;

import net.devstudy.resume.staticdata.entity.Hobby;

public interface HobbyRepository extends JpaRepository<Hobby, Long> {
}
