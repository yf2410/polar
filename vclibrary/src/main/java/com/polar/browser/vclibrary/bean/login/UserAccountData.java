package com.polar.browser.vclibrary.bean.login;

/**
 * Created by lzk-pc on 2017/3/31.
 */

public class UserAccountData {

    //Token
    private String token;  // 登录成功授权Token
    private String sId;   //账户唯一标识
    private String accountType; //登录方式
    //User info
    private String name;  //username
    private String gender;
    private String avatar;  //头像
    private int age;
    private String phoneNum; //user phone number
    private String email;
    private String locale;
    private int timeZone;
    private long avatarLastModified;
    private String birthday;
    private String nickName;

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getUsername() {
        return name;
    }

    public void setUsername(String username) {
        this.name = username;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public int getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(int timeZone) {
        this.timeZone = timeZone;
    }

    public long getAvatarLastModified() {
        if(avatarLastModified == 0){
            avatarLastModified = System.currentTimeMillis();
        }
        return avatarLastModified;
    }

    public void setAvatarLastModified(long avatarLastModified) {
        this.avatarLastModified = avatarLastModified;
    }

    public String getsId() {
        return sId;
    }

    public void setsId(String sId) {
        this.sId = sId;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("UserAccountData{");
        sb.append("token='").append(token).append('\'');
        sb.append("sId='").append(sId).append('\'');
        sb.append("accountType='").append(accountType).append('\'');
        sb.append(", username='").append(name).append('\'');
        sb.append(", gender='").append(gender).append('\'');
        sb.append(", avatar='").append(avatar).append('\'');
        sb.append(", age='").append(age).append('\'');
        sb.append(", phoneNum='").append(phoneNum).append('\'');
        sb.append(", email='").append(email).append('\'');
        sb.append(", locale='").append(locale).append('\'');
        sb.append(", timeZone=").append(timeZone).append('\'');
        sb.append(", avatarLastModified=").append(avatarLastModified);
        sb.append('}');
        return sb.toString();
    }
}
