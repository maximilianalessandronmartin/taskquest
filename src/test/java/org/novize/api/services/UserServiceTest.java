package org.novize.api.services;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.novize.api.dtos.user.UserDto;
import org.novize.api.dtos.auth.RegisterUserDto;
import org.novize.api.enums.RoleEnum;
import org.novize.api.model.Role;
import org.novize.api.model.User;
import org.novize.api.model.UserAchievement;
import org.novize.api.repository.RoleRepository;
import org.novize.api.repository.UserRepository;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;


    @Test
    public void saveUser_shouldReturnUserDto_whenUserIsValid() {
        // Arrange
        User user = User.builder()
                .firstname("John")
                .lastname("Doe")
                .username("john.doe@example.com")
                .password("password123")
                .build();

        User savedUser = User.builder()
                .firstname("John")
                .lastname("Doe")
                .username("john.doe@example.com")
                .password("password123")
                .build();

        savedUser.setId("1234");

        when(userRepository.save(user)).thenReturn(savedUser);

        // Act
        UserDto result = userService.saveUser(user);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("1234");
        assertThat(result.getFirstname()).isEqualTo("John");
        assertThat(result.getLastname()).isEqualTo("Doe");
        assertThat(result.getUsername()).isEqualTo("john.doe@example.com");
        assertThat(result.getXp()).isEqualTo(0L);
    }

    @Test
    public void allUsers_shouldReturnListOfUsers() {
        // Arrange
        User user1 = User.builder()
                .firstname("John")
                .lastname("Doe")
                .username("john.doe@example.com")
                .password("password123")
                .build();
        user1.setId("1234");

        User user2 = User.builder()
                .firstname("Jane")
                .lastname("Smith")
                .username("jane.smith@example.com")
                .password("password456")
                .build();
        user2.setId("5678");

        when(userRepository.findAll()).thenReturn(List.of(user1, user2));

        // Act
        List<User> result = userService.allUsers();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.getFirst().getId()).isEqualTo("1234");
        assertThat(result.getFirst().getFirstname()).isEqualTo("John");
        assertThat(result.get(0).getLastname()).isEqualTo("Doe");
        assertThat(result.get(0).getUsername()).isEqualTo("john.doe@example.com");

        assertThat(result.get(1).getId()).isEqualTo("5678");
        assertThat(result.get(1).getFirstname()).isEqualTo("Jane");
        assertThat(result.get(1).getLastname()).isEqualTo("Smith");
        assertThat(result.get(1).getUsername()).isEqualTo("jane.smith@example.com");
    }
    
    @Test
    public void getUserAchievements_shouldReturnUserAchievements() {
        // Arrange
        UserAchievement achievement1 = UserAchievement.builder()
                .announced(true)
                .build();
        achievement1.setId("achievement1");

        UserAchievement achievement2 = UserAchievement.builder()
                .announced(false)
                .build();
        achievement2.setId("achievement2");

        User user = User.builder()
                .firstname("John")
                .lastname("Doe")
                .username("john.doe@example.com")
                .password("password123")
                .userAchievements(List.of(achievement1, achievement2))
                .build();
        user.setId("1234");
        // Act
        List<UserAchievement> result = userService.getUserAchievements(user);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo("achievement1");
        assertThat(result.get(0).isAnnounced()).isTrue();
        assertThat(result.get(1).getId()).isEqualTo("achievement2");
        assertThat(result.get(1).isAnnounced()).isFalse();
    }
    
    @Test
    public void addUserAchievement_shouldAddAchievementToUser() {
        // Arrange
        User user = User.builder()
                .firstname("John")
                .lastname("Doe")
                .username("john.doe@example.com")
                .password("password123")
                .userAchievements(new ArrayList<>())
                .build();
        user.setId("1234");
        
        UserAchievement newAchievement = UserAchievement.builder()
                .announced(false)
                .build();
        newAchievement.setId("new-achievement");
        
        User updatedUser = User.builder()
                .firstname("John")
                .lastname("Doe")
                .username("john.doe@example.com")
                .password("password123")
                .userAchievements(List.of(newAchievement))
                .build();
        updatedUser.setId("1234");
        
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        
        // Act
        List<UserAchievement> result = userService.addUserAchievement(user, newAchievement);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo("new-achievement");
        assertThat(result.getFirst().isAnnounced()).isFalse();
        
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        
        User capturedUser = userCaptor.getValue();
        assertThat(capturedUser.getUserAchievements()).hasSize(1);
        assertThat(capturedUser.getUserAchievements().getFirst().getId()).isEqualTo("new-achievement");
    }
    
    @Test
    public void updateUserAchievements_shouldUpdateUserAchievements() {
        // Arrange
        UserAchievement existingAchievement = UserAchievement.builder()
                .announced(false)
                .build();
        existingAchievement.setId("existing-achievement");
        
        User user = User.builder()
                .firstname("John")
                .lastname("Doe")
                .username("john.doe@example.com")
                .password("password123")
                .userAchievements(List.of(existingAchievement))
                .build();
        user.setId("1234");
        
        UserAchievement updatedAchievement = UserAchievement.builder()
                .announced(true)
                .build();
        updatedAchievement.setId("existing-achievement");
        
        User savedUser = User.builder()
                .firstname("John")
                .lastname("Doe")
                .username("john.doe@example.com")
                .password("password123")
                .userAchievements(List.of(updatedAchievement))
                .build();
        savedUser.setId("1234");
        
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        
        // Act
        List<UserAchievement> result = userService.updateUserAchievements(user, updatedAchievement);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo("existing-achievement");
        assertThat(result.getFirst().isAnnounced()).isTrue();
        
        verify(userRepository).save(any(User.class));
    }
    
    @Test
    public void findUserByEmail_shouldReturnUser_whenUserExists() {
        // Arrange
        String username = "john.doe@example.com";
        User user = User.builder()
                .firstname("John")
                .lastname("Doe")
                .username(username)
                .password("password123")
                .build();
        user.setId("1234");
        
        when(userRepository.findByEmail(username)).thenReturn(Optional.of(user));
        
        // Act
        User result = userService.findUserByEmail(username);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("1234");
        assertThat(result.getFirstname()).isEqualTo("John");
        assertThat(result.getLastname()).isEqualTo("Doe");
        assertThat(result.getUsername()).isEqualTo(username);
    }
    
    @Test
    public void findUserByEmail_shouldReturnNull_whenUserDoesNotExist() {
        // Arrange
        String username = "nonexistent@example.com";
        when(userRepository.findByEmail(username)).thenReturn(Optional.empty());
        
        // Act
        User result = userService.findUserByEmail(username);
        
        // Assert
        assertThat(result).isNull();
    }
    
    @Test
    public void createAdministrator_shouldCreateAdminUser() {
        // Arrange
        RegisterUserDto registerUserDto = RegisterUserDto.builder()
                .firstname("Admin")
                .lastname("User")
                .email("admin@example.com")
                .password("adminpass")
                .build();

        
        Role adminRole = Role.builder()
                .name(RoleEnum.ADMIN)
                .description("Administrator role")
                .build();
        adminRole.setId("admin-role-id");
        
        User adminUser = User.builder()
                .firstname("Admin")
                .lastname("User")
                .username("admin@example.com")
                .password("encoded-password")
                .role(adminRole)
                .build();
        adminUser.setId("admin-id");
        
        when(roleRepository.findByName(RoleEnum.ADMIN)).thenReturn(Optional.of(adminRole));
        when(passwordEncoder.encode(registerUserDto.getPassword())).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenReturn(adminUser);
        
        // Act
        User result = userService.createAdministrator(registerUserDto);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("admin-id");
        assertThat(result.getFirstname()).isEqualTo("Admin");
        assertThat(result.getLastname()).isEqualTo("User");
        assertThat(result.getUsername()).isEqualTo("admin@example.com");
        assertThat(result.getPassword()).isEqualTo("encoded-password");
        assertThat(result.getRole().getName()).isEqualTo(RoleEnum.ADMIN);
        
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        
        User capturedUser = userCaptor.getValue();
        assertThat(capturedUser.getFirstname()).isEqualTo("Admin");
        assertThat(capturedUser.getLastname()).isEqualTo("User");
        assertThat(capturedUser.getUsername()).isEqualTo("admin@example.com");
        assertThat(capturedUser.getPassword()).isEqualTo("encoded-password");
        assertThat(capturedUser.getRole()).isEqualTo(adminRole);
    }
    
    @Test
    public void createAdministrator_shouldReturnNull_whenAdminRoleNotFound() {
        // Arrange
        RegisterUserDto registerUserDto = RegisterUserDto.builder()
                .firstname("Admin")
                .lastname("User")
                .email("admin@example.com")
                .password("adminpass")
                .build();
        
        when(roleRepository.findByName(RoleEnum.ADMIN)).thenReturn(Optional.empty());
        
        // Act
        User result = userService.createAdministrator(registerUserDto);
        
        // Assert
        assertThat(result).isNull();
        verify(userRepository, never()).save(any(User.class));
    }
}