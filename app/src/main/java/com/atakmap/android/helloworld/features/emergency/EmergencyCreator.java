package com.atakmap.android.helloworld.features.emergency;

import com.atakmap.android.helloworld.abstraction.Creator;

/**
 * Abstraction over ATAK's emergency beacon ({@code EmergencyManager}). Stable
 * across every targeted version today, so this is an insurance seam (see
 * CONTEXT.md). Interface in {@code src/main}; the implementation lives in
 * {@code src/atakShared}.
 */
public interface EmergencyCreator extends Creator {

    /** Start repeating a 911 emergency alert from this device. */
    void start911Alert();

    /** Cancel the repeating 911 emergency alert. */
    void cancel911Alert();

    /**
     * Start repeating an emergency alert of the given type — the speech
     * demo's path, where the type arrives as a spoken description resolved
     * via {@code EmergencyType.fromDescription}. Sets it as the current
     * emergency type, initiates the repeat, and flags the emergency on
     * (the legacy speech dispatch did all three).
     */
    void startAlertOfType(String typeDescription);
}
