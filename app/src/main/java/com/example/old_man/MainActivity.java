package com.example.old_man;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.old_man.Magnifier.Magnifier;
import com.example.old_man.Reminder.MainPage;
import com.example.old_man.community.CommunityCall;
import com.example.old_man.pedometer.pedometer;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Find the CardViews by their IDs
        CardView communityCard = findViewById(R.id.communityCard);
        CardView medicineReminderCard = findViewById(R.id.medicineReminderCard);
        CardView emergencySosCard = findViewById(R.id.emergencySosCard);
        CardView magnifyingGlassCard = findViewById(R.id.magnifyingGlassCard);
        CardView brainPuzzleCard = findViewById(R.id.brainPuzzleCard);
        CardView medicineTrackingCard = findViewById(R.id.medicineTrackingCard);

        // Set click listeners for each CardView
        communityCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle click for Community CardView
                openActivity(CommunityCall.class);
                vibrate();
            }
        });

        medicineReminderCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle click for Medicine Reminder CardView
                openActivity(MainPage.class);
                vibrate();
            }
        });

        emergencySosCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle click for Emergency SOS CardView
                openActivity(EmergencyActivity.class);
                vibrate();
            }
        });

        magnifyingGlassCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle click for Magnifying Glass CardView
                openActivity(Magnifier.class);
                vibrate();
            }
        });

        brainPuzzleCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle click for Brain Puzzle CardView
                openActivity(pedometer.class);
                vibrate();
            }
        });

        medicineTrackingCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle click for Medicine Tracking CardView
                // openActivity(MedicineTrackingActivity.class);
                vibrate();
            }
        });
    }

    private void openActivity(Class<?> activityClass) {
        Intent intent = new Intent(this, activityClass);
        startActivity(intent);
    }

    private void vibrate() {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        // Check if the device has a vibrator
        if (vibrator != null) {
            // Vibrate for 500 milliseconds (adjust duration as needed)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                // Deprecated in API 26
                vibrator.vibrate(500);
            }
        }
    }
}
