///*
//  Copyright 2017 Google Inc. All Rights Reserved.
//  <p>
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//  <p>
//  http://www.apache.org/licenses/LICENSE-2.0
//  <p>
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
// */
//
//package com.google.maps.android;
//
//import android.Manifest;
//import android.app.Activity;
//import android.content.Intent;
//import android.content.pm.PackageManager;
//import android.location.Location;
//import android.os.Bundle;
//import android.support.annotation.NonNull;
//import android.support.v4.app.ActivityCompat;
//import android.util.Log;
//import android.view.View;
//
//import com.google.android.gms.location.FusedLocationProviderClient;
//import com.google.android.gms.location.LocationCallback;
//import com.google.android.gms.location.LocationRequest;
//import com.google.android.gms.location.LocationResult;
//import com.google.android.gms.location.LocationServices;
//import com.google.android.gms.location.LocationSettingsRequest;
//import com.google.android.gms.location.SettingsClient;
//import com.google.android.gms.tasks.OnCompleteListener;
//import com.google.android.gms.tasks.Task;
//
//import java.text.DateFormat;
//import java.util.Date;
//
//
///**
// * Using location settings.
// * <p/>
// * Uses the {@link com.google.android.gms.location.SettingsApi} to ensure that the device's system
// * settings are properly configured for the app's location needs. When making a request to
// * Location services, the device's system settings may be in a state that prevents the app from
// * obtaining the location data that it needs. For example, GPS or Wi-Fi scanning may be switched
// * off. The {@code SettingsApi} makes it possible to determine if a device's system settings are
// * adequate for the location request, and to optionally invoke a dialog that allows the user to
// * enable the necessary settings.
// * <p/>
// * This sample allows the user to request location updates using the ACCESS_FINE_LOCATION setting
// * (as specified in AndroidManifest.xml).
// */
//public class LocationTracker {
//
//    private static final String TAG = LocationTracker.class.getSimpleName();
//
//    /**
//     * Code used in requesting runtime permissions.
//     */
//    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
//
//    /**
//     * Constant used in the location settings dialog.
//     */
//    private static final int REQUEST_CHECK_SETTINGS = 0x1;
//
//    /**
//     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
//     */
//    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
//
//    /**
//     * The fastest rate for active location updates. Exact. Updates will never be more frequent
//     * than this value.
//     */
//    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
//            UPDATE_INTERVAL_IN_MILLISECONDS / 2;
//
//    // Keys for storing activity state in the Bundle.
//    private final static String KEY_REQUESTING_LOCATION_UPDATES = "requesting-location-updates";
//    private final static String KEY_LOCATION = "location";
//    private final static String KEY_LAST_UPDATED_TIME_STRING = "last-updated-time-string";
//
//    /**
//     * Provides access to the Fused Location Provider API.
//     */
//    private FusedLocationProviderClient mFusedLocationClient;
//
//    /**
//     * Provides access to the Location Settings API.
//     */
//    private SettingsClient mSettingsClient;
//
//    /**
//     * Stores parameters for requests to the FusedLocationProviderApi.
//     */
//    private LocationRequest mLocationRequest;
//
//    /**
//     * Stores the types of location services the client is interested in using. Used for checking
//     * settings to determine if the device has optimal location settings.
//     */
//    private LocationSettingsRequest mLocationSettingsRequest;
//
//    /**
//     * Callback for Location events.
//     */
//    private LocationCallback mLocationCallback;
//
//    /**
//     * Represents a geographical location.
//     */
//    private Location mCurrentLocation;
//
//    // Labels.
//    private String mLatitudeLabel;
//    private String mLongitudeLabel;
//    private String mLastUpdateTimeLabel;
//
//    /**
//     * Tracks the status of the location updates request. Value changes when the user presses the
//     * Start Updates and Stop Updates buttons.
//     */
//    private Boolean mRequestingLocationUpdates;
//
//    /**
//     * Time when the location was updated represented as a String.
//     */
//    private String mLastUpdateTime;
//
//    public void onCreate(Bundle savedInstanceState) {
//        // Set labels.
//        mLatitudeLabel = getResources().getString(R.string.latitude_label);
//        mLongitudeLabel = getResources().getString(R.string.longitude_label);
//        mLastUpdateTimeLabel = getResources().getString(R.string.last_update_time_label);
//
//        mRequestingLocationUpdates = false;
//        mLastUpdateTime = "";
//
//        // Update values using data stored in the Bundle.
//        updateValuesFromBundle(savedInstanceState);
//
//        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
//        mSettingsClient = LocationServices.getSettingsClient(this);
//
//        // Kick off the process of building the LocationCallback, LocationRequest, and
//        // LocationSettingsRequest objects.
//        createLocationCallback();
//        createLocationRequest();
//        buildLocationSettingsRequest();
//
//    }
//
//    /**
//     * Updates fields based on data stored in the bundle.
//     *
//     * @param savedInstanceState The activity state saved in the Bundle.
//     */
//    private void updateValuesFromBundle(Bundle savedInstanceState) {
//        if (savedInstanceState != null) {
//            // Update the value of mRequestingLocationUpdates from the Bundle, and make sure that
//            // the Start Updates and Stop Updates buttons are correctly enabled or disabled.
//            if (savedInstanceState.keySet().contains(KEY_REQUESTING_LOCATION_UPDATES)) {
//                mRequestingLocationUpdates = savedInstanceState.getBoolean(
//                        KEY_REQUESTING_LOCATION_UPDATES);
//            }
//
//            // Update the value of mCurrentLocation from the Bundle and update the UI to show the
//            // correct latitude and longitude.
//            if (savedInstanceState.keySet().contains(KEY_LOCATION)) {
//                // Since KEY_LOCATION was found in the Bundle, we can be sure that mCurrentLocation
//                // is not null.
//                mCurrentLocation = savedInstanceState.getParcelable(KEY_LOCATION);
//            }
//
//            // Update the value of mLastUpdateTime from the Bundle and update the UI.
//            if (savedInstanceState.keySet().contains(KEY_LAST_UPDATED_TIME_STRING)) {
//                mLastUpdateTime = savedInstanceState.getString(KEY_LAST_UPDATED_TIME_STRING);
//            }
//            updateUI();
//        }
//    }
//
//    /**
//     * Sets up the location request. Android has two location request settings:
//     * {@code ACCESS_COARSE_LOCATION} and {@code ACCESS_FINE_LOCATION}. These settings control
//     * the accuracy of the current location. This sample uses ACCESS_FINE_LOCATION, as defined in
//     * the AndroidManifest.xml.
//     * <p/>
//     * When the ACCESS_FINE_LOCATION setting is specified, combined with a fast update
//     * interval (5 seconds), the Fused Location Provider API returns location updates that are
//     * accurate to within a few feet.
//     * <p/>
//     * These settings are appropriate for mapping applications that show real-time location
//     * updates.
//     */
//    private void createLocationRequest() {
//        mLocationRequest = new LocationRequest();
//
//        // Sets the desired interval for active location updates. This interval is
//        // inexact. You may not receive updates at all if no location sources are available, or
//        // you may receive them slower than requested. You may also receive updates faster than
//        // requested if other applications are requesting location at a faster interval.
//        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
//
//        // Sets the fastest rate for active location updates. This interval is exact, and your
//        // application will never receive updates faster than this value.
//        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
//
//        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//    }
//
//    /**
//     * Creates a callback for receiving location events.
//     */
//    private void createLocationCallback() {
//        mLocationCallback = new LocationCallback() {
//            @Override
//            public void onLocationResult(LocationResult locationResult) {
//                super.onLocationResult(locationResult);
//
//                mCurrentLocation = locationResult.getLastLocation();
//                mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
//                updateLocationUI();
//            }
//        };
//    }
//
//    /**
//     * Uses a {@link LocationSettingsRequest.Builder} to build
//     * a {@link LocationSettingsRequest} that is used for checking
//     * if a device has the needed location settings.
//     */
//    private void buildLocationSettingsRequest() {
//        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
//        builder.addLocationRequest(mLocationRequest);
//        mLocationSettingsRequest = builder.build();
//    }
//
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        switch (requestCode) {
//            // Check for the integer request code originally supplied to startResolutionForResult().
//            case REQUEST_CHECK_SETTINGS:
//                switch (resultCode) {
//                    case Activity.RESULT_OK:
//                        Log.i(TAG, "User agreed to make required location settings changes.");
//                        // Nothing to do. startLocationupdates() gets called in onResume again.
//                        break;
//                    case Activity.RESULT_CANCELED:
//                        Log.i(TAG, "User chose not to make required location settings changes.");
//                        mRequestingLocationUpdates = false;
//                        updateUI();
//                        break;
//                }
//                break;
//        }
//    }
//
//    /**
//     * Handles the Start Updates button and requests start of location updates. Does nothing if
//     * updates have already been requested.
//     */
//    public void startUpdatesButtonHandler(View view) {
//        if (!mRequestingLocationUpdates) {
//            mRequestingLocationUpdates = true;
////            startLocationUpdates();
//        }
//    }
//
//    /**
//     * Handles the Stop Updates button, and requests removal of location updates.
//     */
//    public void stopUpdatesButtonHandler(View view) {
//        // It is a good practice to remove location requests when the activity is in a paused or
//        // stopped state. Doing so helps battery performance and is especially
//        // recommended in applications that request frequent location updates.
//        stopLocationUpdates();
//    }
//
//    /**
//     * Requests location updates from the FusedLocationApi. Note: we don't call this unless location
//     * runtime permission has been granted.
//     */
//   /* private void startLocationUpdates() {
//        // Begin by checking if the device has the necessary location settings.
//        mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
//                .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
//                    @Override
//                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
//                        Log.i(TAG, "All location settings are satisfied.");
//
//                        //noinspection MissingPermission
//                        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
//                                mLocationCallback, Looper.myLooper());
//
//                        updateUI();
//                    }
//                })
//                .addOnFailureListener(this, new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        int statusCode = ((ApiException) e).getStatusCode();
//                        switch (statusCode) {
//                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
//                                Log.i(TAG, "Location settings are not satisfied. Attempting to upgrade " +
//                                        "location settings ");
//                                try {
//                                    // Show the dialog by calling startResolutionForResult(), and check the
//                                    // result in onActivityResult().
//                                    ResolvableApiException rae = (ResolvableApiException) e;
////                                    rae.startResolutionForResult(LocationTracker.this, REQUEST_CHECK_SETTINGS);
//                                } catch (Exception sie) {
//                                    Log.i(TAG, "PendingIntent unable to execute request.");
//                                }
//                                break;
//                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
//                                String errorMessage = "Location settings are inadequate, and cannot be " +
//                                        "fixed here. Fix in Settings.";
//                                Log.e(TAG, errorMessage);
////                                Toast.makeText(LocationTracker.this, errorMessage, Toast.LENGTH_LONG).show();
//                                mRequestingLocationUpdates = false;
//                        }
//
//                        updateUI();
//                    }
//                });
//    }*/
//
//    /**
//     * Updates all UI fields.
//     */
//    private void updateUI() {
//        updateLocationUI();
//    }
//
//    /**
//     * Sets the value of the UI fields for the location latitude, longitude and last update time.
//     */
//    private void updateLocationUI() {
//        if (mCurrentLocation != null) {
//        }
//    }
//
//    /**
//     * Removes location updates from the FusedLocationApi.
//     */
//    private void stopLocationUpdates() {
//        if (!mRequestingLocationUpdates) {
//            Log.d(TAG, "stopLocationUpdates: updates never requested, no-op.");
//            return;
//        }
//
//        // It is a good practice to remove location requests when the activity is in a paused or
//        // stopped state. Doing so helps battery performance and is especially
//        // recommended in applications that request frequent location updates.
//       /* mFusedLocationClient.removeLocationUpdates(mLocationCallback)
//                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Void> task) {
//                        mRequestingLocationUpdates = false;
////                        setButtonsEnabledState();
//                    }
//                });*/
//    }
//
//    public void onResume() {
//        // Within {@code onPause()}, we remove location updates. Here, we resume receiving
//        // location updates if the user has requested them.
//        if (mRequestingLocationUpdates && checkPermissions()) {
////            startLocationUpdates();
//        } else if (!checkPermissions()) {
////            requestPermissions();
//        }
//
//        updateUI();
//
//        startUpdatesButtonHandler(null);
//    }
//
//    protected void onPause() {
//        // Remove location updates to save battery.
//        stopLocationUpdates();
//    }
//
//    /**
//     * Stores activity data in the Bundle.
//     */
//    public void onSaveInstanceState(Bundle savedInstanceState) {
//        savedInstanceState.putBoolean(KEY_REQUESTING_LOCATION_UPDATES, mRequestingLocationUpdates);
//        savedInstanceState.putParcelable(KEY_LOCATION, mCurrentLocation);
//        savedInstanceState.putString(KEY_LAST_UPDATED_TIME_STRING, mLastUpdateTime);
//    }
//
//    /**
//     * Return the current state of the permissions needed.
//     */
//    private boolean checkPermissions() {
//        int permissionState = ActivityCompat.checkSelfPermission(this,
//                Manifest.permission.ACCESS_FINE_LOCATION);
//        return permissionState == PackageManager.PERMISSION_GRANTED;
//    }
//
//}
