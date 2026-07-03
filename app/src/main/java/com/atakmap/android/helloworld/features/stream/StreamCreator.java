package com.atakmap.android.helloworld.features.stream;

import java.io.File;
import java.util.List;

import com.atakmap.android.helloworld.abstraction.Creator;

/**
 * Minimal seam over ATAK's CoT service for the TAK-server stream demos
 * (the addStream/removeStream pane buttons). The impl owns its own
 * {@code CotServiceRemote} connection, so the buttons no longer depend on the
 * legacy receiver's connection state.
 *
 * <p>DELIBERATELY MINIMAL: the legacy receiver's wider connection-lifecycle
 * demo — the {@code CotStreamListener} subclass, the
 * {@code OutputsChangedListener}, and the {@code printNetworks()} port dump —
 * is deeply entangled with the shell's load lifecycle and is NOT behind this
 * seam yet (see the DEFERRED note on the receiver's {@code csr} field). When
 * that migrates, it grows here as callback-port registrations.
 *
 * <p>Interface in {@code src/main} (ATAK-free — {@link File} is java.io); the
 * implementation lives in {@code src/atakShared}: {@code CotServiceRemote},
 * {@code IOProviderFactory} and the connection-listener plumbing are stable
 * across all ten targeted versions.
 */
public interface StreamCreator extends Creator {

    /**
     * Whether this Creator's own {@code CotServiceRemote} has bound to ATAK's
     * CoT service. The bind is asynchronous (kicked off when the Creator is
     * constructed at plugin load), so this can be {@code false} for a moment
     * after load — exactly the window the legacy buttons had. Both stream
     * demos gate on it, matching the legacy behavior of silently doing
     * nothing while disconnected.
     */
    boolean isCotServiceConnected();

    /**
     * The server-connection mission packages staged for import: the contents
     * of {@code <external-storage>/serverconnections}. Listed through ATAK's
     * {@code IOProviderFactory} so an encrypted-storage IO provider is
     * honored. Seam contract: a missing or unlistable directory yields an
     * EMPTY list (absorbs the legacy null-check on {@code File[]}).
     */
    List<File> listServerConnectionFiles();

    /**
     * Disconnect every configured TAK server stream ({@code removeStream}
     * with the {@code "**"} match-everything connect-string wildcard).
     * DESTRUCTIVE and user-visible: this drops the user's real server
     * connections — which is the demo's point, but also why
     * {@code selfCheck()} must never exercise it.
     */
    void removeAllStreams();
}
