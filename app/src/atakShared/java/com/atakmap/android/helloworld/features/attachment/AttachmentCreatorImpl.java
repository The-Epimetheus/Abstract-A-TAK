package com.atakmap.android.helloworld.features.attachment;

import com.atakmap.android.helloworld.abstraction.SelfCheckResult;
import com.atakmap.android.helloworld.abstraction.SelfChecks;
import com.atakmap.android.maps.MapView;
import com.atakmap.android.maps.Marker;
import com.atakmap.android.user.PlacePointTool;
import com.atakmap.android.util.AttachmentManager;
import com.atakmap.coremap.filesystem.FileSystemUtils;
import com.atakmap.coremap.log.Log;

import java.io.File;

/**
 * The only place ATAK's attachment plumbing is touched
 * ({@code AttachmentManager} for the per-item folder, the point tool for the
 * drop, {@code FileSystemUtils} for asset extraction). Source-stable across
 * all targeted versions → shared impl source set.
 *
 * <p>The placement is kept byte-identical to the legacy demo: no explicit uid
 * (the point tool generates one) and no {@code showCotDetails} /
 * {@code setNeverPersist} calls — the tool's defaults are part of the demo's
 * observed behavior, which is why this drop does not go through MarkerCreator.
 */
public final class AttachmentCreatorImpl implements AttachmentCreator {

    private static final String TAG = "AttachmentCreatorImpl";

    /** Reserved test-artifact namespace (see CONTEXT.md, load-time systems check). */
    private static final String TEST_UID = "com.atakmap.android.helloworld.test.SYSTEMS_CHECK_ATTACHMENT";

    @Override
    public String id() {
        return "AttachmentCreator";
    }

    @Override
    public String attachImageToNewMarker(MarkerAttachmentSpec spec) {
        MapView mv = MapView.getMapView();
        if (mv == null)
            return null;

        PlacePointTool.MarkerCreator mc = new PlacePointTool.MarkerCreator(
                mv.getCenterPoint());

        mc.setType(spec.markerType());
        mc.setCallsign(spec.callsign());
        final Marker m = mc.placePoint();

        String folder = AttachmentManager.getFolderPath(m.getUID());
        String destFile = folder + "/" + spec.attachmentFileName();
        // for the copyFromAssets call, the folder needs to be relative
        // (legacy demo strips the primary-storage atak root; on hosts whose
        // atak root differs, the path passes through unchanged).
        destFile = destFile.replace("/storage/emulated/0/atak/", "");

        Log.d(TAG, "writing attachment to the folder: " + folder);
        // The asset is read from the HOST context's APK (icons/ac130.png
        // ships in ATAK core) — that is why the map view's context, not the
        // plugin context, is passed here.
        FileSystemUtils.copyFromAssetsToStorageFile(
                mv.getContext(),
                spec.assetPath(), destFile, true);
        return m.getUID();
    }

    /**
     * PARTIAL by design: a real run drops a user-visible marker (the point
     * tool announces the placement) and writes a file into its attachments
     * folder — irreversible from the user's point of view. The probe instead
     * resolves the attachment folder path for a reserved-namespace test uid,
     * which links the {@code AttachmentManager} symbol this impl depends on,
     * and sweeps up the (empty) folder if resolving it created one.
     */
    @Override
    public SelfCheckResult selfCheck() {
        return SelfChecks.attempt(id(), "attachment folder resolution threw",
                () -> {
                    MapView mv = MapView.getMapView();
                    if (mv == null)
                        return SelfCheckResult.skipped(id(),
                                "MapView not ready");
                    String folder = AttachmentManager.getFolderPath(TEST_UID);
                    try {
                        if (folder == null || folder.isEmpty())
                            throw new IllegalStateException(
                                    "AttachmentManager returned no folder path");
                        return SelfCheckResult.partial(id(),
                                "attachment folder resolved for a test uid; "
                                        + "marker drop + asset copy not exercised "
                                        + "(user-visible, announces placement)");
                    } finally {
                        if (folder != null && !folder.isEmpty()) {
                            // delete() only removes an EMPTY dir — safe sweep
                            // in case getFolderPath materialized the folder.
                            //noinspection ResultOfMethodCallIgnored
                            new File(folder).delete();
                        }
                    }
                });
    }
}
