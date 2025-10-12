package net.devstudy.resume.repository.storage;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import net.devstudy.resume.domain.Profile;

public interface ProfileRepository extends JpaRepository<Profile, Long>{

    Optional<Profile> findByUid(String uid);
    Page<Profile> findAllByCompletedTrue(Pageable pageable);

    
}