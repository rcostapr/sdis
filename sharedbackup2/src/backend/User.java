package backend;

import java.io.Serializable;

import utils.Encode;

public class User implements Serializable {

    private String userName, password;

    public User(String userName, String password) {
        this.userName = userName;
        this.password = Encode.byteArrayToHexString(password.getBytes());
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }

    public void setPassword(String password) {
        this.password = Encode.byteArrayToHexString(password.getBytes());
    }

    public boolean login(String userName, String password) {
        return this.userName.equals(userName) && Encode.byteArrayToHexString(password.getBytes()).equals(this
                .password);
    }

    public String getHashedPassword() {
        return password;
    }

    public void setHashedPassword(String password) {
        this.password = password;
    }


    @Override
    public boolean equals(Object obj) {
        return obj instanceof User && ((User) obj).getUserName().equals(userName);
    }
}
