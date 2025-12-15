package auth.proj.sam.dto;

public class VerifyPasswordChangeDto {
    private String token; // Token sent to email
    private String newPassword; // The password user wants to set
    private String code; // The verification code user enters

    // Getters and Setters
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
}