package me.avelar.donee.model;

import com.google.gson.annotations.Expose;

public class LoginRequest {

    @Expose private String email;
    @Expose private String password;
    @Expose private String deviceImei;
    @Expose private String deviceModel;

    public LoginRequest(String email, String password,
                        String deviceImei, String deviceModel) {
        this.email = email;
        this.password = password;
        this.deviceImei = deviceImei;
        this.deviceModel = deviceModel;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public String getDeviceImei() {
        return deviceImei;
    }

    public LoginRequest setEmail(String email) {
        this.email = email;
        return this;
    }

    public LoginRequest setPassword(String password) {
        this.password = password;
        return this;
    }

    public LoginRequest setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
        return this;
    }

    public LoginRequest setDeviceImei(String deviceImei) {
        this.deviceImei = deviceImei;
        return this;
    }

}
