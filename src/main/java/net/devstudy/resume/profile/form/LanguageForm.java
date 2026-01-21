package net.devstudy.resume.profile.form;

import java.util.List;

import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;
import net.devstudy.resume.profile.entity.Language;

@Getter
@Setter
public class LanguageForm {
    @Valid
    private List<Language> items;
}
