package org.novize.api.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.novize.api.dtos.user.UserDto;
import org.novize.api.dtos.auth.LoginUserDto;
import org.novize.api.dtos.auth.RegisterUserDto;
import org.novize.api.enums.RoleEnum;
import org.novize.api.model.Role;
import org.novize.api.model.User;
import org.novize.api.repository.RoleRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    @Test
    void testSignupReturnsUserDtoWhenRoleExists() {
        // Arrange
        RegisterUserDto registerUserDto = RegisterUserDto.builder()
                .firstname("John")
                .lastname("Doe")
                .email("john.doe@example.com")
                .password("password123")
                .build();

        Role userRole = Role.builder()
                .name(RoleEnum.USER)
                .description("User role") // Hinzugefügt, da das Feld nicht null sein darf
                .build();

        UserDto userDto = UserDto.builder()
                .id("1")
                .firstname("John")
                .lastname("Doe")
                .username("john.doe@example.com")
                .xp(0L)
                .build();

        String encodedPassword = "encodedPassword123";

        when(roleRepository.findByName(RoleEnum.USER)).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode(registerUserDto.getPassword())).thenReturn(encodedPassword);
        when(userService.saveUser(any(User.class))).thenReturn(userDto);

        // Act
        UserDto result = authenticationService.signup(registerUserDto);

        // Assert
        assertEquals(userDto, result);

        // Verify interactions
        verify(roleRepository).findByName(RoleEnum.USER);
        verify(passwordEncoder).encode(registerUserDto.getPassword());
        verify(userService).saveUser(userCaptor.capture());

        // Verify user object passed to userService.saveUser
        User capturedUser = userCaptor.getValue();
        assertEquals(registerUserDto.getFirstname(), capturedUser.getFirstname());
        assertEquals(registerUserDto.getLastname(), capturedUser.getLastname());
        assertEquals(registerUserDto.getEmail(), capturedUser.getUsername());
        assertEquals(encodedPassword, capturedUser.getPassword());
        assertEquals(userRole, capturedUser.getRole());
    }

    @Test
    void testSignupReturnsNullWhenRoleDoesNotExist() {
        // Arrange
        RegisterUserDto registerUserDto = RegisterUserDto.builder()
                .firstname("Jane")
                .lastname("Smith")
                .email("jane.smith@example.com")
                .password("password456")
                .build();

        when(roleRepository.findByName(RoleEnum.USER)).thenReturn(Optional.empty());

        // Act
        UserDto result = authenticationService.signup(registerUserDto);

        // Assert
        assertNull(result);
        verify(roleRepository).findByName(RoleEnum.USER);
    }
    @Test
    void testAuthenticate_WithValidCredentials_ShouldReturnUser() {
        // Arrange
        LoginUserDto loginUserDto = LoginUserDto.builder()
                .email("test@example.com")
                .password("password123")
                .build();

        User expectedUser = User.builder()
                .username("test@example.com")
                .password("encodedPassword123")
                .firstname("Max")
                .lastname("Mustermann")
                .build();

        // Erstellen Sie ein UserDetails-Objekt (wahrscheinlich CustomUserDetails)
        // Option 1: UserDetails als Principal
        when(userService.findUserByUsername(loginUserDto.getEmail())).thenReturn(expectedUser);

        // Option 2: Direkter User als Principal
        Authentication authentication = mock(Authentication.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        // Act
        User result = authenticationService.authenticate(loginUserDto);

        // Assert
        assertEquals(expectedUser, result);
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void testAuthenticate_WhenAuthenticationFails_ShouldThrowException() {
        // Arrange
        LoginUserDto loginUserDto = LoginUserDto.builder()
                .email("test@example.com")
                .password("password123")
                .build();

        AuthenticationException expectedException = new AuthenticationException("Authentication failed") {};
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(expectedException);

        // Act & Assert
        assertThrows(AuthenticationException.class, () -> authenticationService.authenticate(loginUserDto));

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        // Stelle sicher, dass findUserByEmail nicht aufgerufen wird
        verify(userService, never()).findUserByEmail(anyString());
    }



}