package net.devstudy.resume.domain;

import java.io.Serializable;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;

import org.hibernate.validator.constraints.URL;
import org.springframework.data.annotation.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;

import net.devstudy.resume.annotation.EnableFormErrorConversion;
import net.devstudy.resume.annotation.constraints.EnglishLanguage;
import net.devstudy.resume.annotation.constraints.FirstFieldLessThanSecond;
import net.devstudy.resume.util.DataUtil;

@FirstFieldLessThanSecond(first = "beginDate", second = "finishDate")
@EnableFormErrorConversion(formName = "practicForm", fieldReference = "finishDate", 
validationAnnotationClass = FirstFieldLessThanSecond.class)
public class Practic extends AbstractFinishDateDocument<Long>
        implements Serializable, ProfileCollectionField, Comparable<Practic> {

    private static final long serialVersionUID = 1L;

    // @SafeHtml видалено (його немає у HV 7/8). За потреби додай власну перевірку
    // або @Pattern.
    @EnglishLanguage(withSpecialSymbols = false)
    private String company;

    @JsonIgnore
    @EnglishLanguage
    @URL
    private String demo;

    @JsonIgnore
    @EnglishLanguage
    @URL
    private String src;

    @EnglishLanguage(withSpecialSymbols = false)
    private String position;

    @EnglishLanguage(withSpecialSymbols = false)
    private String responsibilities;

    @JsonIgnore
    private Date beginDate;

    @Transient
    @JsonIgnore
    private Integer beginDateMonth; // 1..12

    @Transient
    @JsonIgnore
    private Integer beginDateYear;

    public Practic() {
        // default ctor required by frameworks (Jackson/Spring Data)
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getDemo() {
        return demo;
    }

    public void setDemo(String demo) {
        this.demo = demo;
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getResponsibilities() {
        return responsibilities;
    }

    public void setResponsibilities(String responsibilities) {
        this.responsibilities = responsibilities;
    }

    public Date getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(Date beginDate) {
        this.beginDate = beginDate;
    }

    public Integer getBeginDateMonth() {
        if (beginDate == null)
            return null;
        return beginDate.toInstant().atZone(ZoneId.systemDefault()).getMonthValue();
    }

    public Integer getBeginDateYear() {
        if (beginDate == null)
            return null;
        return beginDate.toInstant().atZone(ZoneId.systemDefault()).getYear();
    }

    public void setBeginDateMonth(Integer beginDateMonth) {
        this.beginDateMonth = beginDateMonth;
        setupBeginDate();
    }

    public void setBeginDateYear(Integer beginDateYear) {
        this.beginDateYear = beginDateYear;
        setupBeginDate();
    }

    private void setupBeginDate() {
        if (beginDateYear != null && beginDateMonth != null) {
            YearMonth ym = YearMonth.of(beginDateYear, beginDateMonth);
            this.beginDate = Date.from(ym.atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
        } else {
            this.beginDate = null;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(beginDate, company, demo, getFinishDate(), position, responsibilities, src);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!super.equals(o))
            return false; // зберігаємо логіку базового класу
        if (!(o instanceof Practic other))
            return false;
        return Objects.equals(beginDate, other.beginDate)
                && Objects.equals(company, other.company)
                && Objects.equals(demo, other.demo)
                && Objects.equals(getFinishDate(), other.getFinishDate())
                && Objects.equals(position, other.position)
                && Objects.equals(responsibilities, other.responsibilities)
                && Objects.equals(src, other.src);
    }

    @Override
    public int compareTo(Practic o) {
        int res = DataUtil.compareByFields(o.getFinishDate(), getFinishDate(), true);
        return (res != 0) ? res : DataUtil.compareByFields(o.getBeginDate(), getBeginDate(), true);
    }
}
