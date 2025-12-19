package net.devstudy.resume.service;

public interface RestoreAccessMailService {

    void sendRestoreLink(String email, String firstName, String link);
}
