package net.devstudy.resume.form;

import java.util.List;

import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;
import net.devstudy.resume.entity.Practic;

@Getter
@Setter
public class PracticForm {
    @Valid
    private List<Practic> items;
}
