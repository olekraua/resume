package net.devstudy.resume.model;

import jakarta.annotation.Nonnull;

/**
 * Поточний автентифікований профіль користувача.
 *
 * @author devstudy
 * @see https://devstudy.net
 */
public interface CurrentProfile {

    /** Унікальний ідентифікатор профілю (БД). */
    @Nonnull
    String getId();

    /** Публічний UID профілю (для URL тощо). */
    @Nonnull
    String getUid();
}
