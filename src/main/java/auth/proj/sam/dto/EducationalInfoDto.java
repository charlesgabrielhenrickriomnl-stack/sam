package auth.proj.sam.dto;

public class EducationalInfoDto {
    // Academic Info
    private String applicationFor;
    private String classification;
    private String courseProgram;
    private String level;
    private String schoolYear;
    private String term;
    private String lrn;

    // Address Info
    private String unitNumber;
    private String street;
    private String barangay;
    private String city;
    private String zipCode;

    // Getters and Setters
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
    public String getUnitNumber() { return unitNumber; }
    public void setUnitNumber(String unitNumber) { this.unitNumber = unitNumber; }
    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }
    public String getBarangay() { return barangay; }
    public void setBarangay(String barangay) { this.barangay = barangay; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getZipCode() { return zipCode; }
    public void setZipCode(String zipCode) { this.zipCode = zipCode; }
}