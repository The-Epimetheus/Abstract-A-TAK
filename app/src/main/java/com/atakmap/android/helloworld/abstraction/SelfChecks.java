package com.atakmap.android.helloworld.abstraction;

/**
 * Guard scaffolding for {@link Creator#selfCheck()} implementations. It owns
 * ONLY the catch-everything → {@code FAILED} edge; the probe body still returns
 * a graded {@link SelfCheckResult} itself (FULL / PARTIAL / SKIPPED), because
 * that grading — degrade where irreversible, skip where unsafe — is the point
 * of a selfCheck and must stay visible in each impl.
 */
public final class SelfChecks {

    /** A selfCheck probe body: does the real thing, returns its graded result. */
    public interface Probe {
        SelfCheckResult run() throws Exception;
    }

    /**
     * Run {@code probe}, translating anything thrown (including missing-symbol
     * {@link LinkageError}s — the wrong-version-bound signal) into a
     * {@code FAILED} result carrying {@code failureMessage}.
     */
    public static SelfCheckResult attempt(String creatorId,
            String failureMessage, Probe probe) {
        try {
            SelfCheckResult r = probe.run();
            return r != null ? r
                    : SelfCheckResult.skipped(creatorId, "probe returned null");
        } catch (Throwable t) {
            return SelfCheckResult.failed(creatorId, failureMessage, t);
        }
    }

    private SelfChecks() {
    }
}
