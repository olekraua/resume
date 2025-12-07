package net.devstudy.resume.service.impl;

import java.time.Year;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import net.devstudy.resume.entity.Hobby;
import net.devstudy.resume.entity.SkillCategory;
import net.devstudy.resume.model.LanguageLevel;
import net.devstudy.resume.model.LanguageType;
import net.devstudy.resume.repository.storage.HobbyRepository;
import net.devstudy.resume.repository.storage.SkillCategoryRepository;
import net.devstudy.resume.service.StaticDataService;

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
    public List<SkillCategory> findSkillCategories() {
        return skillCategoryRepository.findAll();
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
        return hobbyRepository.findAll();
    }

    @Override
    public List<Hobby> findAllHobbiesWithSelected(List<Long> selectedIds) {
        List<Hobby> all = hobbyRepository.findAll();
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
