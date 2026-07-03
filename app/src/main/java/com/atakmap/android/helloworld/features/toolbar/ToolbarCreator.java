package com.atakmap.android.helloworld.features.toolbar;

import com.atakmap.android.helloworld.abstraction.Creator;

/**
 * Abstraction over ATAK's action-bar toolbar cluster: {@code ActionBarReceiver}
 * and the {@code ActionMenuData} / {@code ActionClickData} /
 * {@code ActionBroadcastData} family. A toolbar item is described by a
 * plugin-owned {@link ToolbarItemSpec}; ATAK is told about it by broadcast
 * (ADD_NEW_TOOLS / REMOVE_TOOLS on the internal bus), and taps on the item
 * come back to the plugin as the spec's click broadcast. Interface in
 * {@code src/main} (ATAK-free); the implementation lives in
 * {@code src/atakShared}.
 */
public interface ToolbarCreator extends Creator {

    /**
     * Add the described item to ATAK's toolbar; its {@link ToolbarPlacement}
     * decides where it appears (the demo lands in the overflow menu). Tapping
     * the item fires the spec's click broadcast on ATAK's internal bus, with
     * no extras — register for that action (see {@code BroadcastCreator}) to
     * react. The built ATAK menu entry is cached under the spec's ref so
     * {@link #removeToolbarItem(String)} can hand ATAK back the very instance
     * it added.
     */
    void addToolbarItem(ToolbarItemSpec spec);

    /**
     * Remove the item previously added under {@code ref}. Seam contract:
     * removal dispatches the same menu-entry instance the add did (the legacy
     * demo built one instance and reused it for both; matching by an
     * equal-looking fresh instance is not a contract ATAK documents). No-op
     * if this Creator never added {@code ref} this load — there is nothing to
     * hand back.
     */
    void removeToolbarItem(String ref);
}
