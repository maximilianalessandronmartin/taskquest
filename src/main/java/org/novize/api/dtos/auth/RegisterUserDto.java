package org.novize.api.dtos.auth;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegisterUserDto {

    private String firstname;

    private String lastname;

    private String email;

    private String password;

}
