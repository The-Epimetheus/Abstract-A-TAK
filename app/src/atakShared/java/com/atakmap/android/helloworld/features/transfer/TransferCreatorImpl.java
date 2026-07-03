package com.atakmap.android.helloworld.features.transfer;

import android.content.Intent;

import com.atakmap.android.helloworld.abstraction.SelfCheckResult;
import com.atakmap.android.helloworld.abstraction.SelfChecks;
import com.atakmap.android.importexport.ExportFilters;
import com.atakmap.android.importexport.Exportable;
import com.atakmap.android.importexport.FormatNotSupportedException;
import com.atakmap.android.importexport.ImportExportMapComponent;
import com.atakmap.android.importexport.send.SendDialog;
import com.atakmap.android.ipc.AtakBroadcast;
import com.atakmap.android.maps.MapView;
import com.atakmap.android.missionpackage.export.MissionPackageExportMarshal;
import com.atakmap.android.missionpackage.export.MissionPackageExportWrapper;
import com.atakmap.coremap.filesystem.FileSystemUtils;
import com.atakmap.coremap.io.IOProviderFactory;
import com.atakmap.coremap.log.Log;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * The only place ATAK's file-transfer surface is touched: the import-manager
 * broadcast, {@code SendDialog}, the {@code MissionPackageExportMarshal}
 * export flow, and the {@code FileSystemUtils}/{@code IOProviderFactory}
 * filesystem plumbing they need. All of it is source-stable across the
 * supported versions, so this impl lives in {@code src/atakShared}.
 */
public final class TransferCreatorImpl implements TransferCreator {

    private static final String TAG = "TransferCreatorImpl";

    @Override
    public String id() {
        return "TransferCreator";
    }

    @Override
    public void requestUserFileImport() {
        // ImportExportMapComponent listens for this on ATAK's internal bus
        // and opens ATAK's own "Import" browser — the plugin never shows a
        // picker of its own; its registered importer just receives whatever
        // matching file the user selects.
        AtakBroadcast.getInstance().sendBroadcast(
                new Intent(ImportExportMapComponent.USER_IMPORT_FILE_ACTION));
    }

    @Override
    public boolean sendFile(FileSendSpec spec) {
        // FileSystemUtils.getItem resolves relative to ATAK's data root;
        // IOProviderFactory routes the write through ATAK's IO abstraction so
        // it works even when the host runs with encrypted storage.
        File testFile = FileSystemUtils.getItem(spec.itemPath());
        testFile.getParentFile().mkdir();

        try (OutputStream os = IOProviderFactory.getOutputStream(testFile)) {
            try (InputStream is = new ByteArrayInputStream(
                    spec.contents().getBytes())) {
                FileSystemUtils.copy(is, os);
            }
        } catch (IOException e) {
            Log.e(TAG, "error occurred", e);
        }

        // Seam contract: only show the dialog if staging actually left a
        // file behind (the write above may have failed and been logged).
        if (FileSystemUtils.isFile(testFile)) {
            // SendDialog is ATAK's "send to contacts/TAK server" chooser; it
            // wraps the file in a mission package for delivery.
            new SendDialog.Builder(MapView.getMapView())
                    .addFile(testFile).show();
            return true;
        }
        return false;
    }

    @Override
    public void exportFilesToDataPackage(List<String> absoluteFilePaths) {
        // The marshal drives ATAK's full "export to data package" UX:
        // prompts for a package name, assembles the entries, offers deploy.
        // The boolean ctor arg selects the new-package flow the legacy demo
        // used.
        MissionPackageExportMarshal missionPackageExportMarshal =
                new MissionPackageExportMarshal(
                        MapView.getMapView().getContext(), true);
        List<Exportable> exportables = new ArrayList<>();
        for (final String path : absoluteFilePaths) {
            exportables.add(new Exportable() {
                @Override
                public boolean isSupported(Class<?> aClass) {
                    return true;
                }

                @Override
                public Object toObjectOf(Class<?> aClass,
                        ExportFilters exportFilters)
                        throws FormatNotSupportedException {
                    // first ctor arg false = a raw file entry; true would
                    // mean the string is a map-item UID instead.
                    return new MissionPackageExportWrapper(false, path);
                }
            });
        }
        try {
            missionPackageExportMarshal.execute(exportables);
        } catch (Exception e) {
            Log.d(TAG, "error building a new datapackage", e);
        }
    }

    /**
     * PARTIAL by design: showing SendDialog, executing the export marshal or
     * broadcasting USER_IMPORT_FILE_ACTION are all user-visible. The probe
     * exercises the real staging path (IOProviderFactory write +
     * FileSystemUtils copy/isFile) against a throwaway cache file, and
     * constructs — without showing/executing — the SendDialog builder and
     * export marshal/wrapper, which is every ATAK type this impl links.
     */
    @Override
    public SelfCheckResult selfCheck() {
        MapView mv = MapView.getMapView();
        if (mv == null)
            return SelfCheckResult.skipped(id(), "MapView not ready");
        // HOST cache dir, not the plugin's — a plugin's own data dir is never
        // provisioned (see the same gotcha in ImportCreatorImpl.selfCheck).
        File probe = new File(mv.getContext().getCacheDir(),
                "helloworld.selfcheck.transfer.hwi");
        return SelfChecks.attempt(id(), "file transfer plumbing threw", () -> {
            try {
                try (OutputStream os = IOProviderFactory
                        .getOutputStream(probe)) {
                    try (InputStream is = new ByteArrayInputStream(
                            "{\"helloWorldSample\":\"selfcheck\"}"
                                    .getBytes())) {
                        FileSystemUtils.copy(is, os);
                    }
                }
                boolean staged = FileSystemUtils.isFile(probe);

                // Construct-only probes of the user-visible paths.
                new SendDialog.Builder(mv).addFile(probe);
                new MissionPackageExportMarshal(mv.getContext(), true);
                new MissionPackageExportWrapper(false,
                        probe.getAbsolutePath());

                return SelfCheckResult.partial(id(),
                        "staged probe file via IOProviderFactory (isFile="
                                + staged + "); SendDialog builder and export "
                                + "marshal constructed; nothing shown or "
                                + "broadcast (would be user-visible)");
            } finally {
                //noinspection ResultOfMethodCallIgnored
                probe.delete();
            }
        });
    }
}
