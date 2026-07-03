package com.atakmap.android.helloworld.features.lrf;

/**
 * Callback port for laser-range-finder readings: the plugin-owned listener a
 * Controller implements to receive them. The impl adapts the plugin's
 * {@code LRFPointTool.LRFCallback} onto it, computing the "point from self"
 * projection on the ATAK side (it needs {@code GeoCalculations} and the self
 * marker) so only primitives and a display string cross the boundary.
 */
public interface LrfPort {

    /**
     * A range-finder reading arrived while the LRF point tool was armed. May
     * be delivered off the UI thread.
     *
     * @param distance      range in meters
     * @param azimuth       azimuth in degrees
     * @param inclination   inclination in degrees
     * @param success       whether the reading parsed cleanly (the demo tool
     *                      always reports {@code true}; kept for parity with
     *                      the tool's callback shape)
     * @param pointFromSelf the reading projected from the self marker's
     *                      position, rendered to a string by the impl (the
     *                      legacy demo logged exactly this projection)
     */
    void onRangeFinderResults(double distance, double azimuth,
            double inclination, boolean success, String pointFromSelf);
}
