package com.atakmap.android.helloworld.compat;

import com.atakmap.android.importfiles.sort.ImportMissionPackageSort.ImportMissionV1PackageSort;
import com.atakmap.android.maps.MapView;

import java.io.File;

/**
 * Compatibility-band impl for ATAK &lt;= 5.7, where
 * {@code ImportMissionPackageSort.ImportMissionV1PackageSort} exists. It was removed
 * in ATAK 5.8 (see the {@code src/bands/atak58plus} stub twin). Keeps this shared demo path
 * off the versioned type so the core class stays version-agnostic.
 */
public final class MissionImportCompat {

    private MissionImportCompat() {}

    public static boolean match(MapView view, File f) {
        return new ImportMissionV1PackageSort(view.getContext(), true, true, false)
                .match(f);
    }

    public static boolean beginImport(MapView view, File f) {
        return new ImportMissionV1PackageSort(view.getContext(), true, true, true)
                .beginImport(f);
    }
}
