package net.devstudy.resume.notification.mail;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.Test;

import net.devstudy.resume.notification.event.RestoreAccessMailRequestedEvent;
import net.devstudy.resume.notification.service.RestoreAccessMailService;

class RestoreAccessMailListenerTest {

    private final RestoreAccessMailService mailService = mock(RestoreAccessMailService.class);
    private final RestoreAccessMailListener listener = new RestoreAccessMailListener(mailService);

    @Test
    void ignoresNullEvent() {
        listener.onRestoreAccessMailRequested(null);

        verifyNoInteractions(mailService);
    }

    @Test
    void sendsRestoreLinkWhenEventPresent() {
        RestoreAccessMailRequestedEvent event = new RestoreAccessMailRequestedEvent(
                "user@example.com",
                "Oleh",
                "http://localhost/restore/token");

        listener.onRestoreAccessMailRequested(event);

        verify(mailService).sendRestoreLink("user@example.com", "Oleh", "http://localhost/restore/token");
    }
}
