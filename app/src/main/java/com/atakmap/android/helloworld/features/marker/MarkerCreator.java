package com.atakmap.android.helloworld.features.marker;

import com.atakmap.android.helloworld.abstraction.Creator;

/**
 * Abstraction over ATAK marker placement ({@code Marker}, the point tool,
 * map groups, icons). One deep method: everything a demo placement needs
 * rides in the {@link MarkerSpec}; no ATAK type crosses the seam. Interface
 * in {@code src/main}; the implementation lives in {@code src/atakShared}.
 */
public interface MarkerCreator extends Creator {

    /**
     * Place the marker described by {@code spec} on the map.
     *
     * @return the placed marker's uid, or null if the map is not ready.
     */
    String placeMarker(MarkerSpec spec);

    /** Whether a marker with this uid is already on the map. */
    boolean markerExists(String uid);

    /**
     * Recolor the label text (ARGB) of an already-placed marker. The custom
     * type demo calls this ten seconds after placement — label styling is
     * live-mutable on the map, no refresh or re-add needed.
     *
     * @return true if the marker was found and recolored; false if the map
     *         is not ready or the uid does not resolve to a marker.
     */
    boolean setMarkerTextColor(String uid, int argb);

    /**
     * Move an existing marker to (latitude, longitude), deriving its track —
     * bearing from the previous point, speed from distance over the time
     * since the previous move (read from, and stored back under,
     * {@code lastUpdateMetaKey} as elapsed-realtime millis).
     *
     * @return true if a map item with {@code uid} is on the map — even a
     *         non-Marker item, which is left untouched but still suppresses
     *         re-placement (the ISS demo's legacy contract); false when
     *         nothing owns the uid (or the map isn't ready), so the caller
     *         should place the marker fresh.
     */
    boolean moveMarkerWithTrack(String uid, double latitude, double longitude,
            String lastUpdateMetaKey);
}
