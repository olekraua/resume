package net.devstudy.resume.shared.messaging;

public final class KafkaTopics {

    public static final String PROFILE_INDEXING = "profile.indexing";
    public static final String PROFILE_REMOVED = "profile.removed";
    public static final String PROFILE_PASSWORD_CHANGED = "profile.password.changed";
    public static final String PROFILE_MEDIA_CLEANUP = "profile.media.cleanup";
    public static final String AUTH_RESTORE_MAIL_REQUESTED = "auth.restore.mail.requested";

    private KafkaTopics() {
    }
}
