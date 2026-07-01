package com.atakmap.android.helloworld.abstraction;

import android.util.Log;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The load-time systems check: a developer sanity check that runs on EVERY plugin
 * load (all builds, field included) to confirm the correct per-version logic is
 * bound. It is invisible to end users — results go to the log only, no UI — and is
 * never fatal to ATAK.
 *
 * <p>It walks every {@link Creator}'s {@link Creator#selfCheck()} and every
 * {@link ShellProbe}, aggregates the graded {@link VerificationLevel}s, and logs a
 * summary plus one line per item. A {@link VerificationLevel#FAILED} result is the
 * signal that this build's logic does not match the host it is running on.
 *
 * <p>ATAK-free (uses only {@code android.util.Log} + {@code java.*}); lives in
 * {@code src/main}. The {@code Set}s are supplied by Dagger multibindings.
 */
public final class SystemsCheck {

    private static final String TAG = "HelloWorldSystemsCheck";

    private final Set<Creator> creators;
    private final Set<ShellProbe> shellProbes;

    public SystemsCheck(Set<Creator> creators, Set<ShellProbe> shellProbes) {
        this.creators = creators;
        this.shellProbes = shellProbes;
    }

    /** Run the whole sweep. Never throws. */
    public void run() {
        long start = System.currentTimeMillis();
        List<SelfCheckResult> results = new ArrayList<>();

        for (Creator c : creators) {
            results.add(safeSelfCheck(c));
        }
        for (ShellProbe p : shellProbes) {
            results.add(safeProbe(p));
        }

        Map<VerificationLevel, Integer> tally = new EnumMap<>(VerificationLevel.class);
        for (VerificationLevel lvl : VerificationLevel.values()) {
            tally.put(lvl, 0);
        }
        for (SelfCheckResult r : results) {
            tally.put(r.level(), tally.get(r.level()) + 1);
        }

        long ms = System.currentTimeMillis() - start;
        String summary = "systems check: " + results.size() + " items in " + ms + "ms — "
                + "FULL=" + tally.get(VerificationLevel.FULL)
                + " PARTIAL=" + tally.get(VerificationLevel.PARTIAL)
                + " SKIPPED=" + tally.get(VerificationLevel.SKIPPED)
                + " FAILED=" + tally.get(VerificationLevel.FAILED);

        if (tally.get(VerificationLevel.FAILED) > 0) {
            Log.e(TAG, summary);
        } else {
            Log.i(TAG, summary);
        }
        for (SelfCheckResult r : results) {
            if (r.level().isFailure()) {
                Log.e(TAG, "  " + r, r.error());
            } else {
                Log.i(TAG, "  " + r);
            }
        }
    }

    private SelfCheckResult safeSelfCheck(Creator c) {
        try {
            SelfCheckResult r = c.selfCheck();
            return r != null ? r : SelfCheckResult.skipped(c.id(), "selfCheck returned null");
        } catch (Throwable t) {
            // selfCheck is contractually not supposed to throw; guard anyway.
            return SelfCheckResult.failed(c.id(), "selfCheck threw", t);
        }
    }

    private SelfCheckResult safeProbe(ShellProbe p) {
        try {
            SelfCheckResult r = p.probe();
            return r != null ? r : SelfCheckResult.skipped(p.id(), "probe returned null");
        } catch (Throwable t) {
            return SelfCheckResult.failed(p.id(), "shell probe threw", t);
        }
    }
}
