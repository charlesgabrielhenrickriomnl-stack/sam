package auth.proj.sam.repository;

import auth.proj.sam.model.TrustedDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TrustedDeviceRepository extends JpaRepository<TrustedDevice, Long> {
    Optional<TrustedDevice> findByToken(String token);
    void deleteByUserAndToken(auth.proj.sam.model.User user, String token);
}