package auth.proj.sam.dto;

public class OtherInfoDto {
    // Remaining Personal Info
    private String birthDate;
    private String birthPlace;
    private String gender;
    private String citizenship;
    private boolean dualCitizenship;
    private String religion;
    private String civilStatus;
    private String cellphoneNumber;
    private String residenceNumber;
    private String facebook;
    private String twitter;

    // Getters and Setters
    public String getBirthDate() { return birthDate; }
    public void setBirthDate(String birthDate) { this.birthDate = birthDate; }
    public String getBirthPlace() { return birthPlace; }
    public void setBirthPlace(String birthPlace) { this.birthPlace = birthPlace; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getCitizenship() { return citizenship; }
    public void setCitizenship(String citizenship) { this.citizenship = citizenship; }
    public boolean isDualCitizenship() { return dualCitizenship; }
    public void setDualCitizenship(boolean dualCitizenship) { this.dualCitizenship = dualCitizenship; }
    public String getReligion() { return religion; }
    public void setReligion(String religion) { this.religion = religion; }
    public String getCivilStatus() { return civilStatus; }
    public void setCivilStatus(String civilStatus) { this.civilStatus = civilStatus; }
    public String getCellphoneNumber() { return cellphoneNumber; }
    public void setCellphoneNumber(String cellphoneNumber) { this.cellphoneNumber = cellphoneNumber; }
    public String getResidenceNumber() { return residenceNumber; }
    public void setResidenceNumber(String residenceNumber) { this.residenceNumber = residenceNumber; }
    public String getFacebook() { return facebook; }
    public void setFacebook(String facebook) { this.facebook = facebook; }
    public String getTwitter() { return twitter; }
    public void setTwitter(String twitter) { this.twitter = twitter; }
}