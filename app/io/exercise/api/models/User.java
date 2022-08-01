package io.exercise.api.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class User extends BaseModel {

    @NotEmpty(message = "cannot be empty")
    @Size(min = 5, message = "should have at least 5 characters")
    String username;

    @NotEmpty(message = "cannot be empty")
    @Size(min = 5, message = "should have at least 5 characters")
    String password;

    List<Role> roles = new ArrayList<>();
}
