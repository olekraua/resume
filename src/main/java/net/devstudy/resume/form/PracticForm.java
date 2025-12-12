package net.devstudy.resume.form;

import java.util.List;

import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;
import net.devstudy.resume.dto.PracticDto;

@Getter
@Setter
public class PracticForm {
    @Valid
    private List<PracticDto> items;
}
