package net.devstudy.resume.profile.repository.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import net.devstudy.resume.profile.entity.Course;
import net.devstudy.resume.profile.entity.Profile;
import net.devstudy.resume.testcontainers.PostgresIntegrationTest;

@Transactional
class CourseRepositoryIntegrationTest extends PostgresIntegrationTest {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Test
    void findByProfileIdOrdersByFinishDateDesc() {
        Profile first = createProfile("course-user-one");
        Profile second = createProfile("course-user-two");

        Course older = createCourse(first, "Old", LocalDate.of(2022, 1, 1));
        Course newer = createCourse(first, "New", LocalDate.of(2024, 5, 1));
        createCourse(second, "Other", LocalDate.of(2023, 3, 1));

        List<Course> result = courseRepository.findByProfileIdOrderByFinishDateDesc(first.getId());

        assertEquals(2, result.size());
        assertEquals(newer.getId(), result.get(0).getId());
        assertEquals(older.getId(), result.get(1).getId());
    }

    @Test
    void deleteByProfileIdRemovesOnlyMatchingCourses() {
        Profile first = createProfile("course-user-three");
        Profile second = createProfile("course-user-four");

        createCourse(first, "One", LocalDate.of(2021, 1, 1));
        createCourse(first, "Two", LocalDate.of(2022, 2, 1));
        createCourse(second, "Three", LocalDate.of(2023, 3, 1));

        courseRepository.deleteByProfileId(first.getId());
        courseRepository.flush();

        assertEquals(0, courseRepository.findByProfileIdOrderByFinishDateDesc(first.getId()).size());
        assertEquals(1, courseRepository.findByProfileIdOrderByFinishDateDesc(second.getId()).size());
        assertEquals(1, courseRepository.count());
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

    private Course createCourse(Profile profile, String name, LocalDate finishDate) {
        Course course = new Course();
        course.setProfile(profile);
        course.setName(name);
        course.setSchool("School " + name);
        course.setFinishDate(finishDate);
        return courseRepository.saveAndFlush(course);
    }
}
