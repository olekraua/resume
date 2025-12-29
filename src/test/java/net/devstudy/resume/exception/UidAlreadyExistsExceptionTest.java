package net.devstudy.resume.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class UidAlreadyExistsExceptionTest {

    @Test
    void exposesUidAndMessage() {
        UidAlreadyExistsException ex = new UidAlreadyExistsException("john-doe");

        assertEquals("john-doe", ex.getUid());
        assertEquals("Uid already exists: john-doe", ex.getMessage());
    }
}
