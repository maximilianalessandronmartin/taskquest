package org.novize.api.dtos.auth;


import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class AuthenticationResponse {
    private String token;
    private String refreshToken;
    private long expiresIn;
    private String tokenType;

}
