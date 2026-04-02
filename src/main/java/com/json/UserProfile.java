package com.json;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users")
public class UserProfile {
    @Id
    private String id;
    private String name;
    private String email;
    private String bio;
    private String phone;
    private String profileImage;

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getBio() { return bio; }
    public String getPhone() { return phone; }
    public String getProfileImage() { return profileImage; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setBio(String bio) { this.bio = bio; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setProfileImage(String profileImage) { this.profileImage = profileImage; }
}