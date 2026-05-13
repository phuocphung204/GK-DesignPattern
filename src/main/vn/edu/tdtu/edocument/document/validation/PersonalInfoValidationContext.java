package vn.edu.tdtu.edocument.document.validation;

public class PersonalInfoValidationContext {
    // thông tin người dùng
    public String name;
    public String email;
    public String phone;

    private PersonalInfoValidationContext(String name, String email, String phone) {
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    public static PersonalInfoValidationContext create(String name, String email, String phone) {
        return new PersonalInfoValidationContext(name, email, phone);
    }
}
