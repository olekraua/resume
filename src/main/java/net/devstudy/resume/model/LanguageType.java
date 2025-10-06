package net.devstudy.resume.model;

import java.beans.PropertyEditorSupport;

import net.devstudy.resume.util.DataUtil;

/**
 * 
 * @author devstudy
 * @see http://devstudy.net
 */
public enum LanguageType {

    ALL,

    SPOKEN,

    WRITING;

    public String getCaption() {
        return DataUtil.capitalizeName(name().toLowerCase());
    }

    public String getDbValue() {
        return name();
    }

    public LanguageType getReverseType() {
        return switch (this) {
            case SPOKEN -> WRITING;
            case WRITING -> SPOKEN;
            default -> throw new IllegalArgumentException(this + " does not have reverse type");
        };
    }

    public static PropertyEditorSupport getPropertyEditor() {
        return new PropertyEditorSupport() {
            @Override
            public void setAsText(String dbValue) throws IllegalArgumentException {
                setValue(LanguageType.valueOf(dbValue.toUpperCase()));
            }
        };
    }
}