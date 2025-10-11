package net.devstudy.resume.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.springframework.data.annotation.Id;

import com.fasterxml.jackson.annotation.JsonIgnore;

import net.devstudy.resume.annotation.ProfileDataFieldGroup;
import net.devstudy.resume.annotation.ProfileInfoField;
import net.devstudy.resume.annotation.constraints.Adulthood;
import net.devstudy.resume.annotation.constraints.EnglishLanguage;
import net.devstudy.resume.annotation.constraints.Phone;



@Entity
public class Profile extends AbstractDocument<Long>{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ProfileDataFieldGroup
    @Adulthood
    @NotNull
    private Date birthDay;

    @ProfileDataFieldGroup
    @Size(max = 100)
    @NotNull
    @EnglishLanguage(withNumbers = false, withSpecialSymbols = false)
    private String city;

    @ProfileDataFieldGroup
    @Size(max = 60)
    @NotNull
    @EnglishLanguage(withNumbers = false, withSpecialSymbols = false)
    private String country;

    private String firstName;

    private String lastName;

    @ProfileDataFieldGroup
    @NotNull
    @EnglishLanguage
    private String objective;

    @JsonIgnore
    @Size(max = 255)
    private String largePhoto;

    @Size(max = 255)
    private String smallPhoto;

    @JsonIgnore
    @ProfileDataFieldGroup
    @NotNull
    @Size(max = 20)
    @Phone
    private String phone;

    @JsonIgnore
    @ProfileDataFieldGroup
    @NotNull
    @Size(max = 100)
    @Email
    @EnglishLanguage
    private String email;

    @ProfileInfoField
    private String info;

    @ProfileDataFieldGroup
    @NotNull
    @EnglishLanguage
    private String summary;

    private String uid;

    @JsonIgnore
    private String password;

    @JsonIgnore
    private boolean completed;

    private Date created;

    private List<Certificate> certificates;

    @JsonIgnore
    private List<Education> educations;

    @JsonIgnore
    private List<Hobby> hobbies;

    private List<Language> languages;

    private List<Practic> practics;

    private List<Skill> skills;

    private List<Course> courses;

    @JsonIgnore
    private Contacts contacts;

    // JPA-потрібний конструктор без аргументів
    public Profile() {
        // Required by frameworks (Spring Data, Jackson) for object creation
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getBirthDay() {
        return this.birthDay;
    }

    public void setBirthDay(Date birthDay) {
        this.birthDay = birthDay;
    }

    public String getCity() {
        return this.city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return this.country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return this.lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getObjective() {
        return this.objective;
    }

    public void setObjective(String objective) {
        this.objective = objective;
    }

    public String getSummary() {
        return this.summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getUid() {
        return this.uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public List<Certificate> getCertificates() {
        return this.certificates;
    }

    public void setCertificates(List<Certificate> certificates) {
        this.certificates = certificates;
    }

    public List<Education> getEducations() {
        return this.educations;
    }

    public void setEducations(List<Education> educations) {
        this.educations = educations;
    }

    public List<Hobby> getHobbies() {
        return this.hobbies;
    }

    public void setHobbies(List<Hobby> hobbies) {
        this.hobbies = hobbies;
    }

    public List<Language> getLanguages() {
        return this.languages;
    }

    public void setLanguages(List<Language> languages) {
        this.languages = languages;
    }

    public List<Practic> getPractics() {
        return this.practics;
    }

    public void setPractics(List<Practic> practics) {
        this.practics = practics;
    }

    public List<Skill> getSkills() {
        return this.skills;
    }

    public void setSkills(List<Skill> skills) {
        this.skills = skills;
    }

    public List<Course> getCourses() {
        return courses;
    }

    public void setCourses(List<Course> courses) {
        this.courses = courses;
    }

    public String getLargePhoto() {
        return largePhoto;
    }

    public void setLargePhoto(String largePhoto) {
        this.largePhoto = largePhoto;
    }

    public String getSmallPhoto() {
        return smallPhoto;
    }

    public void setSmallPhoto(String smallPhoto) {
        this.smallPhoto = smallPhoto;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public String getFullName() {
        return ((firstName == null ? "" : firstName) + " " + (lastName == null ? "" : lastName)).trim();
    }

    public int getAge() {
        if (birthDay == null)
            return 0;
        LocalDate birthdate = Instant.ofEpochMilli(birthDay.getTime())
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        return Period.between(birthdate, LocalDate.now()).getYears();
    }

    public String getProfilePhoto() {
        return (largePhoto != null) ? largePhoto : "/static/img/profile-placeholder.png";
    }

    public void updateProfilePhotos(String largePhoto, String smallPhoto) {
        setLargePhoto(largePhoto);
        setSmallPhoto(smallPhoto);
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public Contacts getContacts() {
        if (contacts == null) {
            contacts = new Contacts();
        }
        return contacts;
    }

    public void setContacts(Contacts contacts) {
        this.contacts = contacts;
    }

    @Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof Profile))
			return false;
		Profile that = (Profile) o;
		return id != null && id.equals(that.id);
	}

    @Override
	public int hashCode() {
		return getClass().hashCode();
	}
}
