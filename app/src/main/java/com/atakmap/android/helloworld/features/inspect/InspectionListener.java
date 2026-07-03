package com.atakmap.android.helloworld.features.inspect;

/**
 * Callback port for one map-item inspection (see
 * {@link InspectCreator#startItemInspection}). ATAK-free by design: the impl
 * adapts the tool's finished broadcast onto this.
 */
public interface InspectionListener {

    /**
     * The inspector tool ended, for any reason. {@code result} is non-null
     * only when the user actually selected a map item that could be resolved;
     * it is null on cancel, invalid selection, or a stale/missing item.
     * Always called exactly once per started inspection, on the main thread.
     */
    void onInspectionFinished(InspectionResult result);
}
