package vn.edu.tdtu.edocument.notification.core;

public class UserProfile {
    private String name;
    private String email;
    private String phone;
    private String role; // "cán bộ" hoặc "người nộp"

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getRole() {
        return role;
    }

    public UserProfile(String name, String email, String phone, String role) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.role = role;
    }

    public boolean isValidProfile() {
        return name != null && email != null && phone != null;
    }
}
