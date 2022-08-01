package io.exercise.api.models.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AuthUserRequest {

    @NotEmpty(message = "Username cannot be empty!")
    @Size(min = 5, message = "Username should have at least 5 characters!")
    String username;

    @NotEmpty(message = "Password cannot be empty!")
    @Size(min = 5, message = "Password should have at least 5 characters!")
    String password;
}
