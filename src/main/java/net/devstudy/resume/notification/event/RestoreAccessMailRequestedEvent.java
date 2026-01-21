package net.devstudy.resume.notification.event;

public record RestoreAccessMailRequestedEvent(String email, String firstName, String link) {
}
