package org.novize.api.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.novize.api.exceptions.UserNotFoundException;
import org.novize.api.model.RefreshToken;
import org.novize.api.model.User;
import org.novize.api.repository.RefreshTokenRepository;
import org.novize.api.repository.UserRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)  // Verwendet Mockito statt Spring
public class RefreshTokenServiceTest {

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;


    @Test
    public void createRefreshToken_whenUserExists_shouldReturnSavedRefreshToken() {
        // Arrange
        String username = "testuser@example.com";
        long expirationTime = 60000L; // Example expiration time
        User user = new User();
        user.setId("id");
        user.setUsername(username);
        RefreshToken expectedToken = RefreshToken.builder()
                .user(user)
                .createdDate(Instant.now())
                .expiryDate(Instant.now().plusMillis(expirationTime))
                .token(UUID.randomUUID().toString())
                .build();

        when(userRepository.findByEmail(username)).thenReturn(Optional.of(user));
        when(refreshTokenRepository.save(Mockito.any(RefreshToken.class))).thenReturn(expectedToken);

        // Act
        RefreshToken actualToken = refreshTokenService.createRefreshToken(username);

        // Assert
        assertNotNull(actualToken);
        assertEquals(expectedToken.getToken(), actualToken.getToken());
        assertEquals(expectedToken.getUser(), actualToken.getUser());
        verify(userRepository, times(1)).findByEmail(username);
        verify(refreshTokenRepository, times(1)).save(Mockito.any(RefreshToken.class));
    }

    @Test
    public void createRefreshToken_whenUserDoesNotExist_shouldThrowUserNotFoundException() {
        // Arrange
        String username = "nonexistentuser@example.com";
        when(userRepository.findByEmail(username)).thenReturn(Optional.empty());

        // Act & Assert
        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> refreshTokenService.createRefreshToken(username));
        assertEquals("User Not Found with username: " + username, exception.getMessage());
        verify(userRepository, times(1)).findByEmail(username);
        verify(refreshTokenRepository, never()).save(Mockito.any(RefreshToken.class));
    }
}