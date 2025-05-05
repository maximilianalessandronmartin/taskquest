package org.novize.api.dtos.user;

import lombok.*;

import java.util.Date;


@Data
@Builder
public class UserDto {
    String id;
    Date createdAt;
    String firstname;
    String lastname;
    String username;
    String email;
    Long xp;
}
