package net.devstudy.resume.form;

import java.util.List;

import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;
import net.devstudy.resume.entity.Language;

@Getter
@Setter
public class LanguageForm {
    @Valid
    private List<Language> items;
}
