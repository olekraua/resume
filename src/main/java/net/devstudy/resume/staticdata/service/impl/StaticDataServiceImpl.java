package net.devstudy.resume.staticdata.service.impl;

import java.time.Year;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import net.devstudy.resume.staticdata.entity.Hobby;
import net.devstudy.resume.staticdata.entity.SkillCategory;
import net.devstudy.resume.shared.model.LanguageLevel;
import net.devstudy.resume.shared.model.LanguageType;
import net.devstudy.resume.staticdata.repository.storage.HobbyRepository;
import net.devstudy.resume.staticdata.repository.storage.SkillCategoryRepository;
import net.devstudy.resume.staticdata.service.StaticDataService;

@Service
@RequiredArgsConstructor
public class StaticDataServiceImpl implements StaticDataService {

    private final SkillCategoryRepository skillCategoryRepository;
    private final HobbyRepository hobbyRepository;

    @Override
    public List<Integer> findPracticsYears() {
        return generateYears(Year.now().getValue(), 1970);
    }

    @Override
    public List<Integer> findCoursesYears() {
        return generateYears(Year.now().getValue(), 1970);
    }

    @Override
    public List<Integer> findEducationYears() {
        return generateYears(Year.now().getValue(), 1960);
    }

    @Override
    public Map<Integer, String> findMonthMap() {
        Map<Integer, String> months = new LinkedHashMap<>();
        for (int m = 1; m <= 12; m++) {
            String name = java.time.Month.of(m).getDisplayName(TextStyle.FULL, Locale.getDefault());
            months.put(m, name);
        }
        return months;
    }

    @Override
    @Cacheable("skillCategories")
    public List<SkillCategory> findSkillCategories() {
        List<SkillCategory> categories = skillCategoryRepository.findAll(Sort.by("category"));
        if (categories.isEmpty()) {
            throw new IllegalStateException("No skill categories found in storage");
        }
        return categories;
    }

    @Override
    public List<LanguageType> findAllLanguageTypes() {
        return List.of(LanguageType.values());
    }

    @Override
    public List<LanguageLevel> findAllLanguageLevels() {
        return List.of(LanguageLevel.values());
    }

    @Override
    public List<Hobby> findAllHobbies() {
        return hobbyRepository.findAll(Sort.by("name"));
    }

    @Override
    public List<Hobby> findAllHobbiesWithSelected(List<Long> selectedIds) {
        List<Hobby> all = findAllHobbies();
        if (selectedIds != null && !selectedIds.isEmpty()) {
            all.forEach(h -> h.setSelected(selectedIds.contains(h.getId())));
        }
        return all;
    }

    private List<Integer> generateYears(int from, int toInclusive) {
        List<Integer> years = new ArrayList<>();
        for (int y = from; y >= toInclusive; y--) {
            years.add(y);
        }
        return years;
    }
}
