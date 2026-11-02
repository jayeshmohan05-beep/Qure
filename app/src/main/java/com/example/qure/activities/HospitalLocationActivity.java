package com.example.qure.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.example.qure.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;

public class HospitalLocationActivity extends AppCompatActivity implements OnMapReadyCallback {

    private ImageButton buttonBack;
    private MaterialButton buttonGetDirections;

    // Dummy Hospital Coordinates (NYC)
    private final double HOSP_LAT = 30.81839;
    private final double HOSP_LNG = 76.74245;
    private final String HOSP_NAME = "Homi Bhabha Cancer Hospital and Research Centre (New Chandigarh)";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hospital_location);

        buttonBack = findViewById(R.id.buttonBack);
        buttonGetDirections = findViewById(R.id.buttonGetDirections);

        buttonBack.setOnClickListener(v -> finish());

        // Launch external Google Maps App for turn-by-turn navigation
        buttonGetDirections.setOnClickListener(v -> {
            String uri = "google.navigation:q=" + HOSP_LAT + "," + HOSP_LNG;
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            intent.setPackage("com.google.android.apps.maps");
            
            // If Google Maps is installed, open it. Otherwise, open in browser.
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, 
                        Uri.parse("https://www.google.com/maps/dir/?api=1&destination=" + HOSP_LAT + "," + HOSP_LNG));
                startActivity(browserIntent);
            }
        });

        // Initialize Embedded Map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * NOTE: This will only show visual map tiles if a valid API Key is provided in the Manifest.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLng hospitalLocation = new LatLng(HOSP_LAT, HOSP_LNG);
        
        // Add a red marker
        googleMap.addMarker(new MarkerOptions()
                .position(hospitalLocation)
                .title(HOSP_NAME));
                
        // Zoom camera in on the hospital
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(hospitalLocation, 15f));
    }
}
