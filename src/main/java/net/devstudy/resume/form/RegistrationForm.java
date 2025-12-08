package net.devstudy.resume.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegistrationForm {

    @NotBlank
    @Size(min = 3, max = 50)
    @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "UID може містити лише літери, цифри, '-' або '_'")
    private String uid;

    @NotBlank
    @Size(max = 50)
    private String firstName;

    @NotBlank
    @Size(max = 50)
    private String lastName;

    @NotBlank
    @Size(min = 6, max = 100)
    private String password;
}
