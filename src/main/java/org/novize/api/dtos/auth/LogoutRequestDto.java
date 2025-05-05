package org.novize.api.dtos.auth;

import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class LogoutRequestDto {
    private String token;

    public LogoutRequestDto(String token) {
        this.token = token;
    }

}
