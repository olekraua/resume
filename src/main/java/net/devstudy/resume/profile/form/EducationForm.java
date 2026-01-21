package net.devstudy.resume.profile.form;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import net.devstudy.resume.profile.entity.Education;

@Getter
@Setter
public class EducationForm {
    @Valid
    @NotEmpty
    private List<Education> items;
}
