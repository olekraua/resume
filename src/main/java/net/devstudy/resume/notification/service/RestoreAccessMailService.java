package net.devstudy.resume.notification.service;

public interface RestoreAccessMailService {

    void sendRestoreLink(String email, String firstName, String link);
}
