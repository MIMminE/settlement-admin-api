package nuts.commerce.settlement.security;

import jakarta.validation.constraints.NotBlank;
import nuts.commerce.settlement.security.jwt.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthController(AdminUserRepository adminUserRepository, PasswordEncoder passwordEncoder, JwtTokenProvider tokenProvider) {
        this.adminUserRepository = adminUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    public record RegisterRequest(@NotBlank String username, @NotBlank String password) {
    }

    public record LoginRequest(@NotBlank String username, @NotBlank String password) {
    }

    public record TokenResponse(String accessToken) {
    }

    @PostMapping("/register")
    public String register(@RequestBody RegisterRequest req) {
        String hash = passwordEncoder.encode(req.password());
        adminUserRepository.save(new AdminUser(req.username(), hash));
        return "OK";
    }

    @PostMapping("/login")
    public TokenResponse login(@RequestBody LoginRequest req) {
        AdminUser user = adminUserRepository.findByUsername(req.username()).orElseThrow();
        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }
        return new TokenResponse(tokenProvider.createAccessToken(user.getUsername()));
    }
}
