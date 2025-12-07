package net.devstudy.resume.form;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import net.devstudy.resume.entity.Language;

@Getter
@Setter
public class LanguageForm {
    @Valid
    @NotEmpty
    private List<Language> items;
}
