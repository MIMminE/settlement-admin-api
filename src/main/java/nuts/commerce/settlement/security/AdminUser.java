package nuts.commerce.settlement.security;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "admin_user", uniqueConstraints = {
        @UniqueConstraint(name = "uq_admin_username", columnNames = {"username"})
})
@Getter
public class AdminUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 200)
    private String passwordHash;

    protected AdminUser() {
    }

    public AdminUser(String username, String passwordHash) {
        this.username = username;
        this.passwordHash = passwordHash;
    }
}