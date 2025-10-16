package net.devstudy.resume.repository.storage;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import net.devstudy.resume.entity.Profile;

public interface ProfileRepository extends JpaRepository<Profile, Long> {

    Optional<Profile> findByUid(String uid);

    @Query("select profile from Profile profile where profile.completed = true")
    Page<Profile> findCompleted(Pageable pageable);
}