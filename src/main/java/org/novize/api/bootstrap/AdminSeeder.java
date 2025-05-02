package org.novize.api.bootstrap;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.novize.api.enums.RoleEnum;

import org.novize.api.model.User;
import org.novize.api.repository.RoleRepository;
import org.novize.api.repository.UserRepository;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Order(2)
@Component
public class AdminSeeder implements ApplicationListener<ContextRefreshedEvent> {
    private static final Logger logger = LogManager.getLogger(AdminSeeder.class);
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    public AdminSeeder(
            RoleRepository roleRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        this.createSuperAdministrator();
    }

    private void createSuperAdministrator() {

        if (roleRepository.findByName(RoleEnum.SUPER_ADMIN).isEmpty() ||
                userRepository.findByUsername("super.admin@email.com").isPresent()) {
            logger.info("Super Admin already exists or role not found");
            return;
        }

        User user = User.builder()
                .firstname("Super")
                .lastname("Admin")
                .username("super.admin@email.com")
                .password(passwordEncoder.encode("123456"))
                .role(roleRepository.findByName(RoleEnum.SUPER_ADMIN).get())
                .build();

        userRepository.save(user);
    }
}

