package net.devstudy.resume.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RestoreAccessForm {

    @NotBlank
    @Size(max = 100)
    private String identifier;
}
