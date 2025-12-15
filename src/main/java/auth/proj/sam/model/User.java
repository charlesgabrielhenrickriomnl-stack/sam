package auth.proj.sam.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "users")
public class User {

    // --- Enums for specific fields ---
    public enum AccountStatus { ACTIVE, BLOCKED, TIMED_OUT }
    public enum Gender { MALE, FEMALE }
    public enum Citizenship { FILIPINO, DUAL_CITIZENSHIP, FOREIGN }
    public enum CivilStatus { SINGLE, MARRIED, WIDOWED, SEPARATED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- Core Account Fields ---
    @Column(nullable = false, unique = true)
    private String username;
    @Column(nullable = false, unique = true)
    private String email;
    @Column(nullable = false)
    private String password;
    private boolean enabled;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus accountStatus = AccountStatus.ACTIVE;

    // --- Security & Verification Fields ---
    private String verificationCode;
    private LocalDateTime verificationCodeExpiryTime;
    private String passwordResetToken;
    private LocalDateTime passwordResetTokenExpiryTime;
    private boolean mfaEnabled;
    private String mfaSecret;
    private LocalDateTime timeoutUntil;
    
    // NEW: Temporary fields for in-session password change verification
    private String changePasswordToken;
    private String pendingNewPassword;

    // --- Registration Progress (NEW) ---
    private Integer registrationStep = 1; // 1: Initial, 2: Educational, 3: Family, 4: Other, 5: Complete
    
    // --- University & Personal Information Fields ---
    private String applicationFor;
    private String classification;
    private String courseProgram;
    private String level;
    private String schoolYear;
    private String term;
    private String lrn;
    private String lastName;
    private String firstName;
    private String middleName;
    private String displayName;
    private LocalDateTime lastDisplayNameChange;
    private LocalDate birthDate;
    private String birthPlace;
    @Enumerated(EnumType.STRING)
    private Gender gender;
    @Enumerated(EnumType.STRING)
    private Citizenship citizenship;
    private boolean dualCitizenship;
    private String religion;
    @Enumerated(EnumType.STRING)
    private CivilStatus civilStatus;
    private String cellphoneNumber;
    private String residenceNumber;
    private String facebook;
    private String twitter;
    
    // --- Family Information Fields (NEW) ---
    private String fatherName;
    private String motherName;
    private String parentContactNumber;
    
    // --- Address Info Fields ---
    private String unitNumber;
    private String street;
    private String barangay;
    private String city;
    private String country;
    private String zipCode;
    private String profileImagePath;

    // --- Relationships ---
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TrustedDevice> trustedDevices;

    public User() {
        this.enabled = false;
        this.mfaEnabled = false;
    }

    // --- GETTERS AND SETTERS for all fields (including new ones) ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public AccountStatus getAccountStatus() { return accountStatus; }
    public void setAccountStatus(AccountStatus accountStatus) { this.accountStatus = accountStatus; }
    public String getVerificationCode() { return verificationCode; }
    public void setVerificationCode(String verificationCode) { this.verificationCode = verificationCode; }
    public LocalDateTime getVerificationCodeExpiryTime() { return verificationCodeExpiryTime; }
    public void setVerificationCodeExpiryTime(LocalDateTime verificationCodeExpiryTime) { this.verificationCodeExpiryTime = verificationCodeExpiryTime; }
    public String getPasswordResetToken() { return passwordResetToken; }
    public void setPasswordResetToken(String passwordResetToken) { this.passwordResetToken = passwordResetToken; }
    public LocalDateTime getPasswordResetTokenExpiryTime() { return passwordResetTokenExpiryTime; }
    public void setPasswordResetTokenExpiryTime(LocalDateTime passwordResetTokenExpiryTime) { this.passwordResetTokenExpiryTime = passwordResetTokenExpiryTime; }
    public boolean isMfaEnabled() { return mfaEnabled; }
    public void setMfaEnabled(boolean mfaEnabled) { this.mfaEnabled = mfaEnabled; }
    public String getMfaSecret() { return mfaSecret; }
    public void setMfaSecret(String mfaSecret) { this.mfaSecret = mfaSecret; }
    public LocalDateTime getTimeoutUntil() { return timeoutUntil; }
    public void setTimeoutUntil(LocalDateTime timeoutUntil) { this.timeoutUntil = timeoutUntil; }
    
    // NEW GETTERS/SETTERS
    public String getChangePasswordToken() { return changePasswordToken; }
    public void setChangePasswordToken(String changePasswordToken) { this.changePasswordToken = changePasswordToken; }
    public String getPendingNewPassword() { return pendingNewPassword; }
    public void setPendingNewPassword(String pendingNewPassword) { this.pendingNewPassword = pendingNewPassword; }
    
    public Integer getRegistrationStep() { return registrationStep; }
    public void setRegistrationStep(Integer registrationStep) { this.registrationStep = registrationStep; }
    
    public String getApplicationFor() { return applicationFor; }
    public void setApplicationFor(String applicationFor) { this.applicationFor = applicationFor; }
    public String getClassification() { return classification; }
    public void setClassification(String classification) { this.classification = classification; }
    public String getCourseProgram() { return courseProgram; }
    public void setCourseProgram(String courseProgram) { this.courseProgram = courseProgram; }
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
    public String getSchoolYear() { return schoolYear; }
    public void setSchoolYear(String schoolYear) { this.schoolYear = schoolYear; }
    public String getTerm() { return term; }
    public void setTerm(String term) { this.term = term; }
    public String getLrn() { return lrn; }
    public void setLrn(String lrn) { this.lrn = lrn; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getMiddleName() { return middleName; }
    public void setMiddleName(String middleName) { this.middleName = middleName; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public LocalDateTime getLastDisplayNameChange() { return lastDisplayNameChange; }
    public void setLastDisplayNameChange(LocalDateTime lastDisplayNameChange) { this.lastDisplayNameChange = lastDisplayNameChange; }
    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }
    public String getBirthPlace() { return birthPlace; }
    public void setBirthPlace(String birthPlace) { this.birthPlace = birthPlace; }
    public Gender getGender() { return gender; }
    public void setGender(Gender gender) { this.gender = gender; }
    public Citizenship getCitizenship() { return citizenship; }
    public void setCitizenship(Citizenship citizenship) { this.citizenship = citizenship; }
    public boolean isDualCitizenship() { return dualCitizenship; }
    public void setDualCitizenship(boolean dualCitizenship) { this.dualCitizenship = dualCitizenship; }
    public String getReligion() { return religion; }
    public void setReligion(String religion) { this.religion = religion; }
    public CivilStatus getCivilStatus() { return civilStatus; }
    public void setCivilStatus(CivilStatus civilStatus) { this.civilStatus = civilStatus; }
    public String getCellphoneNumber() { return cellphoneNumber; }
    public void setCellphoneNumber(String cellphoneNumber) { this.cellphoneNumber = cellphoneNumber; }
    public String getResidenceNumber() { return residenceNumber; }
    public void setResidenceNumber(String residenceNumber) { this.residenceNumber = residenceNumber; }
    public String getFacebook() { return facebook; }
    public void setFacebook(String facebook) { this.facebook = facebook; }
    public String getTwitter() { return twitter; }
    public void setTwitter(String twitter) { this.twitter = twitter; }

    public String getFatherName() { return fatherName; }
    public void setFatherName(String fatherName) { this.fatherName = fatherName; }
    public String getMotherName() { return motherName; }
    public void setMotherName(String motherName) { this.motherName = motherName; }
    public String getParentContactNumber() { return parentContactNumber; }
    public void setParentContactNumber(String parentContactNumber) { this.parentContactNumber = parentContactNumber; }
    
    public String getUnitNumber() { return unitNumber; }
    public void setUnitNumber(String unitNumber) { this.unitNumber = unitNumber; }
    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }
    public String getBarangay() { return barangay; }
    public void setBarangay(String barangay) { this.barangay = barangay; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getZipCode() { return zipCode; }
    public void setZipCode(String zipCode) { this.zipCode = zipCode; }
    public String getProfileImagePath() { return profileImagePath; }
    public void setProfileImagePath(String profileImagePath) { this.profileImagePath = profileImagePath; }
    public Set<Role> getRoles() { return roles; }
    public void setRoles(Set<Role> roles) { this.roles = roles; }
    public List<TrustedDevice> getTrustedDevices() { return trustedDevices; }
    public void setTrustedDevices(List<TrustedDevice> trustedDevices) { this.trustedDevices = trustedDevices; }
}