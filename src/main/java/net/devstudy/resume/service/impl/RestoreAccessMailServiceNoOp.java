package net.devstudy.resume.service.impl;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import net.devstudy.resume.service.RestoreAccessMailService;

@Service
@ConditionalOnProperty(prefix = "app.restore.mail", name = "enabled", havingValue = "false", matchIfMissing = true)
public class RestoreAccessMailServiceNoOp implements RestoreAccessMailService {

    @Override
    public void sendRestoreLink(String email, String firstName, String link) {
        // No-op fallback when mail is disabled.
    }
}
