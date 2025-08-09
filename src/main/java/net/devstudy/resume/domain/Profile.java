package net.devstudy.resume.domain;

public class Profile {
    private Long id;
    private boolean completed = true;

    public Profile() {}
    public Profile(Long id, boolean completed) { this.id = id; this.completed = completed; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
}
