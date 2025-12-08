package net.devstudy.resume.form;

import java.sql.Date;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import net.devstudy.resume.annotation.constraints.Adulthood;

@Getter
@Setter
public class ProfileMainForm {

    private MultipartFile profilePhoto;

    @Adulthood
    private Date birthDay;

    @NotBlank
    @Size(max = 100)
    private String country;

    @NotBlank
    @Size(max = 100)
    private String city;

    @Email
    @NotBlank
    @Size(max = 100)
    private String email;

    @NotBlank
    @Size(max = 20)
    private String phone;

    @NotBlank
    @Size(max = 255)
    private String objective;

    @NotBlank
    @Size(max = 2147483647)
    private String summary;

    @Size(max = 2147483647)
    private String info;
}
