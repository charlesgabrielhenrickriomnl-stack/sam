package auth.proj.sam.config;

import auth.proj.sam.model.Role;
import auth.proj.sam.model.User;
import auth.proj.sam.repository.RoleRepository;
import auth.proj.sam.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    
    // Constant for the default DEPARTMENT user
    private static final String DEFAULT_DEPT_EMAIL = "department@sam.edu";
    private static final String DEFAULT_DEPT_USERNAME = "department"; 
    private static final String DEFAULT_DEPT_PASSWORD = "departmentpass"; 

    public DataInitializer(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        verifyDatabaseInitialization();
    }

    private void verifyDatabaseInitialization() {
        // Verify roles exist in database
        verifyRoleExists("ROLE_TEACHER");
        verifyRoleExists("ROLE_STUDENT");
        verifyRoleExists("ROLE_DEPARTMENT");

        // Verify department user exists in database
        Optional<User> existingDepartment = userRepository.findByUsername(DEFAULT_DEPT_USERNAME);
        if (existingDepartment.isEmpty()) {
            System.err.println("⚠️ Department user is missing from database. Please check data.sql file.");
        } else {
            System.out.println("✅ Department user verified in database (Username: " + DEFAULT_DEPT_USERNAME + ").");
        }
    }

    private void verifyRoleExists(String roleName) {
        Optional<Role> roleOptional = roleRepository.findByName(roleName);
        if (roleOptional.isEmpty()) {
            System.err.println("⚠️ Role " + roleName + " is missing from database. Please check data.sql file.");
        } else {
            System.out.println("✅ Role " + roleName + " verified in database.");
        }
    }
}