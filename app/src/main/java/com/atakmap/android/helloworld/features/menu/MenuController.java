package com.atakmap.android.helloworld.features.menu;

import com.atakmap.android.helloworld.abstraction.Disposable;

/**
 * The menu feature's Controller: swapping the plugin's custom radial-menu
 * factory in and out, and overriding the menu ATAK serves for a whole CoT
 * type. ATAK-free, depending only on {@link MenuCreator}; the Pane controller
 * forwards the taps here (and keeps the buttons' selected-state toggles).
 *
 * <p>Holds live menu registrations through its Creator, so it is
 * {@link Disposable}: the Pane controller's dispose cascade restores the
 * stock radial menus automatically.
 */
public class MenuController implements Disposable {

    private final MenuCreator menuCreator;

    public MenuController(MenuCreator menuCreator) {
        this.menuCreator = menuCreator;
    }

    /**
     * Give every friendly ("a-f") marker the menu ATAK has registered for
     * hostile ("a-h") markers — overriding the default menu of a whole CoT
     * type at once, not just of one marker.
     */
    public void overrideFriendlyMenuWithHostile() {
        menuCreator.overrideMenuForType("a-f", "a-h");
    }

    /** Restore ATAK's stock menu for friendly ("a-f") markers. */
    public void restoreFriendlyMenu() {
        menuCreator.clearMenuOverrideForType("a-f");
    }

    /** Swap the plugin's custom radial menus in for ATAK's stock ones. */
    public void enableCustomRadialMenus() {
        menuCreator.enableCustomRadialMenus();
    }

    /** Restore ATAK's stock radial menus. */
    public void disableCustomRadialMenus() {
        menuCreator.disableCustomRadialMenus();
    }

    /**
     * Make sure we restore stock menus, say when a new version is hot
     * loaded... (Legacy parity: the type-menu override for "a-f" was never
     * torn down on dispose and still is not — MapMenuReceiver only holds
     * host-owned menu objects for it, so nothing of the plugin leaks.)
     */
    @Override
    public void dispose() {
        menuCreator.disableCustomRadialMenus();
    }
}
