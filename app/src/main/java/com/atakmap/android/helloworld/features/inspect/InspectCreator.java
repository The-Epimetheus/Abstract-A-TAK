package com.atakmap.android.helloworld.features.inspect;

import com.atakmap.android.helloworld.abstraction.Creator;

/**
 * Abstraction over ATAK's map-item selection tooling. Behind it sits an
 * {@code AbstractMapItemSelectionTool} — a compact helper (3.12+) that hijacks
 * map interaction, prompts the user to tap a map item, and reports the result
 * as a broadcast on ATAK's internal bus — plus the
 * {@code ToolManagerBroadcastReceiver} start/end round trip and the one-shot
 * {@code AtakBroadcast} finish-listener registration. All of that is ATAK
 * plumbing that must not leak into {@code src/main}; the seam exposes it as a
 * single pick-an-item conversation. Interface here; implementation in
 * {@code src/atakShared}.
 *
 * <p>One inspection runs at a time (the pane's toggle button enforces this at
 * the source; the impl guards anyway).
 */
public interface InspectCreator extends Creator {

    /**
     * Start the map-item inspector: the host shows its "Select Map Item on
     * the screen" prompt and the next map tap picks an item. The listener
     * ALWAYS fires when the tool ends — selection made, invalid selection, or
     * {@link #stopItemInspection() cancelled} — so the caller can clear any
     * toggled UI state; its result is non-null only for a resolvable
     * selection. Delivered on the main thread.
     */
    void startItemInspection(InspectionListener listener);

    /**
     * End a running inspection. The finish round trip still happens: the tool
     * broadcasts its finished intent, so the listener passed to
     * {@link #startItemInspection} fires (with a null result). No-op when no
     * inspection ever started.
     */
    void stopItemInspection();

    /**
     * Tear down the selection tool and any still-armed finish listener.
     * Idempotent; called from the feature Controller's
     * {@code Disposable.dispose()} when the plugin unloads.
     */
    void disposeTool();
}
