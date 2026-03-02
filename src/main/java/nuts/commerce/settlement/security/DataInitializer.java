package nuts.commerce.settlement.security;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer {

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(AdminUserRepository adminUserRepository, PasswordEncoder passwordEncoder) {
        this.adminUserRepository = adminUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        createIfNotExists("admin1", "password1");
        createIfNotExists("admin2", "password2");
    }

    private void createIfNotExists(String username, String rawPassword) {
        if (adminUserRepository.findByUsername(username).isEmpty()) {
            String hash = passwordEncoder.encode(rawPassword);
            adminUserRepository.save(new AdminUser(username, hash));
        }
    }
}

