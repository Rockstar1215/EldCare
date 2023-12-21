package com.example.old_man;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

// ... (existing imports)

public class EmergencyActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_LOCATION = 1001;
    private static final int PERMISSIONS_REQUEST_SMS = 1002;

    private LocationManager locationManager;
    private LocationListener locationListener;

    private ArrayList<String> emergencyContacts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency);

        // Add emergency contacts (phone numbers)
        emergencyContacts.add("9373019639");
        emergencyContacts.add("+917760943388");

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Check and request location permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSIONS_REQUEST_LOCATION);
            }
        }

        // Check and request SMS permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS},
                    PERMISSIONS_REQUEST_SMS);
        }

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                sendEmergencySMS(location);
            }
        };

        Button emergencyButton = findViewById(R.id.emergencyButton);
        emergencyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestLocationUpdates();
            }
        });
    }

    private void requestLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, null);
        }
    }

    private void sendEmergencySMS(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        // Create a Google Maps link with the user's location
        String mapLink = "http://maps.google.com/maps?q=" + latitude + "," + longitude;

        // Compose the message with the location link
        String message = "Emergency! I need help. My location: " + mapLink;

        for (String phoneNumber : emergencyContacts) {
            try {
                SmsManager.getDefault().sendTextMessage(phoneNumber, null, message, null, null);

                Log.d("EmergencyActivity", "SMS sent to " + phoneNumber);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("EmergencyActivity", "Error sending SMS to " + phoneNumber);
                // Handle SMS sending error
            }
        }

        Toast.makeText(this, "Emergency message sent!", Toast.LENGTH_SHORT).show();
    }



    // Add onRequestPermissionsResult method
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, handle accordingly
                    requestLocationUpdates();
                } else {
                    // Permission denied, handle accordingly
                    // You might want to inform the user that the location permission is required
                }
                break;

            case PERMISSIONS_REQUEST_SMS:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, handle accordingly
                } else {
                    // Permission denied, handle accordingly
                    // You might want to inform the user that the SMS permission is required
                }
                break;

            // Handle other permission requests if needed

            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
