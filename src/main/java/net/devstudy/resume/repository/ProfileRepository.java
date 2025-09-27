package net.devstudy.resume.repository;

import net.devstudy.resume.domain.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
    Optional<Profile> findByUid(String uid);

    @Query("""
      select p from Profile p
      where lower(p.firstName) like lower(concat('%', :q, '%'))
         or lower(p.lastName)  like lower(concat('%', :q, '%'))
    """)
    Page<Profile> search(@Param("q") String q, Pageable pageable);
}
