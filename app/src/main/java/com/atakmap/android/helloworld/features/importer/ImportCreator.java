package com.atakmap.android.helloworld.features.importer;

import java.io.File;

import com.atakmap.android.helloworld.abstraction.Creator;

/**
 * Abstraction over ATAK's import framework — the single most version-sensitive
 * cluster this plugin touches. Two real breaks live behind this seam:
 * <ul>
 *   <li>the resolver base class ({@code ImportInternalSDResolver} &le;5.6 vs
 *       {@code ImportResolver} 5.7+) — atakPre57/atak57plus band;</li>
 *   <li>{@code ImportMissionV1PackageSort}, removed in 5.8 —
 *       atakPre58/atak58plus band (a no-op stub on 5.8+).</li>
 * </ul>
 *
 * <p>Interface in {@code src/main} (ATAK-free — {@link File} is java.io); the
 * implementation lives in {@code src/atakShared}, delegating the divergences to
 * the banded {@code HelloImportResolver}/{@code MissionImportCompat} internals.
 */
public interface ImportCreator extends Creator {

    /** Register the plugin's .hwi file importer with ATAK's import framework. */
    void registerHelloImporter();

    /** Unregister the importer registered by {@link #registerHelloImporter()}. */
    void unregisterHelloImporter();

    /**
     * The tool-data directory (relative to ATAK's data root) where imported
     * .hwi files are sorted — for demo code that wants to create one.
     */
    String helloImportToolDirectory();

    /**
     * Whether {@code f} looks like a v1 mission package this host can import.
     * Always {@code false} on ATAK 5.8+, where the v1 import path was removed.
     */
    boolean matchMissionPackage(File f);

    /**
     * Import a v1 mission package. Always {@code false} (no-op) on ATAK 5.8+.
     */
    boolean importMissionPackage(File f);
}
