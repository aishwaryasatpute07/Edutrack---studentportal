package com.edutrack.studentportal.config;

import com.edutrack.studentportal.model.User;
import com.edutrack.studentportal.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class AdminUserBootstrap implements ApplicationRunner {

    private final UserRepository userRepository;
    private final String username;
    private final String email;
    private final String password;

    public AdminUserBootstrap(UserRepository userRepository,
                              @Value("${app.bootstrap-admin.username:}") String username,
                              @Value("${app.bootstrap-admin.email:}") String email,
                              @Value("${app.bootstrap-admin.password:}") String password) {
        this.userRepository = userRepository;
        this.username = username;
        this.email = email;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.count() == 0 && hasBootstrapCredentials()) {
            User admin = new User();
            admin.setUsername(username);
            admin.setEmail(email);
            admin.setPassword(password);
            admin.setRole("ADMIN");
            userRepository.save(admin);
        }
    }

    private boolean hasBootstrapCredentials() {
        return username != null && !username.isBlank()
                && email != null && !email.isBlank()
                && password != null && !password.isBlank();
    }
}