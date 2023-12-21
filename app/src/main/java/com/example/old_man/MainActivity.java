package com.example.old_man;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.old_man.Reminder.MainPage;
import com.example.old_man.community.CommunityCall;

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
            }
        });

        medicineReminderCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle click for Medicine Reminder CardView
               openActivity(MainPage.class);
            }
        });

        emergencySosCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle click for Emergency SOS CardView
                openActivity(EmergencyActivity.class);
            }
        });

        magnifyingGlassCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle click for Magnifying Glass CardView
//                openActivity(MagnifyingGlassActivity.class);
            }
        });

        brainPuzzleCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle click for Brain Puzzle CardView
//                openActivity(BrainPuzzleActivity.class);
            }
        });

        medicineTrackingCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle click for Medicine Tracking CardView
//                openActivity(MedicineTrackingActivity.class);
            }
        });
    }

    private void openActivity(Class<?> activityClass) {
        Intent intent = new Intent(this, activityClass);
        startActivity(intent);

    }
}