package org.novize.api.services;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.novize.api.exceptions.TokenRefreshException;
import org.novize.api.exceptions.UserNotFoundException;
import org.novize.api.model.RefreshToken;
import org.novize.api.repository.RefreshTokenRepository;
import org.novize.api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * RefreshTokenService is a service class that handles operations related to refresh tokens.
 * It provides methods to create, find, and verify refresh tokens.
 */
@Service
public class RefreshTokenService {

    @Value("${security.jwt.refresh-token.expiration-time}")
    private long refreshTokenExpiration;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    @Autowired
    private UserRepository userRepository;

    /**
     * Creates a new refresh token for a user.
     * @param username The username of the user for whom to create the refresh token.
     * @return The created refresh token.
     */
    public RefreshToken createRefreshToken(String username) {
        RefreshToken refreshToken = RefreshToken.builder()
                .user(userRepository.findByEmail(username)
                        .orElseThrow(() -> new UserNotFoundException("User Not Found with username: " + username)))
                .createdDate(Instant.now())
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshTokenExpiration))
                .build();
        return refreshTokenRepository.save(refreshToken);
    }

    /**
     * Retrieves an optional {@link RefreshToken} entity matching the given token string.
     *
     * @param token the refresh token string to search for in the database
     * @return an {@link Optional} containing the matching {@link RefreshToken} if found, or empty if no match is found
     */
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public Optional<RefreshToken> findByUserId(@NotBlank(message = "id is required") String id) {
        return refreshTokenRepository.findByUserId(id);
    }

    /**
     * Verifies the expiration of the given refresh token.
     * If the token is expired, it is deleted from the repository and an exception is thrown.
     * Otherwise, the token is returned as is.
     *
     * @param token the {@link RefreshToken} to verify
     * @return the same {@link RefreshToken} if it is valid (not expired)
     * @throws RuntimeException if the refresh token is expired
     */
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new TokenRefreshException(token.getToken(), "Refresh token was expired. Please make a new sign-in request");
        }
        return token;
    }



    public void deleteByUserId(String id) {
        refreshTokenRepository.deleteByUserId(id);
    }

}