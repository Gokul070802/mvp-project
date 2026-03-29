package com.mvp.mvp_backend.model.dto;

import java.time.LocalDate;

public class ProfileDTO {

    private String firstName;
    private String lastName;
    private String gender;
    private LocalDate dob;
    private String country;
    private String profilePhoto;

    public ProfileDTO() {}

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public LocalDate getDob() { return dob; }
    public void setDob(LocalDate dob) { this.dob = dob; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getProfilePhoto() { return profilePhoto; }
    public void setProfilePhoto(String profilePhoto) { this.profilePhoto = profilePhoto; }
}