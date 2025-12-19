package net.devstudy.resume.event;

public record RestoreAccessMailRequestedEvent(String email, String firstName, String link) {
}
