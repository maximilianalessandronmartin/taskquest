package org.novize.api.services;

import org.junit.jupiter.api.Test;
import org.novize.api.model.RefreshToken;
import org.novize.api.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
public class JwtServiceTest {

    @Autowired  // Ändern Sie @InjectMocks zu @Autowired
    private JwtService jwtService;

    @MockitoBean  // Ändern Sie @Mock zu @MockBean
    private RefreshTokenService refreshTokenService;



    @Test
    void generateTokens_UserDetailsTokensAreGenerated() {
        User user = new User();
        user.setFirstname("John");
        user.setLastname("Doe");
        user.setUsername("john.doe@example.com");
        user.setPassword("password");

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("mockRefreshToken");
        when(refreshTokenService.createRefreshToken(anyString())).thenReturn(refreshToken);

        Map<String, String> tokens = jwtService.generateTokens(user);

        assertNotNull(tokens);
        assertNotNull(tokens.get("accessToken"));
        assertEquals("mockRefreshToken", tokens.get("refreshToken"));
    }
}