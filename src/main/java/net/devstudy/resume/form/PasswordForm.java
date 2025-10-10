package net.devstudy.resume.form;

import jakarta.validation.constraints.NotBlank;

import net.devstudy.resume.annotation.EnableFormErrorConversion;
import net.devstudy.resume.annotation.constraints.FieldMatch;
import net.devstudy.resume.annotation.constraints.PasswordStrength;

/**
 * Password form (Boot 3 / Spring 6 / Jakarta Validation).
 */
@FieldMatch(
    first = "password",
    second = "confirmPassword",
    message = "{password.match}" // -> messages.properties
)
@EnableFormErrorConversion(
    formName = "passwordForm",
    fieldReference = "confirmPassword",
    validationAnnotationClass = FieldMatch.class
)
public class PasswordForm {

    @NotBlank(message = "{password.required}")
    @PasswordStrength // deine benutzerdefinierte Prüfung
    private String password;

    @NotBlank(message = "{password.confirm.required}")
    private String confirmPassword;

    public PasswordForm() {}

    public PasswordForm(String password, String confirmPassword) {
        this.password = password;
        this.confirmPassword = confirmPassword;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}

