package com.atakmap.android.helloworld.abstraction;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * The load-time systems check: a developer sanity check that runs on EVERY plugin
 * load (all builds, field included) to confirm the correct per-version logic is
 * bound. It is invisible to end users — results go to the log only, no UI — and is
 * never fatal to ATAK.
 *
 * <p>It walks every {@link Creator}'s {@link Creator#selfCheck()} and every
 * {@link ShellProbe}, aggregates the graded {@link VerificationLevel}s into a
 * {@link CheckReport}, logs a summary plus one line per item, and retains the
 * report so a second consumer (an instrumented test, after load) can assert on
 * it. A {@link VerificationLevel#FAILED} result is the signal that this build's
 * logic does not match the host it is running on.
 *
 * <p>ATAK-free (uses only {@code android.util.Log} + {@code java.*}); lives in
 * {@code src/main}. The {@code Set}s are supplied by Dagger multibindings.
 */
public final class SystemsCheck {

    private static final String TAG = "HelloWorldSystemsCheck";

    /**
     * The most recent {@link #run()}'s report. Static because instrumentation
     * reaches the plugin's classloader (via the espresso harness's
     * ClassLoaderReplacer) but not the graph instance the composition root
     * built — this is the one sanctioned side channel to the load-run's result.
     */
    private static volatile CheckReport latestReport;

    private final Set<Creator> creators;
    private final Set<ShellProbe> shellProbes;

    public SystemsCheck(Set<Creator> creators, Set<ShellProbe> shellProbes) {
        this.creators = creators;
        this.shellProbes = shellProbes;
    }

    /** The report of the most recent sweep in this process, or null if none ran yet. */
    public static CheckReport latestReport() {
        return latestReport;
    }

    /** Run the whole sweep, log it, and return (and retain) the report. Never throws. */
    public CheckReport run() {
        long start = System.currentTimeMillis();
        List<SelfCheckResult> results = new ArrayList<>();

        for (Creator c : creators) {
            results.add(safeSelfCheck(c));
        }
        for (ShellProbe p : shellProbes) {
            results.add(safeProbe(p));
        }

        CheckReport report = new CheckReport(results,
                System.currentTimeMillis() - start);
        latestReport = report;

        if (report.hasFailed()) {
            Log.e(TAG, report.summaryLine());
        } else {
            Log.i(TAG, report.summaryLine());
        }
        for (SelfCheckResult r : report.results()) {
            if (r.level().isFailure()) {
                Log.e(TAG, "  " + r, r.error());
            } else {
                Log.i(TAG, "  " + r);
            }
        }
        return report;
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
