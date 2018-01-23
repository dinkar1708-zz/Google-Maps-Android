package com.google.maps.android;

import android.content.Context;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.nfc.Tag;
import android.util.Log;

/**
 * Created by dinakar.maurya on 23-01-2018.
 */

public class Utils {

    private static final String TAG = Utils.class.getSimpleName();

    /**
     * from google developers android console
     *
     * @param context
     * @return
     */
    public static boolean isConnectedToInternet(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnected();
        Log.i(TAG, " isConnectedToInternet "+isConnected);
        return isConnected;
    }

    /**
     * check if location services is enabled
     *
     * @param context
     * @return
     */
    public static boolean isLocationServiceEnabled(Context context) {
        boolean isGpsEnabled = false, isNetworkProviderEnabled = false;

        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        try {
            isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            Log.i(TAG, " gps enabled - " + isGpsEnabled);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            isNetworkProviderEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            Log.i(TAG, " network enabled - " + isNetworkProviderEnabled);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return isGpsEnabled || isNetworkProviderEnabled;

    }
}
