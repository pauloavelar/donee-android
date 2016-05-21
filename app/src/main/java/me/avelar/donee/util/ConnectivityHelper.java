package me.avelar.donee.util;

import android.content.Context;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public final class ConnectivityHelper {

    public static boolean isConnectedToInternet(Context c) {
        ConnectivityManager cm = (ConnectivityManager) getService(c, Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    public static boolean isLocationEnabled(Context c) {
        LocationManager lm = (LocationManager) getService(c, Context.LOCATION_SERVICE);
        boolean gpsEnabled = false, networkEnabled = false;

        try{
            gpsEnabled     = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
            networkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ignore) { }

        return gpsEnabled || networkEnabled;
    }

    private static Object getService(Context context, String serviceId) {
        return context.getSystemService(serviceId);
    }

}
