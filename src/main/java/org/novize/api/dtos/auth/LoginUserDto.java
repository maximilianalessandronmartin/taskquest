package org.novize.api.dtos.auth;

import jakarta.validation.constraints.Email;
import lombok.Builder;
import lombok.Data;
import org.novize.api.validator.Password;

@Data
@Builder
public class LoginUserDto {
    @Email(message = "Email must be valid")
    private String email;

    private String password;

}
