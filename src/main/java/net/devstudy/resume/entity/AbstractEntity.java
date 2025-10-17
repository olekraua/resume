package net.devstudy.resume.entity;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.MappedSuperclass;
import net.devstudy.resume.model.AbstractModel;

@MappedSuperclass
public abstract class AbstractEntity<T> extends AbstractModel implements Serializable {

    private static final long serialVersionUID = 8982713310863621560L;

    public abstract T getId();

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof AbstractEntity<?> other))
            return false;
        return Objects.equals(getId(), other.getId());

    }

    @Override
    public String toString(){
        return String.format("%s[id=%s]",getClass().getSimpleName(), getId());
    }
}