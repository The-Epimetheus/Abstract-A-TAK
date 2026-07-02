package com.atakmap.android.helloworld.compat;

import com.atakmap.android.maps.MapView;

import java.io.File;

/**
 * Compatibility-band impl for ATAK &gt;= 5.8, where
 * {@code ImportMissionPackageSort.ImportMissionV1PackageSort} was removed (TAK's own
 * 5.8 HelloWorld branch dropped this demo path). Version-agnostic no-op stub so the
 * shared core class still compiles; the &lt;=5.7 twin in {@code src/bands/atakPre58} carries
 * the real behavior.
 */
public final class MissionImportCompat {

    private MissionImportCompat() {}

    public static boolean match(MapView view, File f) {
        return false; // mission-package v1 import unavailable on 5.8+
    }

    public static boolean beginImport(MapView view, File f) {
        return false;
    }
}
