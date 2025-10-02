package net.devstudy.resume.domain;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document; // ES документ
import org.springframework.data.mongodb.core.index.Indexed;   // Mongo індекси

import com.fasterxml.jackson.annotation.JsonIgnore;

import net.devstudy.resume.annotation.ProfileDataFieldGroup;
import net.devstudy.resume.annotation.ProfileInfoField;
import net.devstudy.resume.annotation.constraints.Adulthood;
import net.devstudy.resume.annotation.constraints.EnglishLanguage;
import net.devstudy.resume.annotation.constraints.Phone;

@Entity
@Table(name = "profile")
public class Profile {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 64)
    private String uid;

    @Column(nullable = false)
    private boolean completed;

    @Column(nullable = false, length = 64)
    private String firstName;

    @Column(nullable = false, length = 64)
    private String lastName;

    // JPA-потрібний конструктор без аргументів
    public Profile() {}

    // Зручний all-args конструктор
    public Profile(Long id, String uid, boolean completed, String firstName, String lastName) {
        this.id = id;
        this.uid = uid;
        this.completed = completed;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    // Getters / Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    @Override
    public String toString() {
        return "Profile{id=" + id +
                ", uid='" + uid + '\'' +
                ", completed=" + completed +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                '}';
    }
}
