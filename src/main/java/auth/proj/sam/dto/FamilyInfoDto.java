package auth.proj.sam.dto;

public class FamilyInfoDto {
    private String fatherName;
    private String motherName;
    private String parentContactNumber;

    // Getters and Setters
    public String getFatherName() { return fatherName; }
    public void setFatherName(String fatherName) { this.fatherName = fatherName; }
    public String getMotherName() { return motherName; }
    public void setMotherName(String motherName) { this.motherName = motherName; }
    public String getParentContactNumber() { return parentContactNumber; }
    public void setParentContactNumber(String parentContactNumber) { this.parentContactNumber = parentContactNumber; }
}