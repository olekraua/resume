package net.devstudy.resume.repository.storage;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import net.devstudy.resume.domain.Profile;

public interface ProfileRepository extends JpaRepository<Profile, Long> {

    Optional<Profile> findByUid(String uid);

    @Query("""
            select profile from Profile profile
            where lower(profile.firstName) like lower(concat('%', :searchText, '%'))
            or lower(profile.lastName) like lower(concat('%', :searchText, '%'))
                           """)

    Page<Profile> search(@Param("searchText") String searchText, Pageable pageable);

    @Query("select profile from Profile profile where profile.completed = true")
    Page<Profile> findCompleted(Pageable pageable);
}