package net.devstudy.resume.domain;

import java.time.YearMonth;
import java.time.ZoneId;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Базова модель з “датою завершення” без Joda-Time.
 * Поведінка повністю збережена (еквівалентна старій версії).
 * Сумісна з Java 21 / Spring Boot 3.
 */
public abstract class AbstractFinishDateDocument<T> {

    @JsonIgnore
    private T id;

    @JsonIgnore
    private Date finishDate;

    @JsonIgnore
    private Integer finishDateMonth;

    @JsonIgnore
    private Integer finishDateYear;

    public Date getFinishDate() {
        return finishDate;
    }

    public void setFinishDate(Date finishDate) {
        this.finishDate = finishDate;
    }

    /** @return true, якщо дата завершення задана */
    public boolean isFinish() {
        return finishDate != null;
    }

    public Integer getFinishDateMonth() {
        if (finishDate == null) return null;
        return finishDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .getMonthValue();
    }

    public Integer getFinishDateYear() {
        if (finishDate == null) return null;
        return finishDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .getYear();
    }

    public void setFinishDateMonth(Integer finishDateMonth) {
        this.finishDateMonth = finishDateMonth;
        setupFinishDate();
    }

    public void setFinishDateYear(Integer finishDateYear) {
        this.finishDateYear = finishDateYear;
        setupFinishDate();
    }

    /** Формує finishDate із полів року/місяця (1-ше число, 00:00). */
    private void setupFinishDate() {
        if (finishDateYear != null && finishDateMonth != null) {
            YearMonth ym = YearMonth.of(finishDateYear, finishDateMonth);
            this.finishDate = Date.from(
                ym.atDay(1)
                  .atStartOfDay(ZoneId.systemDefault())
                  .toInstant()
            );
        } else {
            this.finishDate = null;
        }
    }
}
