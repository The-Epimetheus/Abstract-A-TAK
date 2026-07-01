package com.atakmap.android.helloworld.abstraction;

/**
 * How deeply a {@link Creator#selfCheck()} was able to verify its ATAK code path
 * at load time. selfCheck is best-effort: it does as much of the real work as is
 * safe on the running host and reports the level it reached rather than a bare
 * pass/fail.
 *
 * <p>This type is part of the ATAK-free boundary: it names no ATAK type and lives
 * in {@code src/main}.
 */
public enum VerificationLevel {

    /** Real operation was executed against ATAK and torn down. Full proof. */
    FULL,

    /**
     * The object was built and its ATAK API resolved, but the effect was not
     * committed because it is irreversible/external (e.g. a CoT that would leave
     * the device), or it was routed to a loopback seam. Dispatch itself unproven.
     */
    PARTIAL,

    /** Could not be safely tested on this host at all; said so rather than leaking. */
    SKIPPED,

    /**
     * The real path threw or an expected ATAK symbol was missing — the signal that
     * the wrong version logic is bound for this host.
     */
    FAILED;

    /** True only for FAILED — the level the systems check should surface loudly. */
    public boolean isFailure() {
        return this == FAILED;
    }
}
