package net.devstudy.resume.form;

import java.util.List;

import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;
import net.devstudy.resume.entity.Certificate;

@Getter
@Setter
public class CertificateForm {
    @Valid
    private List<Certificate> items;
}
