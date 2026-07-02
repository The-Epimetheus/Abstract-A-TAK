package com.atakmap.android.helloworld.features.location;

import com.atakmap.android.helloworld.abstraction.Creator;

/**
 * Abstraction over ATAK's mock-GPS feed. The metadata store behind it is
 * version-sensitive: {@code MapView.getMapData()} returned {@code MapData}
 * (Bundle-style {@code putX}) through 5.2 and {@code MetaDataHolder2}
 * ({@code setMetaX}) from 5.3 — a real class removal, absorbed by the
 * atakPre53/atak53plus compatibility band behind this seam.
 *
 * <p>Interface in {@code src/main} (ATAK-free); the implementation lives in
 * {@code src/atakShared} and delegates the diverging writes to the banded
 * internal helper.
 */
public interface LocationCreator extends Creator {

    /**
     * Feed a simulated self position (decimal degrees) into ATAK's mock-GPS
     * pipeline: stamps the mock-location metadata and fires the GPS-received
     * broadcast, exactly like a real fix.
     *
     * @return the mock timestamp that was stamped, or {@code -1} if the self
     *         marker is not available yet (nothing was applied).
     */
    long simulateSelfLocation(double latitude, double longitude);
}
