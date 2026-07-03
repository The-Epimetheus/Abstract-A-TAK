package com.atakmap.android.helloworld.features.attachment;

import android.util.Log;

/**
 * The attachment feature's Controller: the marker-with-attachment demo,
 * ATAK-free, depending only on {@link AttachmentCreator}. A deliberately
 * shallow pass-through — every feature gets a Controller so the reference
 * shows exactly one shape (ADR-0005). The Pane controller forwards the tap
 * here.
 */
public class AttachmentController {

    private static final String TAG = "AttachmentController";

    private final AttachmentCreator attachmentCreator;

    public AttachmentController(AttachmentCreator attachmentCreator) {
        this.attachmentCreator = attachmentCreator;
    }

    /**
     * The imageAttach demo: drop a friendly aircraft at the map center and
     * attach the host's bundled ac130 icon to it as {@code test.png} —
     * showing where per-item attachments live
     * ({@code atak/attachments/<uid>/}) and that they appear in the marker's
     * attachments gallery.
     */
    public void attachImageToNewMarker() {
        String uid = attachmentCreator.attachImageToNewMarker(
                MarkerAttachmentSpec.builder()
                        .markerType("a-f-A")
                        .callsign("Marker with Attachment")
                        .assetPath("icons/ac130.png")
                        .attachmentFileName("test.png")
                        .build());
        if (uid == null)
            Log.w(TAG, "map not ready; no marker placed");
    }
}
