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
        initializeRolesAndUsers();
    }

    private void initializeRolesAndUsers() {
        // Create roles if they don't exist
        Role teacherRole = findOrCreateRole("ROLE_TEACHER");
        Role studentRole = findOrCreateRole("ROLE_STUDENT");
        // NEW: Create Department Role
        Role departmentRole = findOrCreateRole("ROLE_DEPARTMENT");

        // --- START: DEPARTMENT AUTO-CREATION BLOCK ---
        Optional<User> existingDepartment = userRepository.findByUsername(DEFAULT_DEPT_USERNAME);

        if (existingDepartment.isEmpty()) {
            User departmentUser = new User();
            departmentUser.setUsername(DEFAULT_DEPT_USERNAME);
            departmentUser.setEmail(DEFAULT_DEPT_EMAIL);
            departmentUser.setPassword(passwordEncoder.encode(DEFAULT_DEPT_PASSWORD));
            departmentUser.setEnabled(true);
            departmentUser.getRoles().add(departmentRole); 
            departmentUser.setFirstName("Department");
            departmentUser.setLastName("Admin");
            userRepository.save(departmentUser);
            System.out.println("✅ Created initial department user (Username: " + DEFAULT_DEPT_USERNAME + ").");
        } else {
             System.out.println("✅ Department user already exists (Username: " + DEFAULT_DEPT_USERNAME + ").");
        }
        // --- END: DEPARTMENT AUTO-CREATION BLOCK ---
    }

    private Role findOrCreateRole(String roleName) {
        Optional<Role> roleOptional = roleRepository.findByName(roleName);
        if (roleOptional.isEmpty()) {
            Role newRole = new Role();
            newRole.setName(roleName);
            return roleRepository.save(newRole);
        }
        return roleOptional.get();
    }
}