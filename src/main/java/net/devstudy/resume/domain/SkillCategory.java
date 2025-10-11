package net.devstudy.resume.domain;

import org.springframework.data.annotation.Id;

/**
 * 
 * @author devstudy
 * @see http://devstudy.net
 */
public class SkillCategory extends AbstractDocument<Long> {
    private static final long serialVersionUID = -8959739023562086833L;
    public static final String ORDER_FIELD_NAME = "idCategory";
    @Id
    private Long id;

    private Short idCategory;

    @jakarta.validation.constraints.NotBlank
    private String category;

    public SkillCategory() {
        super();
    }

    public SkillCategory(String category) {
        super();
        this.category = category;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Short getIdCategory() {
        return idCategory;
    }

    public void setIdCategory(Short idCategory) {
        this.idCategory = idCategory;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof SkillCategory))
			return false;
		SkillCategory that = (SkillCategory) o;
		return id != null && id.equals(that.id);
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}
}