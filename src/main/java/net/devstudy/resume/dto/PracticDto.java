package net.devstudy.resume.dto;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PracticDto {
    private Long id;

    @NotBlank
    private String company;

    private String demo;

    private String src;

    @NotBlank
    private String position;

    @NotBlank
    private String responsibilities;

    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate beginDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate finishDate;

    /**
     * Чекбокс у формі: true означає є дата завершення, false — ще триває.
     */
    private Boolean finish;
}
