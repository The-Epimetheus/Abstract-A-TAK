package com.atakmap.android.helloworld.features.attachment;

import com.atakmap.android.helloworld.abstraction.Creator;

/**
 * Abstraction over ATAK's per-item attachment plumbing: the
 * {@code AttachmentManager} folder layout ({@code atak/attachments/<uid>/})
 * plus host-asset extraction into it. One deep, intent-level method — drop a
 * marker and file an image under its attachments folder in a single call; no
 * ATAK type crosses the seam. Interface in {@code src/main}; the
 * implementation lives in {@code src/atakShared}.
 *
 * <p>Deliberately NOT routed through
 * {@code com.atakmap.android.helloworld.features.marker.MarkerCreator}: the
 * legacy demo's placement leans on the point tool's own defaults (a
 * tool-generated uid, the tool's default details/persist handling), which the
 * MarkerSpec path would override (it always sets a uid and always calls
 * {@code showCotDetails}). Keeping the drop inside this seam preserves the
 * demo's placement byte-for-byte.
 */
public interface AttachmentCreator extends Creator {

    /**
     * Place a marker at the current map center and copy an image out of the
     * HOST APK's assets into that marker's attachments folder, so the image
     * shows up as the marker's attachment in ATAK's UI (radial menu →
     * attachments gallery).
     *
     * @return the placed marker's uid, or null if the map is not ready.
     */
    String attachImageToNewMarker(MarkerAttachmentSpec spec);
}
