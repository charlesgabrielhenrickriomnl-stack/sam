package auth.proj.sam.service;

import auth.proj.sam.dto.ChangePasswordDto;
import auth.proj.sam.dto.VerifyPasswordChangeDto;
import auth.proj.sam.dto.EducationalInfoDto;
import auth.proj.sam.dto.FamilyInfoDto;
import auth.proj.sam.dto.OtherInfoDto;
import auth.proj.sam.dto.StudentRegistrationDto;
import auth.proj.sam.dto.TeacherRegistrationDto;
import auth.proj.sam.model.Role;
import auth.proj.sam.model.User;
import auth.proj.sam.repository.RoleRepository;
import auth.proj.sam.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    
    // --- STATIC/TEMPORARY COUNTER FOR ID GENERATION ---
    private static final String STUDENT_ID_PREFIX = "22-1-";
    // FIX: Starting the student ID counter at 1000 to avoid duplicates (22-1-00001, etc.)
    private static final AtomicInteger STUDENT_ID_COUNTER = new AtomicInteger(2000); 
    private static final AtomicInteger TEACHER_ID_COUNTER = new AtomicInteger(50000); 

    // --- Template ID Configuration (Reading from application.properties/Env Vars) ---
    @Value("${brevo.template.verification-id:}")
    private Long verificationTemplateId;

    @Value("${brevo.template.password-reset-id:}")
    private Long passwordResetTemplateId;
    
    // UPDATED: Now injected from config
    @Value("${brevo.template.student-credentials-id:}") 
    private Long studentCredentialsTemplateId; 
    
    // UPDATED: Now injected from config
    @Value("${brevo.template.teacher-credentials-id:}")
    private Long teacherCredentialsTemplateId;
    
    private static final Long PASSWORD_CHANGE_TEMPLATE_ID = 6L; 
    
    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, EmailService emailService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }
    
    private String generateStudentId() {
        return String.format(STUDENT_ID_PREFIX + "%05d", STUDENT_ID_COUNTER.getAndIncrement());
    }
    
    private String generateTeacherId() {
        return String.format(STUDENT_ID_PREFIX + "%05d", TEACHER_ID_COUNTER.getAndIncrement());
    }

    // --- NEW: Method to count users by role ---
    public long countUsersByRole(String roleName) {
        return userRepository.findAll().stream()
                .filter(user -> user.getRoles().stream()
                        .anyMatch(role -> role.getName().equals(roleName)))
                .count();
    }
    
    // --- NEW: CHANGE PASSWORD REQUEST LOGIC (Step 1: Initiate) ---
    public String initiatePasswordChange(User user, ChangePasswordDto dto) {
        // 1. Validate new password matches confirmation
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new RuntimeException("New password and confirmation password do not match.");
        }
        
        // 2. Verify current password
        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Incorrect current password.");
        }

        // 3. Generate a token (used as the session identifier) and a code (sent to email)
        String token = UUID.randomUUID().toString();
        String code = String.format("%06d", new Random().nextInt(999999));
        
        // 4. Save token, code, and pending password (encoded)
        user.setChangePasswordToken(token);
        user.setVerificationCode(code); // Re-use verification code field
        user.setVerificationCodeExpiryTime(LocalDateTime.now().plusMinutes(10)); // 10 minute expiry
        user.setPendingNewPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);

        // 5. Send email with the verification code
        Map<String, String> params = new HashMap<>();
        params.put("code", code);
        params.put("firstName", user.getFirstName());

        // Cleaned up logic to rely on @Async and log only if ID is missing
        if (PASSWORD_CHANGE_TEMPLATE_ID > 0) {
             emailService.sendEmail(user.getEmail(), PASSWORD_CHANGE_TEMPLATE_ID, params);
        } else {
            System.out.println("Email change verification code for " + user.getEmail() + " is: " + code);
        }
        
        return token; // Return the token for the controller to use in the flash attribute
    }
    
    // --- NEW: CHANGE PASSWORD FINALIZATION LOGIC (Step 2: Finalize) ---
    public void finalizePasswordChange(User user, VerifyPasswordChangeDto dto) {
        
        // Check if the code matches and is not expired
        if (user.getVerificationCode() == null || !user.getVerificationCode().equals(dto.getCode())) {
            throw new RuntimeException("Invalid verification code.");
        }
        
        if (user.getVerificationCodeExpiryTime() == null || user.getVerificationCodeExpiryTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Verification code has expired.");
        }
        
        // 1. Update password using the pre-encoded value
        if (user.getPendingNewPassword() == null) {
            throw new RuntimeException("No pending password found. Please restart the change process.");
        }
        user.setPassword(user.getPendingNewPassword());

        // 2. Clear all temporary fields
        user.setChangePasswordToken(null);
        user.setVerificationCode(null);
        user.setVerificationCodeExpiryTime(null);
        user.setPendingNewPassword(null);
        
        userRepository.save(user);
    }
    
    // --- EXISTING: TEACHER REGISTRATION (NOW USES TEACHER_CREDENTIALS_TEMPLATE_ID @Value) ---
    public void registerTeacher(TeacherRegistrationDto dto) {
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new RuntimeException("An account with this email already exists.");
        }
        
        String username = (dto.getTeacherId() != null && !dto.getTeacherId().isEmpty()) ? dto.getTeacherId() : generateTeacherId();
        
        if (userRepository.findByUsername(username).isPresent()) {
             throw new RuntimeException("Teacher ID " + username + " is already taken.");
        }
        
        String password = (dto.getInitialPassword() != null && !dto.getInitialPassword().isEmpty()) 
            ? dto.getInitialPassword() 
            : dto.getLastName();
        
        User user = new User();
        user.setEmail(dto.getEmail());
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setDisplayName(user.getFirstName() + " " + user.getLastName().charAt(0) + "."); 
        
        Role teacherRole = roleRepository.findByName("ROLE_TEACHER")
                .orElseThrow(() -> new RuntimeException("Error: ROLE_TEACHER is not found."));
        user.getRoles().add(teacherRole);
        user.setEnabled(true);
        user.setAccountStatus(User.AccountStatus.ACTIVE);
        user.setRegistrationStep(5);
        
        userRepository.save(user);
        
        // REMOVED THE CODE BLOCK FOR MOCK ASSIGNMENT LOGIC HERE

        Map<String, String> params = new HashMap<>();
        params.put("teacherId", user.getUsername());
        params.put("password", password);
        params.put("firstName", user.getFirstName());
        
        // UPDATED: Use the new injected template ID
        if (teacherCredentialsTemplateId != null && teacherCredentialsTemplateId > 0) {
             emailService.sendEmail(user.getEmail(), teacherCredentialsTemplateId, params);
        } else {
             System.out.println("Credentials email disabled. Teacher ID: " + user.getUsername() + " / Initial Password: " + password + " sent to " + user.getEmail());
        }
        
        // UPDATED LOGGING: Removed mock assignment output
        System.out.println("✅ Teacher account " + username + " created successfully. Assignment must be done via Manage Teachers.");

    }
    
    // --- EXISTING: STUDENT REGISTRATION ---
    public String registerStudent(StudentRegistrationDto studentDto) {
        if (userRepository.findByEmail(studentDto.getEmail()).isPresent()) {
            throw new RuntimeException("An account with this email already exists.");
        }

        User user = new User();
        
        String studentId = generateStudentId();
        String initialPassword = studentDto.getLastName(); 
        
        user.setEmail(studentDto.getEmail());
        user.setUsername(studentId);
        user.setPassword(passwordEncoder.encode(initialPassword));

        Role studentRole = roleRepository.findByName("ROLE_STUDENT")
                .orElseThrow(() -> new RuntimeException("Error: ROLE_STUDENT is not found."));
        user.getRoles().add(studentRole);

        user.setLastName(studentDto.getLastName());
        user.setFirstName(studentDto.getFirstName());
        user.setMiddleName(studentDto.getMiddleName());
        
        user.setEnabled(false);
        user.setAccountStatus(User.AccountStatus.ACTIVE);
        user.setRegistrationStep(2);

        String code = String.format("%06d", new Random().nextInt(999999));
        user.setVerificationCode(code);
        user.setVerificationCodeExpiryTime(LocalDateTime.now().plusMinutes(15));

        userRepository.save(user);

        Map<String, String> params = new HashMap<>();
        params.put("username", user.getUsername()); 
        params.put("verificationCode", code);

        if (verificationTemplateId != null && verificationTemplateId > 0) {
             emailService.sendEmail(user.getEmail(), verificationTemplateId, params);
        } else {
            System.out.println("Email sending is disabled. Verification code for " + user.getEmail() + " is: " + code);
            // Updated to NOT display the password
            System.out.println("Generated Student ID: " + studentId + ". Initial password is NOT displayed in console for security.");
        }
        
        return user.getUsername();
    }
    
    public void updateEducationalInfo(User user, EducationalInfoDto dto) {
        user.setApplicationFor(dto.getApplicationFor());
        user.setClassification(dto.getClassification());
        user.setCourseProgram(dto.getCourseProgram());
        user.setLevel(dto.getLevel());
        user.setSchoolYear(dto.getSchoolYear());
        user.setTerm(dto.getTerm());
        user.setLrn(dto.getLrn());
        
        user.setUnitNumber(dto.getUnitNumber());
        user.setStreet(dto.getStreet());
        user.setBarangay(dto.getBarangay());
        user.setCity(dto.getCity());
        user.setZipCode(dto.getZipCode());
        user.setCountry("PHILIPPINES"); 

        user.setRegistrationStep(3);
        userRepository.save(user);
    }
    
    public void updateFamilyInfo(User user, FamilyInfoDto dto) {
        user.setFatherName(dto.getFatherName());
        user.setMotherName(dto.getMotherName());
        user.setParentContactNumber(dto.getParentContactNumber());

        user.setRegistrationStep(4);
        userRepository.save(user);
    }
    
    public void updateOtherInfo(User user, OtherInfoDto dto) {
        user.setBirthPlace(dto.getBirthPlace());
        user.setReligion(dto.getReligion());
        user.setCellphoneNumber(dto.getCellphoneNumber());
        user.setResidenceNumber(dto.getResidenceNumber());
        user.setFacebook(dto.getFacebook());
        user.setTwitter(dto.getTwitter());
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        if (dto.getBirthDate() != null && !dto.getBirthDate().isEmpty()) {
            user.setBirthDate(LocalDate.parse(dto.getBirthDate(), formatter));
        }

        if (dto.getGender() != null) user.setGender(User.Gender.valueOf(dto.getGender().toUpperCase()));
        if (dto.getCitizenship() != null) user.setCitizenship(User.Citizenship.valueOf(dto.getCitizenship().toUpperCase()));
        if (dto.getCivilStatus() != null) user.setCivilStatus(User.CivilStatus.valueOf(dto.getCivilStatus().toUpperCase()));
        user.setDualCitizenship(dto.isDualCitizenship());
        
        user.setRegistrationStep(5);
        userRepository.save(user);
    }
    
    // --- EXISTING: STUDENT VERIFICATION (NOW USES STUDENT_CREDENTIALS_TEMPLATE_ID @Value) ---
    public boolean verifyUser(String username, String code) {
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        
        if (user.getVerificationCode() != null && user.getVerificationCode().equals(code) &&
            user.getVerificationCodeExpiryTime().isAfter(LocalDateTime.now())) {

            user.setEnabled(true);
            user.setVerificationCode(null);
            user.setVerificationCodeExpiryTime(null);
            userRepository.save(user);

            String initialPassword = user.getLastName();
            
            Map<String, String> params = new HashMap<>();
            params.put("studentId", user.getUsername());
            params.put("password", initialPassword);
            params.put("firstName", user.getFirstName());
            
            // UPDATED: Use the new injected template ID
            if (studentCredentialsTemplateId != null && studentCredentialsTemplateId > 0) {
                 emailService.sendEmail(user.getEmail(), studentCredentialsTemplateId, params);
            } else {
                 System.out.println("Credentials email disabled. Student ID: " + user.getUsername() + " / Initial Password: " + initialPassword + " sent to " + user.getEmail());
            }

            return true;
        }
        return false;
    }

    public void registerUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        Role userRole = roleRepository.findByName("ROLE_STUDENT").orElseThrow(() -> new RuntimeException("Error: Role is not found."));
        user.getRoles().add(userRole);

        String code = String.format("%06d", new Random().nextInt(999999));
        user.setVerificationCode(code);
        user.setVerificationCodeExpiryTime(LocalDateTime.now().plusMinutes(15));

        userRepository.save(user);

        Map<String, String> params = new HashMap<>();
        params.put("username", user.getUsername());
        params.put("verificationCode", code);

        if (verificationTemplateId != null && verificationTemplateId > 0) {
            emailService.sendEmail(user.getEmail(), verificationTemplateId, params);
        }
    }

    public void generatePasswordResetToken(String email, String siteURL) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User with this email not found"));

        String token = UUID.randomUUID().toString();
        user.setPasswordResetToken(token);
        user.setPasswordResetTokenExpiryTime(LocalDateTime.now().plusHours(1));
        userRepository.save(user);

        String resetURL = siteURL + "/reset-password?token=" + token;
        Map<String, String> params = new HashMap<>();
        params.put("username", user.getUsername());
        params.put("reset_url", resetURL);

        if (passwordResetTemplateId != null && passwordResetTemplateId > 0) {
            emailService.sendEmail(user.getEmail(), passwordResetTemplateId, params);
        }
    }

    public boolean validatePasswordResetToken(String token) {
        User user = userRepository.findByPasswordResetToken(token);
        return user != null && user.getPasswordResetTokenExpiryTime().isAfter(LocalDateTime.now());
    }

    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findByPasswordResetToken(token);
        if (user == null || user.getPasswordResetTokenExpiryTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Invalid or expired password reset token.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiryTime(null);
        userRepository.save(user);
    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    public void blockUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        user.setAccountStatus(User.AccountStatus.BLOCKED);
        userRepository.save(user);
    }

    public void unblockUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        user.setAccountStatus(User.AccountStatus.ACTIVE);
        user.setTimeoutUntil(null);
        userRepository.save(user);
    }

    public void timeoutUser(Long userId, int minutes) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        user.setAccountStatus(User.AccountStatus.TIMED_OUT);
        user.setTimeoutUntil(LocalDateTime.now().plusMinutes(minutes));
        userRepository.save(user);
    }

    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }
}