package com.google.maps.android;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final int ACCESS_FINE_LOCATION_CODE = 120;
    private static final String TAG = MapsActivity.class.getSimpleName();
    private GoogleMap googleMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if (!Utils.isConnectedToInternet(this)) {
            Toast.makeText(this, "Please connect to internet!", Toast.LENGTH_LONG).show();
        }

        if (!Utils.isLocationServiceEnabled(this)) {
            Toast.makeText(this, "Please enable location services!", Toast.LENGTH_LONG).show();
        }
        boolean permissionGranted = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;

        if (permissionGranted) {
            Log.i(TAG, "permissionGranted fine location");
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_FINE_LOCATION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case ACCESS_FINE_LOCATION_CODE: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "onRequestPermissionsResult fine location granted now...");
                }
            }
        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;

//        drawSimpleLocation();
        drawMultipleLocation();
    }

    /**
     * from google api
     */
    private void drawSimpleLocation() {
        // Add a marker in Sydney, Australia,
        // and move the map's camera to the same location.
        LatLng sydney = new LatLng(-33.852, 151.211);
        googleMap.addMarker(new MarkerOptions().position(sydney)
                .title("Marker in Sydney"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    private void drawMultipleLocation() {

        googleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                // Create bounds that include all locations of the map
                LatLngBounds.Builder boundsBuilder = LatLngBounds.builder();
                LatLngBounds.Builder builder = new LatLngBounds.Builder();

                ArrayList<LocationsData> locationsDatas = LocationsData.getData();
                for (LocationsData data : locationsDatas) {
                    MarkerOptions marker = new MarkerOptions().position(data.location).title(data.title);
                    marker.icon(data.bitmapDescriptor);
                    googleMap.addMarker(marker);
//
                    builder.include(data.location);
                }
                LatLngBounds bounds = builder.build();
                int padding = 150;
                //  Returns a CameraUpdate that transforms the camera such that the specified latitude/longitude
                // bounds are centered on screen at the greatest possible zoom level.
                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                googleMap.moveCamera(cu);
                int durationMs = 3 * 1000;
                // Moves the map according to the update with an animation over a specified duration,
                // and calls an optional callback on completion
                googleMap.animateCamera(CameraUpdateFactory.zoomTo(10), durationMs, null);
            }
        });

    }
}
