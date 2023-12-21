package com.example.old_man.Reminder;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.old_man.R;

public class My_Reminder extends AppCompatActivity {

    /**
     * i copied this project from git hub link is( https://github.com/vaibhavjain30699/Reminder-App/tree/master/app/src/main/java/com/example/reminderapp)
     * and after doing some changes i made it working now it is perfectly working and testing by me congratulations
     * */
    private EditText user, pass;
    private Button login;
    private TextView register;
    private AppDatabase appDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder_login);

        user = findViewById(R.id.editText);
        pass = findViewById(R.id.editText2);
        login = findViewById(R.id.button);
        register = findViewById(R.id.register);

        appDatabase = AppDatabase.geAppdatabase(My_Reminder.this);

        RoomDAO roomDAO = appDatabase.getRoomDAO();
        UsernamePassword temp = roomDAO.getLoggedInUser();
        if (temp != null) {
            Intent intent = new Intent(My_Reminder.this, MainPage.class);
            startActivity(intent);
            finish();
        }


        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser(user.getText().toString().trim(), pass.getText().toString().trim());
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(My_Reminder.this, Register.class);
                startActivity(intent);
                finish();
            }
        });

    }


    public void loginUser(String user, String pass) {

        RoomDAO roomDAO = appDatabase.getRoomDAO();
        UsernamePassword temp = roomDAO.getUserwithUsername(user);
        //Toast.makeText(this, temp.getPassword(), Toast.LENGTH_SHORT).show();
        if (temp == null) {
            Toast.makeText(My_Reminder.this, "Invalid username", Toast.LENGTH_SHORT).show();
        } else {
            if (temp.getPassword().equals(pass)) {
                temp.setIsloggedIn(1);
                roomDAO.Update(temp);
                AppDatabase.destroyInstance();
                Intent intent = new Intent(My_Reminder.this, MainPage.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(My_Reminder.this, "Invalid Password", Toast.LENGTH_SHORT).show();
            }
        }

    }
}