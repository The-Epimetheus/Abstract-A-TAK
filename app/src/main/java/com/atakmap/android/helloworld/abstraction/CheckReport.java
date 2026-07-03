package com.atakmap.android.helloworld.abstraction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * The value the load-time systems check produces: every item's graded
 * {@link SelfCheckResult} plus the tally, retained so consumers beyond Logcat
 * can assert on it (the log writer at load, an instrumented test after load).
 * ATAK-free; lives in {@code src/main}.
 */
public final class CheckReport {

    private final List<SelfCheckResult> results;
    private final Map<VerificationLevel, Integer> tally;
    private final long durationMs;

    public CheckReport(List<SelfCheckResult> results, long durationMs) {
        this.results = Collections.unmodifiableList(new ArrayList<>(results));
        this.durationMs = durationMs;
        Map<VerificationLevel, Integer> t = new EnumMap<>(VerificationLevel.class);
        for (VerificationLevel lvl : VerificationLevel.values()) {
            t.put(lvl, 0);
        }
        for (SelfCheckResult r : this.results) {
            t.put(r.level(), t.get(r.level()) + 1);
        }
        this.tally = Collections.unmodifiableMap(t);
    }

    /** Every item's result, in sweep order. */
    public List<SelfCheckResult> results() {
        return results;
    }

    /** How many items graded at {@code level}. */
    public int count(VerificationLevel level) {
        return tally.get(level);
    }

    /** True when any item graded {@link VerificationLevel#FAILED} — wrong version logic bound. */
    public boolean hasFailed() {
        return count(VerificationLevel.FAILED) > 0;
    }

    public long durationMs() {
        return durationMs;
    }

    /** The one-line summary the systems check logs. */
    public String summaryLine() {
        return "systems check: " + results.size() + " items in " + durationMs + "ms — "
                + "FULL=" + count(VerificationLevel.FULL)
                + " PARTIAL=" + count(VerificationLevel.PARTIAL)
                + " SKIPPED=" + count(VerificationLevel.SKIPPED)
                + " FAILED=" + count(VerificationLevel.FAILED);
    }

    @Override
    public String toString() {
        return summaryLine();
    }
}
