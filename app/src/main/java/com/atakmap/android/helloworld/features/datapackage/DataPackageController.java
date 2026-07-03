package com.atakmap.android.helloworld.features.datapackage;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.atakmap.android.helloworld.features.importer.ImportCreator;

import java.io.File;
import java.util.Collections;

/**
 * The data-package feature's Controller: the two data-package demos, both
 * ATAK-free — the Mission Package machinery (manifest/builder, export
 * marshal) lives behind {@link DataPackageCreator}, and the version-sensitive
 * import path behind {@link com.atakmap.android.helloworld.features.importer.ImportCreator}.
 */
public class DataPackageController {

    /** Manifest name shown in ATAK's Mission Package tool. */
    private static final String PACKAGE_NAME = "items-around-me";

    /** Where the generated demo package zip is written. */
    private static final String PACKAGE_PATH = "/sdcard/test.zip";

    /** Search radius around the self marker, in meters. */
    private static final int NEARBY_RADIUS_METERS = 20000;

    /** The file the file-prompt demo offers to ATAK's export flow. */
    private static final String SUPPORT_INF_PATH = "/sdcard/atak/support/support.inf";

    private final DataPackageCreator dataPackageCreator;
    private final ImportCreator importCreator;

    public DataPackageController(DataPackageCreator dataPackageCreator,
            ImportCreator importCreator) {
        this.dataPackageCreator = dataPackageCreator;
        this.importCreator = importCreator;
    }

    /**
     * Build a data package of the markers/shapes within 20km of self at
     * {@code /sdcard/test.zip}, then import it back into this ATAK on a
     * worker thread — the round trip demonstrates both halves of the mission
     * package API. Toasts "import failed" if the import path declines
     * (which it always does on ATAK 5.8+, where the v1 import path was
     * removed — see {@link ImportCreator#importMissionPackage}).
     *
     * @param hostContext the host activity context the failure toast is
     *                    shown on (the legacy demo used the MapView context)
     */
    public void generateItemsAroundMePackage(final Context hostContext) {
        // Build synchronously, as the legacy demo did (UI thread).
        dataPackageCreator.buildNearbyItemsPackage(NearbyItemsPackageSpec
                .builder()
                .name(PACKAGE_NAME)
                .filePath(PACKAGE_PATH)
                .radiusMeters(NEARBY_RADIUS_METERS)
                .build());

        final File f = new File(PACKAGE_PATH);
        Thread t = new Thread("import-thread") {
            public void run() {
                if (!importCreator.importMissionPackage(f)) {
                    // Legacy posted the toast through MapView.post(); a
                    // main-looper Handler is the ATAK-free equivalent.
                    new Handler(Looper.getMainLooper()).post(
                            () -> Toast.makeText(hostContext, "import failed",
                                    Toast.LENGTH_SHORT).show());
                }
            }
        };
        t.start();
    }

    /**
     * Launch ATAK's Mission Package export flow seeded with
     * {@code /sdcard/atak/support/support.inf}: ATAK prompts the user to pick
     * or create the destination data package and copies the file in.
     */
    public void promptFileDataPackage(Context hostContext) {
        dataPackageCreator.exportFilesWithPrompt(hostContext,
                Collections.singletonList(SUPPORT_INF_PATH));
    }
}
