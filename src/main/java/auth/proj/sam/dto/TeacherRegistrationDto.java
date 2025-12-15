package auth.proj.sam.dto;

public class TeacherRegistrationDto {
    private String lastName;
    private String firstName;
    private String email;
    private String teacherId; 
    private String initialPassword;
    
    // The mock assignment fields (subject, sectionToAssign, studentIdToAssign) have been removed.

    // Getters and Setters
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTeacherId() { return teacherId; }
    public void setTeacherId(String teacherId) { this.teacherId = teacherId; }
    public String getInitialPassword() { return initialPassword; }
    public void setInitialPassword(String initialPassword) { this.initialPassword = initialPassword; }
}