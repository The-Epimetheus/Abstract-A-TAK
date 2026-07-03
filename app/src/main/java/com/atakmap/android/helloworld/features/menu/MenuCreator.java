package com.atakmap.android.helloworld.features.menu;

import com.atakmap.android.helloworld.abstraction.Creator;

/**
 * Abstraction over swapping the plugin's custom radial menus in and out.
 * Version-sensitive: the menu/button widget classes behind it moved from
 * {@code com.atakmap.android.widgets} to {@code gov.tak.api.widgets} in ATAK 5.5
 * — the whole factory is banded (atakPre55/atak55plus) behind this seam; neither
 * the factory nor ATAK's registration API is named here.
 *
 * <p>Interface in {@code src/main} (ATAK-free); the implementation lives in
 * {@code src/atakShared}, owning the banded factory instance.
 */
public interface MenuCreator extends Creator {

    /**
     * Swap the plugin's custom radial menus in for ATAK's stock ones
     * (idempotent — at most one factory instance is ever registered by this
     * Creator).
     */
    void enableCustomRadialMenus();

    /** Restore ATAK's stock radial menus (undoes {@link #enableCustomRadialMenus()}). */
    void disableCustomRadialMenus();

    /**
     * Render the plugin asset {@code assetName} (e.g. {@code "menu.xml"}, a
     * radial-menu definition) into the string form a marker's {@code menu}
     * metadata expects.
     */
    String loadRadialMenu(String assetName);

    /**
     * Override the radial menu for a whole CoT type: every marker of
     * {@code type} gets the menu ATAK currently has registered for
     * {@code sourceType}. Re-registering just replaces the previous
     * override for that type.
     */
    void overrideMenuForType(String type, String sourceType);

    /** Restore ATAK's stock menu for {@code type} (undoes {@link #overrideMenuForType}). */
    void clearMenuOverrideForType(String type);
}
