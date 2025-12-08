package net.devstudy.resume.search;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * Elasticsearch документ для пошуку профілів.
 */
@Document(indexName = "profiles")
public class ProfileSearchDocument {

    @Id
    private Long id;

    @Field(type = FieldType.Keyword)
    private String uid;

    @Field(type = FieldType.Text)
    private String fullName;

    @Field(type = FieldType.Text)
    private String objective;

    @Field(type = FieldType.Text)
    private String summary;

    @Field(type = FieldType.Text)
    private String skills;

    public ProfileSearchDocument() {
    }

    public ProfileSearchDocument(Long id, String uid, String fullName, String objective, String summary, String skills) {
        this.id = id;
        this.uid = uid;
        this.fullName = fullName;
        this.objective = objective;
        this.summary = summary;
        this.skills = skills;
    }

    public Long getId() {
        return id;
    }

    public String getUid() {
        return uid;
    }

    public String getFullName() {
        return fullName;
    }

    public String getObjective() {
        return objective;
    }

    public String getSummary() {
        return summary;
    }

    public String getSkills() {
        return skills;
    }
}
