package io.exercise.api.models;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Data
public class Role extends BaseModel {

    @NotEmpty(message = "cannot be empty!")
    @Size(min = 5, message = "should have at least 2 characters")
    String name;
}
