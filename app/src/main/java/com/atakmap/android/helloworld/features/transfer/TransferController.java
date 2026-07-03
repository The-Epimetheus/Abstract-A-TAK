package com.atakmap.android.helloworld.features.transfer;

import android.content.Context;
import android.widget.Toast;

import com.atakmap.android.helloworld.features.importer.ImportCreator;

import java.io.File;
import java.util.Collections;

/**
 * The file-transfer feature's Controller: the import-a-file, send-a-file and
 * export-to-data-package demos, all ATAK-free — the import-manager broadcast,
 * SendDialog and MissionPackageExportMarshal live behind
 * {@link TransferCreator}. Leans on {@link ImportCreator} for the plugin's
 * import-tool directory so the sent sample lands where the importer sorts
 * incoming {@code .hwi} files.
 */
public class TransferController {

    private final TransferCreator transferCreator;
    private final ImportCreator importCreator;

    public TransferController(TransferCreator transferCreator,
            ImportCreator importCreator) {
        this.transferCreator = transferCreator;
        this.importCreator = importCreator;
    }

    /**
     * Open ATAK's own import file browser; picking a {@code .hwi} file
     * exercises the plugin's registered importer.
     */
    public void requestUserImport(Context hostContext) {
        toast(hostContext, "Select .hwi file");
        transferCreator.requestUserFileImport();
    }

    /**
     * Write the sample {@code .hwi} file into the plugin's import-tool
     * directory and offer it through ATAK's send dialog.
     */
    public void sendSampleFile(Context hostContext) {
        toast(hostContext, "Sending .hwi file...");
        final String contents = "{\"helloWorldSample\" : \"Sample File\" }";
        transferCreator.sendFile(FileSendSpec.builder()
                .itemPath(importCreator.helloImportToolDirectory()
                        + File.separator + "sample1.hwi")
                .contents(contents)
                .build());
    }

    /**
     * Run the prompt-driven data-package export over ATAK's own
     * {@code support.inf} (a file known to exist on every install) — the
     * minimal demo of packaging an arbitrary file for distribution.
     */
    public void exportSupportFileDataPackage() {
        transferCreator.exportFilesToDataPackage(Collections
                .singletonList("/sdcard/atak/support/support.inf"));
    }

    private void toast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }
}
