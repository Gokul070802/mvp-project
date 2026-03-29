package com.mvp.mvp_backend.config;

import com.mvp.mvp_backend.model.User;
import com.mvp.mvp_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Seed admin user if not exists
        String adminEmail = "admin@mvp.com";
        if (!userRepository.findByEmail(adminEmail).isPresent()) {
            User adminUser = new User();
            adminUser.setEmail(adminEmail);
            adminUser.setName("Admin User");
            adminUser.setPassword(passwordEncoder.encode("Admin@123456"));
            adminUser.setRole("ROLE_ADMIN");
            userRepository.save(adminUser);
            System.out.println("✅ Admin user seeded: admin@mvp.com / Admin@123456");
        } else {
            System.out.println("✅ Admin user already exists");
        }
    }
}
