package com.pilv.chat;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.firebase.firestore.FirebaseFirestore;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private EditText usernameInput;
    private TextView locationText;
    private Button saveButton, detectLocationButton;
    private ProgressBar progressBar;
    private FirebaseFirestore db;
    private String detectedCity = "";
    private String detectedCountry = "";

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize views
        usernameInput = findViewById(R.id.usernameInput);
        locationText = findViewById(R.id.locationText);
        saveButton = findViewById(R.id.saveButton);
        detectLocationButton = findViewById(R.id.detectLocationButton);
        progressBar = findViewById(R.id.progressBar);

        db = FirebaseFirestore.getInstance();

        // Load saved profile
        loadSavedProfile();

        // Detect Location Button
        detectLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                detectLocation();
            }
        });

        // Save Button
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProfile();
            }
        });
    }

    private void loadSavedProfile() {
        SharedPreferences prefs = getSharedPreferences("pilv_profile", MODE_PRIVATE);
        String username = prefs.getString("username", "");
        String location = prefs.getString("location", "📍 Unknown");

        if (!username.isEmpty()) {
            usernameInput.setText(username);
        }
        locationText.setText(location);
    }

    private void detectLocation() {
        // Check permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        detectLocationButton.setEnabled(false);

        // Use Android's LocationManager
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        try {
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location == null) {
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }

            if (location != null) {
                getLocationName(location);
            } else {
                progressBar.setVisibility(View.GONE);
                detectLocationButton.setEnabled(true);
                Toast.makeText(this,
                        "Unable to detect location. Please try again or enter manually.",
                        Toast.LENGTH_LONG).show();
            }
        } catch (SecurityException e) {
            progressBar.setVisibility(View.GONE);
            detectLocationButton.setEnabled(true);
            Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
        }
    }

    private void getLocationName(Location location) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(
                    location.getLatitude(), location.getLongitude(), 1);

            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                detectedCity = address.getLocality() != null ? address.getLocality() : "Unknown City";
                detectedCountry = address.getCountryName() != null ? address.getCountryName() : "Unknown Country";

                String locationString = "📍 " + detectedCity + ", " + detectedCountry;
                locationText.setText(locationString);
                Toast.makeText(ProfileActivity.this,
                        "✅ Location detected: " + detectedCity,
                        Toast.LENGTH_SHORT).show();
            } else {
                locationText.setText("📍 Unknown Location");
                Toast.makeText(this, "Could not determine location name", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Toast.makeText(this, "Error getting location: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            locationText.setText("📍 Unknown Location");
        }

        progressBar.setVisibility(View.GONE);
        detectLocationButton.setEnabled(true);
    }

    private void saveProfile() {
        String username = usernameInput.getText().toString().trim();

        if (username.isEmpty()) {
            Toast.makeText(this, "Please enter a username", Toast.LENGTH_SHORT).show();
            return;
        }

        if (username.length() < 3) {
            Toast.makeText(this, "Username must be at least 3 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        if (username.length() > 20) {
            Toast.makeText(this, "Username must be less than 20 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        saveButton.setEnabled(false);

        // Save to SharedPreferences
        SharedPreferences prefs = getSharedPreferences("pilv_profile", MODE_PRIVATE);
        String userId = prefs.getString("userId", "user_" + System.currentTimeMillis());

        prefs.edit()
                .putString("userId", userId)
                .putString("username", username)
                .putString("location", locationText.getText().toString())
                .apply();

        // Save to Firebase
        Map<String, Object> profile = new HashMap<>();
        profile.put("username", username);
        profile.put("location", locationText.getText().toString());
        profile.put("city", detectedCity);
        profile.put("country", detectedCountry);
        profile.put("createdAt", System.currentTimeMillis());

        db.collection("users").document(userId).set(profile)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    saveButton.setEnabled(true);
                    Toast.makeText(ProfileActivity.this,
                            "✅ Profile saved successfully!",
                            Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    saveButton.setEnabled(true);
                    Toast.makeText(ProfileActivity.this,
                            "❌ Error saving profile: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                detectLocation();
            } else {
                Toast.makeText(this,
                        "Location permission is needed to auto-detect your location.\nYou can enter it manually.",
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}