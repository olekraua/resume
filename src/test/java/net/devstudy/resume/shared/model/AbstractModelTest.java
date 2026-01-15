package net.devstudy.resume.shared.model;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class AbstractModelTest {

    @Test
    void toStringUsesReflection() {
        TestModel model = new TestModel(42, "sample");

        String value = model.toString();

        assertNotNull(value);
        assertTrue(value.contains("TestModel"));
        assertTrue(value.contains("number=42"));
        assertTrue(value.contains("text=sample"));
    }

    @SuppressWarnings("unused")
    private static final class TestModel extends AbstractModel {
        private final int number;
        private final String text;

        private TestModel(int number, String text) {
            this.number = number;
            this.text = text;
        }
    }
}
