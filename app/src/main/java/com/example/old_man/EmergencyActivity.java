package com.example.old_man;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class EmergencyActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_LOCATION = 1001;
    private static final int PERMISSIONS_REQUEST_SMS = 1002;

    private LocationManager locationManager;
    private LocationListener locationListener;

    private Set<String> emergencyContactsSet;
    private ArrayList<String> emergencyContactsList;
    private EditText contactEditText;
    private EmergencyContactsAdapter emergencyContactsAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency);

        contactEditText = findViewById(R.id.contactEditText);
        ListView emergencyContactsListView = findViewById(R.id.emergencyContactsListView);
        loadEmergencyContacts();
        emergencyContactsAdapter = new EmergencyContactsAdapter(this, R.layout.list_item_emergency_contact, emergencyContactsList);
        emergencyContactsListView.setAdapter(emergencyContactsAdapter);

        // Set choice mode for multiple selection
        emergencyContactsListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        Button addContactButton = findViewById(R.id.addContactButton);
        addContactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addEmergencyContact();
            }
        });

        Button deleteContactButton = findViewById(R.id.deleteContactButton);
        deleteContactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteSelectedContacts(emergencyContactsListView);
            }
        });

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

    private void loadEmergencyContacts() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        emergencyContactsSet = prefs.getStringSet("emergencyContacts", new HashSet<>());
        emergencyContactsList = new ArrayList<>(emergencyContactsSet);
    }

    private void saveEmergencyContacts() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putStringSet("emergencyContacts", emergencyContactsSet);
        editor.apply();
    }

    private void addEmergencyContact() {
        String contactNumber = contactEditText.getText().toString().trim();
        if (!contactNumber.isEmpty() && !emergencyContactsSet.contains(contactNumber)) {
            emergencyContactsSet.add(contactNumber);
            emergencyContactsList.add(contactNumber);
            saveEmergencyContacts(); // Save the updated list
            updateEmergencyContactsUI(); // Update UI to show the added contact
            Toast.makeText(this, "Emergency contact added: " + contactNumber, Toast.LENGTH_SHORT).show();
            contactEditText.getText().clear();
        } else if (emergencyContactsSet.contains(contactNumber)) {
            Toast.makeText(this, "Emergency contact already exists: " + contactNumber, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Please enter a valid contact number", Toast.LENGTH_SHORT).show();
        }
    }
    private void deleteSelectedContacts(ListView listView) {
        // Get the selected positions from the adapter
        Set<Integer> selectedPositions = emergencyContactsAdapter.getSelectedPositions();

        if (!selectedPositions.isEmpty()) {
            // Iterate over the selected positions and remove contacts
            List<String> contactsToRemove = new ArrayList<>();
            for (Integer position : selectedPositions) {
                contactsToRemove.add(emergencyContactsList.get(position));
            }

            emergencyContactsSet.removeAll(contactsToRemove);
            emergencyContactsList.removeAll(contactsToRemove);

            saveEmergencyContacts();
            updateEmergencyContactsUI();
            listView.clearChoices();
        } else {
            Toast.makeText(this, "Please select contacts to delete", Toast.LENGTH_SHORT).show();
        }
    }



    private void updateEmergencyContactsUI() {
        emergencyContactsAdapter.notifyDataSetChanged(); // Notify the adapter that the data set has changed
    }

    private void requestLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, null);
        }
    }

    private void sendEmergencySMS(Location location) {
        if (!emergencyContactsSet.isEmpty()) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();

            // Create a Google Maps link with the user's location
            String mapLink = "http://maps.google.com/maps?q=" + latitude + "," + longitude;

            // Create a message with the location link
            String message = "Emergency! I need help. My location: " + mapLink;

            for (String phoneNumber : emergencyContactsSet) {
                try {
                    SmsManager.getDefault().sendTextMessage(phoneNumber, null, message, null, null);
                    Log.d("EmergencyActivity", "SMS sent to " + phoneNumber);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("EmergencyActivity", "Error sending SMS to " + phoneNumber + ": " + e.getMessage());
                    Toast.makeText(this, "Error sending SMS. Please try again later.", Toast.LENGTH_SHORT).show();
                }
            }

            Toast.makeText(this, "Emergency message sent!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "No emergency contacts available. Add contacts to send messages.", Toast.LENGTH_SHORT).show();
        }
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