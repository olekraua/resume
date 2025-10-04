package net.devstudy.resume.domain;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

import net.devstudy.resume.model.AbstractModel;

public abstract class AbstractDocument<T> extends AbstractModel implements Serializable {
    @Serial
    private static final long serialVersionUID = 8982713310863621560L;

    public abstract T getId();

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        AbstractDocument<?> other = (AbstractDocument<?>) obj;
        // Якщо не хочеш вважати сутності з null-id рівними — раскоментуй наступний рядок
        // if (getId() == null || other.getId() == null) return false;
        return Objects.equals(getId(), other.getId());
    }

    @Override
    public String toString() {
        return "%s[id=%s]".formatted(getClass().getSimpleName(), getId());
    }
}

