package com.atakmap.android.helloworld.compat;

import android.content.Context;
import android.location.Address;
import android.os.AsyncTask;

import com.atakmap.android.user.geocode.GeocodeManager;
import com.atakmap.coremap.maps.coords.GeoBounds;
import com.atakmap.coremap.maps.coords.GeoPoint;

import java.util.List;

/**
 * Drop-in replacement for ATAK's {@code GeocodingTask}, which existed in ATAK &lt;=5.0
 * but was REMOVED in 5.1+. It replicates that class's public API on top of
 * {@link GeocodeManager} (present and identical across every supported version:
 * {@code getSelectedGeocoder().getLocation(String, GeoBounds)} — byte-confirmed), so
 * the callers only change their import. Lives in {@code src/atakShared} (the ATAK
 * touch), keeping the shared source compiling on all versions.
 *
 * <p>This is the pragmatic compatibility-shim tier of the migration; a first-class
 * GeocodeCreator is the cleaner long-term home.
 */
public class GeocodingTaskCompat extends AsyncTask<Object, Void, List<Address>> {

    /** Same contract as the original {@code GeocodingTask.ResultListener}. */
    public interface ResultListener {
        void onResult();
    }

    private final Context context;
    private final GeoBounds bounds;
    private ResultListener listener;
    private GeoPoint point;
    private String humanAddress;

    public GeocodingTaskCompat(Context context, double south, double west,
            double north, double east, boolean unused) {
        this.context = context;
        this.bounds = new GeoBounds(south, west, north, east);
    }

    public GeocodingTaskCompat(Context context, double south, double west,
            double north, double east) {
        this(context, south, west, north, east, false);
    }

    public void setOnResultListener(ResultListener listener) {
        this.listener = listener;
    }

    public GeoPoint getPoint() {
        return point;
    }

    public String getHumanAddress() {
        return humanAddress;
    }

    @Override
    protected List<Address> doInBackground(Object... params) {
        String address = (params != null && params.length > 0 && params[0] != null)
                ? String.valueOf(params[0]) : null;
        if (address == null) {
            return null;
        }
        try {
            GeocodeManager.Geocoder geocoder =
                    GeocodeManager.getInstance(context).getSelectedGeocoder();
            if (geocoder == null) {
                return null;
            }
            return geocoder.getLocation(address, bounds);
        } catch (Throwable t) {
            return null;
        }
    }

    @Override
    protected void onPostExecute(List<Address> results) {
        if (results != null && !results.isEmpty()) {
            Address a = results.get(0);
            if (a.hasLatitude() && a.hasLongitude()) {
                point = new GeoPoint(a.getLatitude(), a.getLongitude());
                if (a.getMaxAddressLineIndex() >= 0) {
                    humanAddress = a.getAddressLine(0);
                }
            }
        }
        if (listener != null) {
            listener.onResult();
        }
    }
}
