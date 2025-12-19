package net.devstudy.resume.component;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public interface DataBuilder {

    @NonNull
    String buildProfileUid(@NonNull String firstName, @NonNull String lastName);

    @NonNull
    String buildRestoreAccessLink(@NonNull String appHost, @NonNull String token);

    @NonNull
    String rebuildUidWithRandomSuffix(@NonNull String baseUid, @NonNull String alphabet, int letterCount);

    @NonNull
    String buildCertificateName(@Nullable String fileName);
}
