package com.atakmap.android.helloworld.features.overlayview;

import android.content.Intent;

import com.atakmap.android.helloworld.features.broadcast.BroadcastCreator;
import com.atakmap.android.helloworld.view.ViewOverlayExample;

/**
 * The overlay-view feature's Controller. A bare pass-through to
 * {@link BroadcastCreator} — kept anyway so every feature shows the same
 * tap → Controller → Creator shape (ADR-0005); {@link ViewOverlayExample}
 * owns the receiving side.
 */
public class OverlayViewController {

    private final BroadcastCreator broadcastCreator;

    public OverlayViewController(BroadcastCreator broadcastCreator) {
        this.broadcastCreator = broadcastCreator;
    }

    /** Toggle the map-anchored overlay view demo. */
    public void toggleOverlayView() {
        broadcastCreator.send(
                new Intent(ViewOverlayExample.TOGGLE_OVERLAY_VIEW));
    }
}
