package com.atakmap.android.helloworld.features.datapackage;

import android.content.Context;
import android.util.Log;

import com.atakmap.android.helloworld.abstraction.SelfCheckResult;
import com.atakmap.android.helloworld.abstraction.SelfChecks;
import com.atakmap.android.importexport.CotEventFactory;
import com.atakmap.android.importexport.ExportFilters;
import com.atakmap.android.importexport.Exportable;
import com.atakmap.android.importexport.FormatNotSupportedException;
import com.atakmap.android.maps.MapItem;
import com.atakmap.android.maps.MapView;
import com.atakmap.android.maps.Marker;
import com.atakmap.android.maps.Shape;
import com.atakmap.android.missionpackage.export.MissionPackageExportMarshal;
import com.atakmap.android.missionpackage.export.MissionPackageExportWrapper;
import com.atakmap.android.missionpackage.file.MissionPackageBuilder;
import com.atakmap.android.missionpackage.file.MissionPackageManifest;
import com.atakmap.coremap.cot.event.CotEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * The only place ATAK's Mission Package ("data package") machinery is
 * touched: {@code MissionPackageManifest}/{@code MissionPackageBuilder} for
 * programmatic construction, and {@code MissionPackageExportMarshal} +
 * {@code Exportable}/{@code MissionPackageExportWrapper} for the export flow
 * that pops ATAK's own package-selection UI. Source-stable across all
 * targeted versions → shared impl source set.
 */
public final class DataPackageCreatorImpl implements DataPackageCreator {

    private static final String TAG = "DataPackageCreatorImpl";

    /** Reserved test-artifact namespace (see CONTEXT.md, load-time systems check). */
    private static final String TEST_NAME = "com.atakmap.android.helloworld.test.SYSTEMS_CHECK_DATAPACKAGE";

    private final Context pluginContext;

    public DataPackageCreatorImpl(Context pluginContext) {
        this.pluginContext = pluginContext;
    }

    @Override
    public String id() {
        return "DataPackageCreator";
    }

    @Override
    public String buildNearbyItemsPackage(NearbyItemsPackageSpec spec) {
        // a mission package is also known as a data package
        MapView mv = MapView.getMapView();
        if (mv == null)
            return null;

        Marker self = mv.getSelfMarker();
        Collection<MapItem> items = mv.getRootGroup()
                .deepFindItems(self.getPoint(), spec.radiusMeters(), null);
        MissionPackageManifest mpm = new MissionPackageManifest();
        for (MapItem item : items) {
            if (item instanceof Marker || item instanceof Shape) {
                // Round-trip filter: only items that turn into valid CoT are
                // added (a nevercot / non-persistable item would arrive
                // broken on the receiving end).
                CotEvent ce = CotEventFactory.createCotEvent(item);
                if (ce != null && ce.isValid()) {
                    if (item != mv.getSelfMarker())
                        mpm.addMapItem(item.getUID());
                }
            }
        }

        // sample code to show how to add a file to the data package
        //mpm.addFile(new File("/sdcard/sample.tif"), null);

        mpm.setName(spec.name());
        mpm.setPath(spec.filePath());

        // enable if you would like the data package deleted as soon as it is imported
        //mpm.getConfiguration().setParameter("onReceiveDelete", "true");

        MissionPackageBuilder mpb = new MissionPackageBuilder(null, mpm,
                mv.getRootGroup());
        return mpb.build();
    }

    @Override
    public void exportFilesWithPrompt(Context hostContext,
            List<String> absolutePaths) {
        // true = interactive: ATAK prompts the user to pick or create the
        // destination data package before copying the exportables in.
        MissionPackageExportMarshal missionPackageExportMarshal = new MissionPackageExportMarshal(
                hostContext, true);
        List<Exportable> exportables = new ArrayList<>();
        for (final String path : absolutePaths) {
            exportables.add(new Exportable() {
                @Override
                public boolean isSupported(Class<?> aClass) {
                    return true;
                }

                @Override
                public Object toObjectOf(Class<?> aClass,
                        ExportFilters exportFilters)
                        throws FormatNotSupportedException {
                    // false = a file path (not map-item uids).
                    return new MissionPackageExportWrapper(false, path);
                }
            });
        }
        try {
            missionPackageExportMarshal.execute(exportables);
        } catch (Exception e) {
            // Legacy behavior: swallow + log, never crash the tap.
            Log.d(TAG, "error building a new datapackage", e);
        }
    }

    /**
     * PARTIAL by design: the builder path IS exercised end-to-end — an empty
     * manifest under the reserved test namespace is built into the plugin
     * cache dir and the zip deleted in a finally — but the export-marshal
     * path is only constructed, because {@code execute()} would pop ATAK's
     * package-selection UI (user-visible).
     */
    @Override
    public SelfCheckResult selfCheck() {
        return SelfChecks.attempt(id(), "mission package machinery threw",
                () -> {
                    MapView mv = MapView.getMapView();
                    if (mv == null)
                        return SelfCheckResult.skipped(id(),
                                "MapView not ready");

                    File dir = new File(pluginContext.getCacheDir(),
                            "com.atakmap.android.helloworld.test");
                    dir.mkdirs();
                    File zip = new File(dir, "SYSTEMS_CHECK_DATAPACKAGE.zip");
                    try {
                        MissionPackageManifest mpm = new MissionPackageManifest();
                        mpm.setName(TEST_NAME);
                        mpm.setPath(zip.getPath());
                        MissionPackageBuilder mpb = new MissionPackageBuilder(
                                null, mpm, mv.getRootGroup());
                        String built = mpb.build();
                        // Export path: construct only — execute() is UI.
                        new MissionPackageExportMarshal(mv.getContext(), true);
                        return SelfCheckResult.partial(id(),
                                "built + deleted an empty test package ("
                                        + built
                                        + "); export marshal constructed, execute() not run (UI prompt)");
                    } finally {
                        zip.delete();
                        dir.delete();
                    }
                });
    }
}
