package com.atakmap.android.helloworld.features.stream;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import com.atakmap.android.helloworld.abstraction.SelfCheckResult;
import com.atakmap.android.helloworld.abstraction.SelfChecks;
import com.atakmap.comms.CotServiceRemote;
import com.atakmap.coremap.io.IOProviderFactory;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * The only place this feature touches ATAK's comms plumbing. Owns a dedicated
 * {@link CotServiceRemote} — multiple simultaneous CotServiceRemote clients
 * are fine (the legacy receiver already ran two: its own {@code csr} plus the
 * one inside {@code CotStreamListener}), so this seam does not have to share
 * the receiver's still-unmigrated connection-lifecycle demo.
 *
 * <p>{@code connect()} is asynchronous: the connected flag flips when ATAK's
 * CoT service binds, shortly after plugin load. Legacy parity: the connection
 * is never explicitly torn down on plugin unload — the legacy receiver never
 * disconnected its {@code csr} either, and no disconnect API is proven across
 * the ten targeted versions in this repo, so we deliberately match that
 * rather than invent one.
 */
public final class StreamCreatorImpl implements StreamCreator {

    private static final String TAG = "StreamCreatorImpl";

    private final CotServiceRemote csr;
    // volatile: the ConnectionListener callbacks arrive off the caller's
    // thread. (The legacy field was a plain boolean; harmless hardening.)
    private volatile boolean connected = false;

    public StreamCreatorImpl() {
        csr = new CotServiceRemote();
        csr.connect(new CotServiceRemote.ConnectionListener() {
            @Override
            public void onCotServiceConnected(Bundle fullServiceState) {
                Log.d(TAG, "onCotServiceConnected: ");
                connected = true;
            }

            @Override
            public void onCotServiceDisconnected() {
                Log.d(TAG, "onCotServiceDisconnected: ");
                connected = false;
            }
        });
    }

    @Override
    public String id() {
        return "StreamCreator";
    }

    @Override
    public boolean isCotServiceConnected() {
        return connected;
    }

    @Override
    public List<File> listServerConnectionFiles() {
        final File dir = new File(Environment
                .getExternalStorageDirectory()
                .getPath() + "/serverconnections");
        // IOProviderFactory, not File.listFiles(): file I/O must route
        // through ATAK's pluggable IO provider so it still works when the
        // host runs with encrypted storage.
        File[] listing = IOProviderFactory.listFiles(dir);
        // Seam contract: missing/unlistable directory -> empty list
        // (absorbs the legacy null-check on the raw File[]).
        if (listing == null)
            return Collections.emptyList();
        return Arrays.asList(listing);
    }

    @Override
    public void removeAllStreams() {
        // "**" is the CoT service's match-everything connect-string
        // wildcard: one call drops every configured TAK server stream.
        csr.removeStream("**");
    }

    /**
     * PARTIAL by design: exercises the harmless read path (the
     * serverconnections listing through IOProviderFactory) and reports the
     * live connection state; {@code removeStream} is never probed — it would
     * drop the user's real TAK server connections.
     */
    @Override
    public SelfCheckResult selfCheck() {
        return SelfChecks.attempt(id(), "stream plumbing threw", () -> {
            List<File> staged = listServerConnectionFiles();
            return SelfCheckResult.partial(id(),
                    "CotServiceRemote created (connected=" + connected
                            + ", async bind); serverconnections listing="
                            + staged.size() + " file(s); removeStream not"
                            + " exercised (would drop the user's live TAK"
                            + " server connections)");
        });
    }
}
