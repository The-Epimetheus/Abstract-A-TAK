package com.atakmap.android.helloworld.features.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.atakmap.android.helloworld.abstraction.Disposable;
import com.atakmap.android.helloworld.abstraction.SelfCheckResult;
import com.atakmap.android.helloworld.abstraction.SelfChecks;
import com.atakmap.android.ipc.AtakBroadcast;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The only place {@code AtakBroadcast} is touched — outbound sends, plugin
 * receiver registrations, and inbound listen registrations. Source-stable
 * across all targeted versions → shared impl source set.
 */
public final class BroadcastCreatorImpl implements BroadcastCreator {

    private static final String TAG = "BroadcastCreatorImpl";

    /** Reserved test-artifact namespace (see CONTEXT.md, load-time systems check). */
    private static final String TEST_ACTION = "com.atakmap.android.helloworld.test.SYSTEMS_CHECK_PING";

    @Override
    public String id() {
        return "BroadcastCreator";
    }

    @Override
    public void send(Intent intent) {
        AtakBroadcast.getInstance().sendBroadcast(intent);
    }

    @Override
    public Disposable register(BroadcastReceiver receiver, String action,
            String description) {
        AtakBroadcast.getInstance().registerReceiver(receiver,
                new AtakBroadcast.DocumentedIntentFilter(action, description));
        // Idempotent handle: the guard makes double-dispose a no-op, and the
        // catch-all mirrors the legacy receiver's defensive unregister (a
        // failed unregister must never break plugin unload).
        final AtomicBoolean disposed = new AtomicBoolean(false);
        return () -> {
            if (!disposed.compareAndSet(false, true))
                return;
            try {
                AtakBroadcast.getInstance().unregisterReceiver(receiver);
            } catch (Exception e) {
                Log.e(TAG, "error unregistering " + action, e);
            }
        };
    }

    @Override
    public Registration listen(String action, String description,
            final BroadcastPort port) {
        final BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                port.onBroadcast(intent);
            }
        };
        AtakBroadcast.getInstance().registerReceiver(receiver,
                new AtakBroadcast.DocumentedIntentFilter(action, description));
        return new Registration() {
            private boolean closed;

            @Override
            public synchronized void close() {
                if (closed)
                    return;
                closed = true;
                // The flag keeps close() idempotent as the seam promises —
                // a second unregister of the same receiver is undefined.
                AtakBroadcast.getInstance().unregisterReceiver(receiver);
            }
        };
    }

    /**
     * Exercises the impl's own {@link #listen} and {@link #send} paths under
     * the reserved test namespace, closing the registration in a finally.
     * PARTIAL because delivery is posted asynchronously — receipt cannot be
     * confirmed inline without blocking the load.
     */
    @Override
    public SelfCheckResult selfCheck() {
        return SelfChecks.attempt(id(), "broadcast bus path threw", () -> {
            if (AtakBroadcast.getInstance() == null)
                return SelfCheckResult.skipped(id(), "AtakBroadcast not ready");
            Registration registration = listen(TEST_ACTION,
                    "systems-check probe (reserved test namespace)",
                    intent -> {
                        // Receipt is async; nothing to do — registration + send
                        // exercising the real bus is the point.
                    });
            try {
                send(new Intent(TEST_ACTION));
            } finally {
                registration.close();
            }
            return SelfCheckResult.partial(id(),
                    "listen-registered + sent + closed on the real bus; delivery is async");
        });
    }
}
