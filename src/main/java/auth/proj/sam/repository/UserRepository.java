package auth.proj.sam.repository;

import auth.proj.sam.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email); // For password reset
    User findByPasswordResetToken(String token); // For password reset
}