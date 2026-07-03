package com.atakmap.android.helloworld.features.datapackage;

import android.content.Context;

import com.atakmap.android.helloworld.abstraction.Creator;

import java.util.List;

/**
 * Abstraction over ATAK's Mission Package ("data package") machinery:
 * {@code MissionPackageManifest}/{@code MissionPackageBuilder} for
 * programmatic package construction, and the
 * {@code MissionPackageExportMarshal}/{@code Exportable} export flow that
 * drives ATAK's own package-selection UI. All of those are versioned ATAK
 * types that must not leak into {@code src/main}. Interface here;
 * implementation in {@code src/atakShared} (source-stable across all targeted
 * versions).
 */
public interface DataPackageCreator extends Creator {

    /**
     * Build a mission package of the map items around the self marker: every
     * {@code Marker} or {@code Shape} within {@code spec.radiusMeters()} of
     * self that round-trips to valid CoT (the self marker itself is excluded)
     * is added by uid, and the package zip is written at
     * {@code spec.filePath()}.
     *
     * <p>Runs synchronously on the calling thread (the legacy demo built on
     * the UI thread; the heavy part is the zip write).
     *
     * @return the built package path as reported by ATAK's builder, or
     *         {@code null} when the MapView is not ready.
     */
    String buildNearbyItemsPackage(NearbyItemsPackageSpec spec);

    /**
     * Launch ATAK's Mission Package export flow seeded with the given files:
     * ATAK prompts the user to pick (or create) the target data package, then
     * copies the files in. Each path becomes one {@code Exportable} wrapping a
     * {@code MissionPackageExportWrapper}.
     *
     * <p>Errors are logged, not thrown — matching the legacy demo, which
     * swallowed the export exception with a log line.
     *
     * @param hostContext the host activity context (the export UI dialogs
     *                    must be built on it, not the plugin context)
     * @param absolutePaths absolute file paths to offer for export
     */
    void exportFilesWithPrompt(Context hostContext, List<String> absolutePaths);
}
