package com.atakmap.android.helloworld.features.stream;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.atakmap.android.helloworld.features.importer.ImportCreator;

import java.io.File;

/**
 * The stream feature's Controller: the two TAK-server stream demos from the
 * pane (addStream / removeStream), ATAK-free. Connection state, the
 * serverconnections listing and the stream teardown live behind
 * {@link StreamCreator}; the mission-package match/import path is the
 * already-seamed {@link ImportCreator}.
 */
public class StreamController {

    private static final String TAG = "StreamController";

    private final StreamCreator streamCreator;
    private final ImportCreator importCreator;

    public StreamController(StreamCreator streamCreator,
            ImportCreator importCreator) {
        this.streamCreator = streamCreator;
        this.importCreator = importCreator;
    }

    /**
     * Import every server-connection mission package staged in
     * {@code <external-storage>/serverconnections}: each file is matched and
     * imported as a v1 mission package, toasting per-file success/failure.
     * Does nothing (beyond the log line) while the CoT service is not yet
     * connected — legacy behavior, preserved.
     *
     * @param hostContext the host activity context (from the pane shell) for
     *            the per-file toasts.
     */
    public void importServerConnections(Context hostContext) {
        Log.d(TAG, "connected to the cotservice: "
                + streamCreator.isCotServiceConnected());
        try {
            if (streamCreator.isCotServiceConnected()) {
                for (File f : streamCreator.listServerConnectionFiles()) {
                    Log.d(TAG, "found: " + f);

                    // v1 mission-package import was removed in ATAK 5.8;
                    // ImportCreator absorbs that (no-op stub on 5.8+).
                    if (!importCreator.matchMissionPackage(f)) {
                        Toast.makeText(hostContext,
                                "failure [1]: " + f,
                                Toast.LENGTH_SHORT).show();
                    } else {

                        boolean success = importCreator
                                .importMissionPackage(f);
                        if (success) {
                            Toast.makeText(hostContext,
                                    "success: " + f,
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(hostContext,
                                    "failure [2]: " + f,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                }
            }
        } catch (Exception ioe) {
            Log.d(TAG, "error: ", ioe);
        }
    }

    /**
     * Remove every configured TAK server stream. Destructive — drops the
     * user's real server connections (that is the demo). Does nothing while
     * the CoT service is not yet connected — legacy behavior, preserved.
     */
    public void removeAllStreams() {
        Log.d(TAG, "connected to the cotservice: "
                + streamCreator.isCotServiceConnected());
        if (streamCreator.isCotServiceConnected())
            streamCreator.removeAllStreams();
    }
}
