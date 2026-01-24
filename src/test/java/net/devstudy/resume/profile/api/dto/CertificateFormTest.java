package net.devstudy.resume.profile.api.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;

import org.junit.jupiter.api.Test;

import net.devstudy.resume.profile.api.model.Certificate;

class CertificateFormTest {

    @Test
    void itemsDefaultsToNull() {
        CertificateForm form = new CertificateForm();

        assertNull(form.getItems());
    }

    @Test
    void itemsCanBeSetAndRead() {
        CertificateForm form = new CertificateForm();
        List<Certificate> items = List.of(new Certificate());

        form.setItems(items);

        assertEquals(items, form.getItems());
    }
}
