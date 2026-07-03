package com.atakmap.android.helloworld.features.broadcast;

import android.content.Intent;

/**
 * Callback port for inbound broadcasts: a Controller implements it (usually as
 * a lambda) to hear the intents a
 * {@link BroadcastCreator#listen(String, String, BroadcastPort)} registration
 * matches. The {@link Intent} crossing it is Android, not ATAK — boundary-legal
 * under ADR-0002 — so no Plugin-DTO translation is needed at this seam.
 */
public interface BroadcastPort {

    /**
     * One matching broadcast arrived. Called on whatever thread ATAK's bus
     * delivers on — hop to the main thread yourself before touching UI (the
     * legacy demo posted its toast for exactly this reason).
     */
    void onBroadcast(Intent intent);
}
