package me.avelar.donee.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

public class PermissionHelper {

    public static final int REQUEST_PERMISSION_GPS = 12153;

    public static boolean checkForLocationPermission(Context context) {
        return ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestLocationPermission(Activity callbackActivity) {
        ActivityCompat.requestPermissions(callbackActivity, new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION_GPS);
    }



}
