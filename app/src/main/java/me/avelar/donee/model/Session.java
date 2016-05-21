package me.avelar.donee.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;

import java.util.Date;

public class Session implements Parcelable {

    @Expose private boolean status;
    @Expose private String id;
    @Expose private User user;
    @Expose private long lastUsed;

    public Date getLastUsed() {
        return new Date(lastUsed);
    }

    public void setLastUsed(Date lastUsed) {
        this.lastUsed = lastUsed.getTime();
    }

    public Session(String id, User user) {
        this.id   = id;
        this.user = user;
    }

    public boolean isValid() {
        return status;
    }

    @SuppressWarnings("unused")
    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    @SuppressWarnings("unused")
    public void setUser(User user) {
        this.user = user;
    }

    protected Session(Parcel in) {
        id   = in.readString();
        user = in.readParcelable(User.class.getClassLoader());
    }

    public static final Creator<Session> CREATOR = new Creator<Session>() {
        @Override
        public Session createFromParcel(Parcel in) {
            return new Session(in);
        }
        @Override
        public Session[] newArray(int size) {
            return new Session[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeParcelable(user, flags);
    }

}
