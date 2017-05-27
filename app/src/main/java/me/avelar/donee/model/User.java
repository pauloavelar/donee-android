package me.avelar.donee.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;

public class User implements Parcelable {

    @Expose private String id; // because of Javascript lack of 8bit integers
    @Expose private String name;
    @Expose private String email;
    @Expose private String account;
    @Expose private long   lastSynced;

    public User(String id, String name, String email, String account, long lastSynced) {
        this.id         = id;
        this.name       = name;
        this.email      = email;
        this.account    = account;
        this.lastSynced = lastSynced;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getAccount() {
        return account;
    }

    public long getLastSynced() {
        return lastSynced;
    }

    public User setLastSynced(long lastSynced) {
        this.lastSynced = lastSynced;
        return this;
    }

    public boolean equals(Object other) {
        return other != null && other instanceof User && ((User) other).getId().equals(this.id);
    }

    protected User(Parcel in) {
        id         = in.readString();
        name       = in.readString();
        email      = in.readString();
        account    = in.readString();
        lastSynced = in.readLong();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(email);
        dest.writeString(account);
        dest.writeLong(lastSynced);
    }

}