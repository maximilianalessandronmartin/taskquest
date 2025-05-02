package org.novize.api.controller;

import jakarta.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.novize.api.dtos.*;
import org.novize.api.dtos.auth.AuthenticationResponse;
import org.novize.api.dtos.auth.LoginUserDto;
import org.novize.api.dtos.auth.RefreshRequestDto;
import org.novize.api.dtos.auth.RegisterUserDto;
import org.novize.api.model.RefreshToken;
import org.novize.api.model.User;
import org.novize.api.services.AuthenticationService;
import org.novize.api.services.JwtService;
import org.novize.api.services.RefreshTokenService;
import org.novize.api.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RequestMapping("/api/auth")
@RestController
public class AuthenticationController {
    private final static Logger logger = LogManager.getLogger(AuthenticationController.class);

    private final JwtService jwtService;
    private final AuthenticationService authenticationService;
    private final UserService userService;
    private final RefreshTokenService refreshTokenService;

    public AuthenticationController(
            JwtService jwtService,
            AuthenticationService authenticationService,
            UserService userService,
            RefreshTokenService refreshTokenService) {
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
        this.userService = userService;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/signup")
    public ResponseEntity<UserDto> register(@RequestBody @Valid RegisterUserDto registerUserDto) {
        UserDto registeredUser = authenticationService.signup(registerUserDto);
        return ResponseEntity.ok(registeredUser);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthenticationResponse> refresh(@RequestBody RefreshRequestDto refreshRequestDto) {
        logger.info("Received refresh token: {}", refreshRequestDto.getToken());
        String requestToken = refreshRequestDto.getToken();

        // Suche den Refresh-Token in der Datenbank
        Optional<RefreshToken> refreshTokenOptional = refreshTokenService.findByToken(requestToken);

        if (refreshTokenOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Verifiziere, dass der Token nicht abgelaufen ist
        RefreshToken refreshToken;
        try {
            refreshToken = refreshTokenService.verifyExpiration(refreshTokenOptional.get());
        } catch (RuntimeException e) {
            logger.error("Refresh token ist abgelaufen: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Hole den Benutzer aus dem Token
        User user = refreshToken.getUser();

        // Generiere ein neues Access-Token
        String newAccessToken = jwtService.generateToken(user);

        AuthenticationResponse authenticationResponse = AuthenticationResponse.builder()
                .token(newAccessToken)
                .refreshToken(requestToken)
                .expiresIn(jwtService.getJwtExpiration())
                .tokenType(jwtService.getTokenType())
                .build();

        return ResponseEntity.ok(authenticationResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody @Valid LoginUserDto loginUserDto) {
        User authenticatedUser = authenticationService.authenticate(loginUserDto);

        // Generiere ein neues Access-Token
        String jwtToken = jwtService.generateToken(authenticatedUser);

        // Lösche vorhandene Refresh-Tokens des Benutzers
        refreshTokenService.deleteByUserId(authenticatedUser.getId());


        // Erstelle einen neuen Refresh-Token
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(authenticatedUser.getUsername());

        AuthenticationResponse authenticationResponse = AuthenticationResponse.builder()
                .token(jwtToken)
                .refreshToken(refreshToken.getToken())
                .expiresIn(jwtService.getJwtExpiration())
                .tokenType(jwtService.getTokenType())
                .build();

        return ResponseEntity.ok(authenticationResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody @Valid LogoutRequestDto logoutRequestDto) {
        String token = logoutRequestDto.getToken();
        Optional<RefreshToken> refreshTokenOptional = refreshTokenService.findByToken(token);

        if (refreshTokenOptional.isPresent()) {
            refreshTokenService.deleteByUserId(refreshTokenOptional.get().getUser().getId());
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}