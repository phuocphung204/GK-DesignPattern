package vn.edu.tdtu.edocument.document.model;

/**
 * UserPreference chứa các tùy chọn nhận thông báo của người dùng.<br/>
 * Nó bao gồm các thuộc tính để xác định người dùng có muốn nhận thông báo qua
 * email, SMS hay app push hay không.<br/>
 */
public class UserPreference {
    public boolean receiveEmail;
    public boolean receiveSms;
    public boolean receiveAppPush;

    public UserPreference(boolean receiveEmail, boolean receiveSms, boolean receiveAppPush) {
        this.receiveEmail = receiveEmail;
        this.receiveSms = receiveSms;
        this.receiveAppPush = receiveAppPush;
    }

    public static UserPreference defaultPreference() {
        return new UserPreference(true, true, false);
    }
}
