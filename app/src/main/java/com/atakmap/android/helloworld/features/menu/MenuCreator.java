package com.atakmap.android.helloworld.features.menu;

import com.atakmap.android.helloworld.abstraction.Creator;

/**
 * Abstraction over ATAK's radial-menu factory registration. Version-sensitive:
 * the menu/button widget classes the factory builds moved from
 * {@code com.atakmap.android.widgets} to {@code gov.tak.api.widgets} in ATAK 5.5
 * — the whole factory is banded (atakPre55/atak55plus) behind this seam.
 *
 * <p>Interface in {@code src/main} (ATAK-free); the implementation lives in
 * {@code src/atakShared}, owning the banded {@code MenuFactory} instance.
 */
public interface MenuCreator extends Creator {

    /**
     * Register the plugin's custom radial-menu factory with ATAK (idempotent —
     * at most one factory instance is ever registered by this Creator).
     */
    void registerMenuFactory();

    /** Unregister the factory registered by {@link #registerMenuFactory()}. */
    void unregisterMenuFactory();
}
