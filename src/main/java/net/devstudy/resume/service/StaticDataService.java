package net.devstudy.resume.service;

import java.util.List;
import java.util.Map;

import net.devstudy.resume.entity.Hobby;
import net.devstudy.resume.entity.SkillCategory;
import net.devstudy.resume.model.LanguageLevel;
import net.devstudy.resume.model.LanguageType;

public interface StaticDataService {
    List<Integer> findPracticsYears();

    List<Integer> findCoursesYears();

    List<Integer> findEducationYears();

    Map<Integer, String> findMonthMap();

    List<SkillCategory> findSkillCategories();

    List<LanguageType> findAllLanguageTypes();

    List<LanguageLevel> findAllLanguageLevels();

    List<Hobby> findAllHobbies();
}
