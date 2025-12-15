package auth.proj.sam.config;

import auth.proj.sam.model.Role;
import auth.proj.sam.model.Subject;
import auth.proj.sam.model.User;
import auth.proj.sam.repository.RoleRepository;
import auth.proj.sam.repository.SubjectRepository;
import auth.proj.sam.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final SubjectRepository subjectRepository; // Injected SubjectRepository
    private final PasswordEncoder passwordEncoder;

    // Constant for the default DEPARTMENT user
    private static final String DEFAULT_DEPT_EMAIL = "department@sam.edu";
    private static final String DEFAULT_DEPT_USERNAME = "department";
    private static final String DEFAULT_DEPT_PASSWORD = "departmentpass";

    public DataInitializer(UserRepository userRepository, RoleRepository roleRepository,
                           SubjectRepository subjectRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.subjectRepository = subjectRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        createRolesIfNotFound();
        createSubjectsIfNotFound();
        createDepartmentUserIfNotFound();
    }

    private void createRolesIfNotFound() {
        createRoleIfNotFound("ROLE_TEACHER");
        createRoleIfNotFound("ROLE_STUDENT");
        createRoleIfNotFound("ROLE_DEPARTMENT");
    }

    private void createRoleIfNotFound(String roleName) {
        if (roleRepository.findByName(roleName).isEmpty()) {
            Role newRole = new Role(roleName);
            roleRepository.save(newRole);
            System.out.println("✅ Created and saved role: " + roleName);
        } else {
            System.out.println("ℹ️ Role " + roleName + " already exists.");
        }
    }

    private void createSubjectsIfNotFound() {
        List<Subject> subjects = Arrays.asList(
            new Subject("IT101", "Introduction to IT", 3, 0, 3, null, "MWF 8:30 AM - 9:30 AM R 10:30 AM - 12:30 PM"),
            new Subject("CS202", "Data Structures", 2, 1, 3, null, "MW 1:30 PM - 3:00 PM"),
            new Subject("MATE1", "College Algebra", 3, 0, 3, null, "TTh 9:30 AM - 11:00 AM"),
            new Subject("FIL12", "Filipino Subject", 3, 0, 3, null, "F 3:30 PM - 5:30 PM"),
            new Subject("PE301", "Physical Fitness", 2, 0, 2, null, "Sat 7:30 AM - 9:30 AM")
        );

        for (Subject subject : subjects) {
            if (subjectRepository.findByCode(subject.getCode()).isEmpty()) {
                subjectRepository.save(subject);
                System.out.println("✅ Created and saved subject: " + subject.getCode());
            } else {
                System.out.println("ℹ️ Subject " + subject.getCode() + " already exists.");
            }
        }
    }

    private void createDepartmentUserIfNotFound() {
        if (userRepository.findByUsername(DEFAULT_DEPT_USERNAME).isEmpty()) {
            Role deptRole = roleRepository.findByName("ROLE_DEPARTMENT")
                .orElseThrow(() -> new RuntimeException("Error: ROLE_DEPARTMENT is not found."));

            User deptUser = new User();
            deptUser.setUsername(DEFAULT_DEPT_USERNAME);
            deptUser.setEmail(DEFAULT_DEPT_EMAIL);
            deptUser.setPassword(passwordEncoder.encode(DEFAULT_DEPT_PASSWORD));
            deptUser.setRoles(new HashSet<>(Arrays.asList(deptRole)));
            deptUser.setEnabled(true);
            deptUser.setFirstName("Department");
            deptUser.setLastName("Admin");

            userRepository.save(deptUser);
            System.out.println("✅ Created and saved department user: " + DEFAULT_DEPT_USERNAME);
        } else {
            System.out.println("ℹ️ Department user " + DEFAULT_DEPT_USERNAME + " already exists.");
        }
    }
}