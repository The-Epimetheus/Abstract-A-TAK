package com.atakmap.android.helloworld.features.importer;

import android.content.Context;

import com.atakmap.android.helloworld.abstraction.SelfCheckResult;
import com.atakmap.android.helloworld.compat.MissionImportCompat;
import com.atakmap.android.helloworld.importer.HelloImportResolver;
import com.atakmap.android.importexport.ImportExportMapComponent;
import com.atakmap.android.maps.MapView;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

/**
 * The only place ATAK's import framework is touched. Both real breaks stay in
 * their band source sets behind this impl: {@link HelloImportResolver}'s base
 * class (src/bands/atakPre57 / src/bands/atak57plus) and the v1 mission-package sort in
 * {@link MissionImportCompat} (src/bands/atakPre58 / src/bands/atak58plus stub). This impl
 * itself is version-stable, so it lives in {@code src/atakShared}.
 */
public final class ImportCreatorImpl implements ImportCreator {

    private final Context pluginContext;
    private HelloImportResolver resolver;

    public ImportCreatorImpl(Context pluginContext) {
        this.pluginContext = pluginContext;
    }

    @Override
    public String id() {
        return "ImportCreator";
    }

    @Override
    public void registerHelloImporter() {
        if (resolver != null)
            return;
        resolver = new HelloImportResolver(MapView.getMapView(), pluginContext);
        ImportExportMapComponent.getInstance().addImporterClass(resolver);
    }

    @Override
    public void unregisterHelloImporter() {
        if (resolver == null)
            return;
        ImportExportMapComponent.getInstance().removeImporterClass(resolver);
        resolver = null;
    }

    @Override
    public String helloImportToolDirectory() {
        return HelloImportResolver.TOOL_NAME;
    }

    @Override
    public boolean matchMissionPackage(File f) {
        return MissionImportCompat.match(MapView.getMapView(), f);
    }

    @Override
    public boolean importMissionPackage(File f) {
        return MissionImportCompat.beginImport(MapView.getMapView(), f);
    }

    /**
     * Exercises both banded paths without importing anything: constructs a
     * throwaway {@link HelloImportResolver} (the banded base-class ctor is the
     * 5.7 break) and matches a probe .hwi written to — and deleted from — the
     * plugin cache dir; then probes the mission-package sort against that same
     * probe file (constructs the banded sort; matches false, imports nothing).
     */
    @Override
    public SelfCheckResult selfCheck() {
        MapView mv = MapView.getMapView();
        if (mv == null)
            return SelfCheckResult.skipped(id(), "MapView not ready");
        // GOTCHA (caught live by this very check): the PLUGIN context's cache dir
        // is unusable — a plugin never runs as its own app, so its data dir is
        // never provisioned and the host process cannot create it. Use the HOST
        // (MapView) context for any filesystem needs.
        File probe = new File(mv.getContext().getCacheDir(),
                "helloworld.selfcheck.probe.hwi");
        try {
            HelloImportResolver probeResolver =
                    new HelloImportResolver(mv, pluginContext);

            try (FileOutputStream out = new FileOutputStream(probe)) {
                out.write("{\"helloWorldSample\":\"selfcheck\"}"
                        .getBytes(StandardCharsets.UTF_8));
            }
            boolean hwiMatched = probeResolver.match(probe);
            boolean mpMatched = MissionImportCompat.match(mv, probe);

            return SelfCheckResult.partial(id(),
                    "banded resolver constructed (hwi probe match=" + hwiMatched
                            + "), mission-package sort probed (match=" + mpMatched
                            + "); no import performed");
        } catch (Throwable t) {
            return SelfCheckResult.failed(id(), "banded import path threw", t);
        } finally {
            //noinspection ResultOfMethodCallIgnored
            probe.delete();
        }
    }
}
