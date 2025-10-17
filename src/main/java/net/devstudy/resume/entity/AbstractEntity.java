package net.devstudy.resume.entity;

import java.io.Serializable;

import jakarta.persistence.MappedSuperclass;
import net.devstudy.resume.model.AbstractModel;

@MappedSuperclass
public abstract class AbstractEntity<T> extends AbstractModel implements Serializable {

    private static final long serialVersionUID = 8982713310863621560L;

    
}