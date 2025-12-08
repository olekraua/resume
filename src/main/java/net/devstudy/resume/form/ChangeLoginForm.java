package net.devstudy.resume.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangeLoginForm {

    @NotBlank
    @Size(min = 3, max = 50)
    @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "UID може містити лише літери, цифри, '-' або '_'")
    private String newUid;
}
