package me.avelar.donee.web;

import com.google.gson.annotations.Expose;

@SuppressWarnings("unused")
public class SenderResponse {

    @Expose private boolean success;
    @Expose private String  message;

    public boolean isSuccessful() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
