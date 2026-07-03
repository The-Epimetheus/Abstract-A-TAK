package com.atakmap.android.helloworld.features.transfer;

import java.util.List;

import com.atakmap.android.helloworld.abstraction.Creator;

/**
 * Abstraction over ATAK's file-transfer surface: the import-manager file
 * browser, the {@code SendDialog} (send a file to contacts/servers as a
 * mission package), and the prompt-driven {@code MissionPackageExportMarshal}
 * export flow. Also hides the filesystem plumbing those flows require —
 * {@code FileSystemUtils} path resolution and {@code IOProviderFactory}
 * writes (ATAK's IO abstraction; encrypted-storage aware) — so staging a
 * file under ATAK's data root never leaks a coremap type into
 * {@code src/main}. Interface here; implementation in {@code src/atakShared}.
 */
public interface TransferCreator extends Creator {

    /**
     * Ask ATAK's import manager to open its own "Import" file browser (the
     * {@code USER_IMPORT_FILE_ACTION} broadcast on ATAK's internal bus). No
     * plugin UI is involved: whatever the user picks is routed through the
     * registered importers — a {@code .hwi} file lands in the plugin's own
     * importer.
     */
    void requestUserFileImport();

    /**
     * Stage the spec's contents as a file under ATAK's data root, then show
     * ATAK's send dialog for it so the user can pick contacts/servers.
     * Staging failures are logged and swallowed (legacy behavior); the
     * dialog is shown only if the file exists afterwards.
     *
     * @return {@code true} if the send dialog was shown, {@code false} if
     *         staging left no file behind.
     */
    boolean sendFile(FileSendSpec spec);

    /**
     * Run ATAK's prompt-driven "export to data package" flow (name prompt,
     * package assembly, deploy) over the given files. Each path is packaged
     * as a raw file entry — not a map item. Errors during the export are
     * logged and swallowed (legacy behavior).
     *
     * @param absoluteFilePaths absolute paths of the files to include
     */
    void exportFilesToDataPackage(List<String> absoluteFilePaths);
}
