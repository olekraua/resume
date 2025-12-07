package net.devstudy.resume.form;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HobbyForm {
    @NotEmpty
    private List<Long> hobbyIds;
}
