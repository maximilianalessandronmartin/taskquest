package org.novize.api.services;

import org.novize.api.dtos.auth.RegisterUserDto;
import org.novize.api.dtos.user.UserDto;
import org.novize.api.enums.RoleEnum;
import org.novize.api.model.Role;
import org.novize.api.model.User;
import org.novize.api.model.UserAchievement;
import org.novize.api.repository.RoleRepository;
import org.novize.api.repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;


    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }



    /**
     * Retrieves a list of all users from the data repository.
     *
     * @return a list containing all users stored in the repository
     */
    public List<User> allUsers() {
        List<User> users = new ArrayList<>();

        userRepository.findAll().forEach(users::add);

        return users;
    }

    /**
     * Saves the provided user entity into the repository and constructs a UserDto object
     * from the newly created user data.
     *
     * @param user the user entity to be saved
     * @return a UserDto object containing the details of the saved user
     */
    @Transactional
    public UserDto saveUser(User user) {
        User createdUser = userRepository.save(user);
        return UserDto.builder()
                .id(createdUser.getId())
                .firstname(createdUser.getFirstname())
                .lastname(createdUser.getLastname())
                .username(createdUser.getUsername())
                .xp(createdUser.getXp())
                .build();
    }

    /**
     * Retrieves the list of achievements for a given user.
     *
     * @param user the user for which the achievements are to be retrieved
     * @return a list of UserAchievement objects associated with the given user
     */
    @Transactional
    public List<UserAchievement> getUserAchievements(User user) {
        return user.getUserAchievements();
    }

    /**
     * Adds a new achievement to the specified user and persists the updated user entity.
     *
     * @param user            the user to whom the new achievement will be added
     * @param newAchievement  the achievement to be added to the user's list of achievements
     * @return a list of the user's updated achievements, including the newly added achievement
     */
    @Transactional
    public List<UserAchievement> addUserAchievement(User user, UserAchievement newAchievement) {
        user.getUserAchievements().add(newAchievement);
        newAchievement.setUser(user); // Setze die Beziehung
        user = userRepository.save(user); // Speichere den Benutzer, um die Änderung zu persistieren
        return user.getUserAchievements();

    }

    /**
     * Updates the achievements of a given user by replacing the existing list
     * of achievements with the provided updated achievements. The user entity
     * is then persisted with the updated list of achievements.
     *
     * @param user the user whose achievements are to be updated; must not be null
     * @param updatedAchievements the new achievements to replace the user's current achievements; must not be null
     * @return a list of the user's updated achievements after they have been persisted
     */
    @Transactional
    public List<UserAchievement> updateUserAchievements(User user, UserAchievement updatedAchievements) {
        // Erstelle eine neue veränderbare Liste mit dem aktualisierten Achievement
        List<UserAchievement> newAchievements = new ArrayList<>();
        newAchievements.add(updatedAchievements);

        // Setze die Benutzerreferenz für das Achievement
        updatedAchievements.setUser(user);

        // Ersetze die Liste komplett anstatt sie zu leeren
        user.setUserAchievements(newAchievements);

        return userRepository.save(user).getUserAchievements();
    }

    /**
     * Retrieves a user from the repository by their username.
     *
     * @param username the username of the user to search for; must not be null or empty
     * @return the User object if found; otherwise, returns null
     */
    @Transactional(readOnly = true)
    public User findUserByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }


    /**
     * Retrieves a user from the repository by their email address.
     *
     * @param email the email address of the user to search for; must not be null or empty
     * @return the User object if found; otherwise, returns null
     */
    @Transactional(readOnly = true)
    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    /**
     * Creates an administrator user by building a User object from the provided input
     * and assigns the administrator role if available in the repository.
     *
     * @param input an instance of RegisterUserDto containing the details of the user to be created;
     *              must include firstname, lastname, email, and password.
     * @return the created User object if the administrator role is found; otherwise, returns null.
     */
    public User createAdministrator(RegisterUserDto input) {
        Optional<Role> optionalRole = roleRepository.findByName(RoleEnum.ADMIN);

        if (optionalRole.isEmpty()) {
            return null;
        }

        var user = User.builder()
                .firstname(input.getFirstname())
                .lastname(input.getLastname())
                .username(input.getEmail())
                .password(passwordEncoder.encode(input.getPassword()))
                .role(optionalRole.get())
                .build();

        return userRepository.save(user);
    }



}
