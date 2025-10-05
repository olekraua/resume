package net.devstudy.resume.domain;

import java.io.Serializable;

import net.devstudy.resume.annotation.EnableFormErrorConversion;
import net.devstudy.resume.annotation.constraints.EnglishLanguage;
import net.devstudy.resume.annotation.constraints.FirstFieldLessThanSecond;
import net.devstudy.resume.util.DataUtil;

@FirstFieldLessThanSecond(first = "beginYear", second = "finishYear")
@EnableFormErrorConversion(formName="educationForm",
        fieldReference="finishYear",
        validationAnnotationClass=FirstFieldLessThanSecond.class)
public class Education implements Serializable, ProfileCollectionField, Comparable<Education> {
    private static final long serialVersionUID = 1L;

    // @SafeHtml — прибрано: у Hibernate Validator 7 більше немає
    @EnglishLanguage(withSpecialSymbols = false)
    private String faculty;

    // @SafeHtml — прибрано
    @EnglishLanguage(withSpecialSymbols = false)
    private String summary;

    // @SafeHtml — прибрано
    @EnglishLanguage(withSpecialSymbols = false)
    private String university;

    private Integer beginYear;
    private Integer finishYear;

    public Education() {
        // required by frameworks (Jackson/Spring Data) for instantiation
    }

    public String getFaculty() { return this.faculty; }
    public void setFaculty(String faculty) { this.faculty = faculty; }

    public String getSummary() { return this.summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getUniversity() { return university; }
    public void setUniversity(String university) { this.university = university; }

    public Integer getBeginYear() { return beginYear; }
    public void setBeginYear(Integer beginYear) { this.beginYear = beginYear; }

    public Integer getFinishYear() { return finishYear; }
    public void setFinishYear(Integer finishYear) { this.finishYear = finishYear; }

    public boolean isFinish() { return finishYear != null; }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1; // super.hashCode() від Object нічого не додає
        result = prime * result + ((beginYear == null) ? 0 : beginYear.hashCode());
        result = prime * result + ((faculty == null) ? 0 : faculty.hashCode());
        result = prime * result + ((finishYear == null) ? 0 : finishYear.hashCode());
        result = prime * result + ((summary == null) ? 0 : summary.hashCode());
        result = prime * result + ((university == null) ? 0 : university.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
//      if (!super.equals(obj)) return false; // Object.equals() = reference equality; зайве
        if (!(obj instanceof Education)) return false;
        Education other = (Education) obj;
        if (beginYear == null ? other.beginYear != null : !beginYear.equals(other.beginYear)) return false;
        if (faculty   == null ? other.faculty   != null : !faculty.equals(other.faculty))     return false;
        if (finishYear== null ? other.finishYear!= null : !finishYear.equals(other.finishYear))return false;
        if (summary   == null ? other.summary   != null : !summary.equals(other.summary))     return false;
        return !(university == null ? other.university != null : !university.equals(other.university));
    }

    @Override
    public int compareTo(Education o) {
        int res = DataUtil.compareByFields(o.getFinishYear(), getFinishYear(), true);
        return (res == 0) ? DataUtil.compareByFields(o.getBeginYear(), getBeginYear(), true) : res;
    }
}
