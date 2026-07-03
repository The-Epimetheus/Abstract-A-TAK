package com.atakmap.android.helloworld.features.broadcast;

import android.content.BroadcastReceiver;
import android.content.Intent;

import com.atakmap.android.helloworld.abstraction.Creator;
import com.atakmap.android.helloworld.abstraction.Disposable;

/**
 * Abstraction over ATAK's intra-app broadcast bus ({@code AtakBroadcast}).
 * Stable across every targeted version today, so this is an insurance seam
 * (see CONTEXT.md): many features signal ATAK components by broadcast, and a
 * future bus change lands in one impl. Interface in {@code src/main}
 * (ATAK-free — {@link Intent} is Android); the implementation lives in
 * {@code src/atakShared}.
 */
public interface BroadcastCreator extends Creator {

    /** Send {@code intent} on ATAK's internal broadcast bus. */
    void send(Intent intent);

    /**
     * Register {@code receiver} for {@code action} on ATAK's internal
     * broadcast bus ({@link BroadcastReceiver} is Android — boundary-legal).
     * ATAK wants every registration documented; {@code description} feeds
     * that documentation. Returns a {@link Disposable} whose idempotent
     * {@code dispose()} unregisters — the caller owns the registration's
     * lifetime (ATAK hot-reloads plugins; a leaked receiver survives into
     * the next load).
     */
    Disposable register(BroadcastReceiver receiver, String action,
            String description);

    /**
     * A live {@link #listen} registration. {@link #close()} unregisters the
     * port; idempotent, so a {@code Disposable} Controller may call it
     * unconditionally. ATAK hot-reloads plugins — a leaked registration
     * survives into the next load — so every listener must be closed on
     * dispose. Extends {@link AutoCloseable} minus the checked exception.
     */
    interface Registration extends AutoCloseable {
        @Override
        void close();
    }

    /**
     * Register {@code port} for broadcasts of {@code action} on ATAK's
     * internal bus. {@code description} feeds ATAK's DocumentedIntentFilter —
     * the bus's self-documentation of who listens for what — so say what the
     * listener does, not just the action name.
     *
     * @return the live registration; close it when done listening.
     */
    Registration listen(String action, String description, BroadcastPort port);
}
