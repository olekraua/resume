package net.devstudy.resume.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import net.devstudy.resume.annotation.constraints.EnglishLanguage;

/**
 * SignUp form model (Spring Boot 3 / Jakarta Validation)
 */
public class SignUpForm extends PasswordForm {

    @NotBlank
    @Size(max = 50)
    @EnglishLanguage(withNumbers = false, withSpecialSymbols = false)
    private String firstName;

    @NotBlank
    @Size(max = 50)
    @EnglishLanguage(withNumbers = false, withSpecialSymbols = false)
    private String lastName;

    // Optional: Wenn du Whitespace auto-trimmen willst, gib hier get*() mit trim() zurück
    public String getFirstName() {
        return firstName != null ? firstName.trim() : null;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public String getLastName() {
        return lastName != null ? lastName.trim() : null;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}

