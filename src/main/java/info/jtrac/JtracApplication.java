package info.jtrac;

import info.jtrac.config.SecurityConfig;
import info.jtrac.domain.User;
import info.jtrac.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class JtracApplication {

    public static void main(String[] args) {
        SpringApplication.run(JtracApplication.class, args);
    }

    @Bean
    @Profile("!test")
    public CommandLineRunner demo(UserRepository repository, PasswordEncoder passwordEncoder) {
        return (args) -> {
            if (repository.findByLoginName("admin").isEmpty()) {
                User admin = new User();
                admin.setLoginName("admin");
                admin.setName("Admin");
                admin.setEmail("admin@jtrac.info");
                admin.setPassword(passwordEncoder.encode("admin"));
                repository.save(admin);
            }
        };
    }

}
