package net.devstudy.resume.service.impl;

import java.time.Duration;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import net.devstudy.resume.service.RestoreAccessMailService;

@Service
@ConditionalOnProperty(prefix = "app.restore.mail", name = "enabled", havingValue = "true")
public class RestoreAccessMailServiceImpl implements RestoreAccessMailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestoreAccessMailServiceImpl.class);

    private final JavaMailSender mailSender;
    private final String from;
    private final String subject;
    private final Duration tokenTtl;

    public RestoreAccessMailServiceImpl(JavaMailSender mailSender,
            @Value("${app.restore.mail.from:}") String from,
            @Value("${spring.mail.username:}") String username,
            @Value("${app.restore.mail.subject:Password reset}") String subject,
            @Value("${app.restore.token-ttl:PT1H}") Duration tokenTtl) {
        this.mailSender = mailSender;
        this.from = (from == null || from.isBlank()) ? username : from;
        this.subject = subject;
        this.tokenTtl = tokenTtl;
    }

    @Override
    public void sendRestoreLink(String email, String firstName, String link) {
        if (email == null || email.isBlank() || link == null || link.isBlank()) {
            return;
        }
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    "UTF-8");
            if (from != null && !from.isBlank()) {
                helper.setFrom(from);
            }
            helper.setTo(email);
            helper.setSubject(subject);
            helper.setText(buildPlainBody(firstName, link), buildHtmlBody(firstName, link));
            mailSender.send(message);
        } catch (MessagingException | MailException ex) {
            LOGGER.warn("Failed to send restore email to {}: {}", email, ex.getMessage());
        }
    }

    private String buildPlainBody(String firstName, String link) {
        StringBuilder body = new StringBuilder();
        body.append("Hello");
        if (firstName != null && !firstName.isBlank()) {
            body.append(' ').append(firstName.trim());
        }
        body.append(",\n\n");
        body.append("To reset your password, open this link:\n");
        body.append(link).append('\n');
        body.append("\nThis link expires in ").append(formatDuration(tokenTtl)).append('.');
        body.append("\nIf you did not request a reset, you can ignore this email.");
        return body.toString();
    }

    private String buildHtmlBody(String firstName, String link) {
        String safeName = escapeHtml(firstName == null ? "" : firstName.trim());
        String safeLink = escapeHtml(link);
        String ttl = escapeHtml(formatDuration(tokenTtl));

        StringBuilder html = new StringBuilder();
        html.append("<!doctype html><html><body>");
        html.append("<p>Hello");
        if (!safeName.isBlank()) {
            html.append(' ').append(safeName);
        }
        html.append(",</p>");
        html.append("<p>To reset your password, open this link:</p>");
        html.append("<p><a href=\"").append(safeLink).append("\">").append(safeLink).append("</a></p>");
        html.append("<p>This link expires in ").append(ttl).append(".</p>");
        html.append("<p>If you did not request a reset, you can ignore this email.</p>");
        html.append("</body></html>");
        return html.toString();
    }

    private String formatDuration(Duration duration) {
        if (duration == null) {
            return "a limited time";
        }
        long hours = duration.toHours();
        if (hours > 0) {
            return hours + " hour(s)";
        }
        long minutes = duration.toMinutes();
        if (minutes > 0) {
            return minutes + " minute(s)";
        }
        long seconds = duration.toSeconds();
        if (seconds > 0) {
            return seconds + " second(s)";
        }
        return "a limited time";
    }

    private String escapeHtml(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String escaped = value;
        escaped = escaped.replace("&", "&amp;");
        escaped = escaped.replace("<", "&lt;");
        escaped = escaped.replace(">", "&gt;");
        escaped = escaped.replace("\"", "&quot;");
        escaped = escaped.replace("'", "&#39;");
        return escaped;
    }
}
