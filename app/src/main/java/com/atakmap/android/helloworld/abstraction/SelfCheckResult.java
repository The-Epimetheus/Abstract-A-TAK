package com.atakmap.android.helloworld.abstraction;

/**
 * The graded outcome of one {@link Creator#selfCheck()}, reported to the log by the
 * load-time systems check. ATAK-free; lives in {@code src/main}.
 */
public final class SelfCheckResult {

    private final String creatorId;
    private final VerificationLevel level;
    private final String message;
    private final Throwable error; // non-null only when level == FAILED

    private SelfCheckResult(String creatorId, VerificationLevel level, String message, Throwable error) {
        this.creatorId = creatorId;
        this.level = level;
        this.message = message;
        this.error = error;
    }

    /** Real op executed and torn down. */
    public static SelfCheckResult full(String creatorId, String message) {
        return new SelfCheckResult(creatorId, VerificationLevel.FULL, message, null);
    }

    /** Built + API resolved, but dispatch not exercised (irreversible/loopback). */
    public static SelfCheckResult partial(String creatorId, String message) {
        return new SelfCheckResult(creatorId, VerificationLevel.PARTIAL, message, null);
    }

    /** Could not be safely tested on this host. */
    public static SelfCheckResult skipped(String creatorId, String message) {
        return new SelfCheckResult(creatorId, VerificationLevel.SKIPPED, message, null);
    }

    /** The path threw or a symbol was missing — wrong version logic bound. */
    public static SelfCheckResult failed(String creatorId, String message, Throwable error) {
        return new SelfCheckResult(creatorId, VerificationLevel.FAILED, message, error);
    }

    public String creatorId() { return creatorId; }
    public VerificationLevel level() { return level; }
    public String message() { return message; }
    public Throwable error() { return error; }

    @Override
    public String toString() {
        String base = creatorId + " -> " + level + (message == null || message.isEmpty() ? "" : " (" + message + ")");
        return error == null ? base : base + " [" + error.getClass().getSimpleName() + ": " + error.getMessage() + "]";
    }
}
