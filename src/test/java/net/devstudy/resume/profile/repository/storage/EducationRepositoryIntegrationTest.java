package net.devstudy.resume.profile.repository.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import net.devstudy.resume.profile.entity.Education;
import net.devstudy.resume.profile.entity.Profile;
import net.devstudy.resume.testcontainers.PostgresIntegrationTest;

@Transactional
class EducationRepositoryIntegrationTest extends PostgresIntegrationTest {

    @Autowired
    private EducationRepository educationRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Test
    void findByProfileIdOrdersByFinishYearBeginYearIdDesc() {
        Profile profile = createProfile("education-user-one");
        Profile other = createProfile("education-user-two");

        Education oldest = createEducation(profile, "Faculty 1", "Summary 1", "University 1", 2018, 2022);
        Education newest = createEducation(profile, "Faculty 2", "Summary 2", "University 2", 2017, 2023);
        Education mid = createEducation(profile, "Faculty 3", "Summary 3", "University 3", 2020, 2022);
        Education tieOlder = createEducation(profile, "Faculty 4", "Summary 4", "University 4", 2015, 2021);
        Education tieNewer = createEducation(profile, "Faculty 5", "Summary 5", "University 5", 2015, 2021);
        createEducation(other, "Faculty 6", "Summary 6", "University 6", 2019, 2024);

        List<Education> result = educationRepository
                .findByProfileIdOrderByFinishYearDescBeginYearDescIdDesc(profile.getId());

        assertEquals(5, result.size());
        assertEquals(newest.getId(), result.get(0).getId());
        assertEquals(mid.getId(), result.get(1).getId());
        assertEquals(oldest.getId(), result.get(2).getId());
        assertEquals(tieNewer.getId(), result.get(3).getId());
        assertEquals(tieOlder.getId(), result.get(4).getId());
    }

    @Test
    void deleteByProfileIdRemovesOnlyMatchingEducations() {
        Profile first = createProfile("education-user-three");
        Profile second = createProfile("education-user-four");

        createEducation(first, "Faculty 1", "Summary 1", "University 1", 2010, 2014);
        createEducation(first, "Faculty 2", "Summary 2", "University 2", 2011, 2015);
        createEducation(second, "Faculty 3", "Summary 3", "University 3", 2012, 2016);

        educationRepository.deleteByProfileId(first.getId());
        educationRepository.flush();

        assertEquals(0, educationRepository
                .findByProfileIdOrderByFinishYearDescBeginYearDescIdDesc(first.getId()).size());
        assertEquals(1, educationRepository
                .findByProfileIdOrderByFinishYearDescBeginYearDescIdDesc(second.getId()).size());
        assertEquals(1, educationRepository.count());
    }

    private Profile createProfile(String uid) {
        Profile profile = new Profile();
        profile.setUid(uid);
        profile.setFirstName("John");
        profile.setLastName("Doe");
        profile.setPassword("password");
        profile.setCompleted(true);
        return profileRepository.saveAndFlush(profile);
    }

    private Education createEducation(Profile profile, String faculty, String summary, String university,
            int beginYear, int finishYear) {
        Education education = new Education();
        education.setProfile(profile);
        education.setFaculty(faculty);
        education.setSummary(summary);
        education.setUniversity(university);
        education.setBeginYear(beginYear);
        education.setFinishYear(finishYear);
        return educationRepository.saveAndFlush(education);
    }
}
