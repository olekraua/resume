package net.devstudy.resume.form;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;

import org.junit.jupiter.api.Test;

import net.devstudy.resume.entity.Practic;

class PracticFormTest {

    @Test
    void itemsDefaultsToNull() {
        PracticForm form = new PracticForm();

        assertNull(form.getItems());
    }

    @Test
    void itemsCanBeSetAndRead() {
        PracticForm form = new PracticForm();
        List<Practic> items = List.of(new Practic());

        form.setItems(items);

        assertEquals(items, form.getItems());
    }
}
