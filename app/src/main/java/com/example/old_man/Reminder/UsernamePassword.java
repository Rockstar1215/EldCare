package com.example.old_man.Reminder;


import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "login")
public class UsernamePassword {

    @PrimaryKey
    @NonNull
    String usename;
    @NonNull
    String password;
    int isloggedIn = 0;

    @NonNull
    public String getUsename() {
        return usename;
    }

    public String getPassword() {
        return password;
    }

    public int getIsloggedIn() {
        return isloggedIn;
    }

    public void setUsename(@NonNull String usename) {
        this.usename = usename;
    }

    public void setPassword(@NonNull String password) {
        this.password = password;
    }

    public void setIsloggedIn(int isloggedIn) {
        this.isloggedIn = isloggedIn;
    }
}