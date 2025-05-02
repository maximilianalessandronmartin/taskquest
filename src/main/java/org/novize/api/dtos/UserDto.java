package org.novize.api.dtos;

import lombok.*;


@Data
@Builder
public class UserDto {
    String id;
    String firstname;
    String lastname;
    String username;
    Long xp;
}
