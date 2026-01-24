package net.devstudy.resume.web.controller;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import net.devstudy.resume.shared.model.LanguageType;
import net.devstudy.resume.staticdata.api.model.Hobby;
import net.devstudy.resume.staticdata.api.model.SkillCategory;
import net.devstudy.resume.staticdata.api.service.StaticDataService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/static-data")
public class StaticDataApiController {

    private final StaticDataService staticDataService;
    private final MessageSource messageSource;

    @GetMapping
    public StaticDataResponse staticData() {
        List<SkillCategoryItem> skillCategories = staticDataService.findSkillCategories().stream()
                .filter(Objects::nonNull)
                .map(this::toSkillCategory)
                .toList();
        List<LanguageTypeItem> languageTypes = buildLanguageTypes();
        List<LanguageLevelItem> languageLevels = staticDataService.findAllLanguageLevels().stream()
                .filter(Objects::nonNull)
                .map(level -> new LanguageLevelItem(level.name(), level.getSliderIntValue()))
                .toList();
        List<HobbyItem> hobbies = staticDataService.findAllHobbies().stream()
                .filter(Objects::nonNull)
                .map(this::toHobby)
                .toList();
        List<MonthItem> months = staticDataService.findMonthMap().entrySet().stream()
                .map(entry -> new MonthItem(entry.getKey(), entry.getValue()))
                .toList();
        return new StaticDataResponse(
                skillCategories,
                languageTypes,
                languageLevels,
                hobbies,
                staticDataService.findPracticsYears(),
                staticDataService.findCoursesYears(),
                staticDataService.findEducationYears(),
                months
        );
    }

    private SkillCategoryItem toSkillCategory(SkillCategory category) {
        return new SkillCategoryItem(category.getId(), category.getCategory());
    }

    private HobbyItem toHobby(Hobby hobby) {
        return new HobbyItem(hobby.getId(), hobby.getName(), hobby.getCssClassName());
    }

    private List<LanguageTypeItem> buildLanguageTypes() {
        Locale locale = LocaleContextHolder.getLocale();
        return staticDataService.findAllLanguageTypes().stream()
                .filter(Objects::nonNull)
                .map(type -> toLanguageType(type, locale))
                .toList();
    }

    private LanguageTypeItem toLanguageType(LanguageType type, Locale locale) {
        String code = type.name();
        String label = messageSource.getMessage("language.type." + code, null, code, locale);
        if (label == null || label.isBlank()) {
            label = code;
        }
        return new LanguageTypeItem(code, label);
    }

    public record StaticDataResponse(
            List<SkillCategoryItem> skillCategories,
            List<LanguageTypeItem> languageTypes,
            List<LanguageLevelItem> languageLevels,
            List<HobbyItem> hobbies,
            List<Integer> practicYears,
            List<Integer> courseYears,
            List<Integer> educationYears,
            List<MonthItem> months
    ) {
    }

    public record SkillCategoryItem(
            Long id,
            String category
    ) {
    }

    public record LanguageTypeItem(
            String code,
            String label
    ) {
    }

    public record LanguageLevelItem(
            String code,
            int sliderValue
    ) {
    }

    public record HobbyItem(
            Long id,
            String name,
            String cssClassName
    ) {
    }

    public record MonthItem(
            int value,
            String label
    ) {
    }
}
