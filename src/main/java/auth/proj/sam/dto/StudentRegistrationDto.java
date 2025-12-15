package auth.proj.sam.dto;

public class StudentRegistrationDto {

    // --- Step 1: Personal Info (Core Registration) ---
    private String lastName;
    private String firstName;
    private String middleName;
    private String email;
    // REMOVED: private String password;

    // --- GETTERS AND SETTERS for only the fields above ---

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getMiddleName() { return middleName; }
    public void setMiddleName(String middleName) { this.middleName = middleName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}