package com.example.moto3

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final String TAG = MainActivity.class.getSimpleName();

    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LocationCallback mLocationCallback;
    private PolylineOptions mPolylineOptions;
    private Polyline mPolyline;
    private List<LatLng> mLatLngs;
    private float mDistance;

    private TextView mDistanceText;
    private Button mStartButton;
    private Button mStopButton;

    private boolean mIsRecording = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_main);

        mDistanceText = findViewById(R.id.distance_text);
        mStartButton = findViewById(R.id.start_button);
        mStopButton = findViewById(R.id.stop_button);

        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRecording();
            }
        });

        mStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRecording();
            }
        });

        // Check for location permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // Permission is granted
            initializeMap();
        }
    }

    @SuppressLint("MissingPermission")
    private void initializeMap() {
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        MapFragment mapFragment = (MapFragment) getFragmentManager()
            .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mPolylineOptions = new PolylineOptions();
        mLatLngs = new ArrayList<>();

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (mIsRecording) {
                    for (Location location : locationResult.getLocations()) {
                        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                        if (mLatLngs.size() == 0) {
                            mPolylineOptions.add(latLng);
                            mLatLngs.add(latLng);
                        } else {
                            LatLng lastLatLng = mLatLngs.get(mLatLngs.size() - 1);
                            float[] results = new float[1];
                            Location.distanceBetween(lastLatLng.latitude, lastLatLng.longitude,
                                latLng.latitude, latLng.longitude, results);
                            mDistance += results[0];
                            mDistanceText.setText(String.format("%.2f meters", mDistance));
                            mPolylineOptions.add(latLng);
                            mLatLngs.add(latLng);
                        }
                    }
                    mPolyline.setPoints(mLatLngs);
                }
            }
        };
    }

    private void startRecording() {
        mIsRecording = true;
        mStartButton.setEnabled(false);
        mStopButton.setEnabled(true);

        mDistanceText.setText("0 meters");

        mPolyline = mMap.addPolyline(mPolylineOptions);
        mPolyline.setColor(Color.BLUE);

        mFusedLocationProviderClient.requestLocationUpdates(LocationUtils.getLocationRequest(),
            mLocationCallback, Looper.getMainLooper());
    }

    private void stopRecording() {
        mIsRecording = false;
        mStartButton.setEnabled(true);
        mStopButton.setEnabled(false);

        mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeMap();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.zoomTo(18F));
    }
}
