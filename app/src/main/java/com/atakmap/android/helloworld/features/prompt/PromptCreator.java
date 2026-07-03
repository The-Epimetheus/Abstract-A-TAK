package com.atakmap.android.helloworld.features.prompt;

import com.atakmap.android.helloworld.abstraction.Creator;

/**
 * Abstraction over ATAK's {@code TextContainer} — the single shared
 * top-of-screen prompt line ATAK's own tools use for "now do X" instructions.
 * A small seam, but {@code TextContainer} is an ATAK widget type
 * ({@code com.atakmap.android.toolbar.widgets}) that must not leak into
 * {@code src/main}. Interface here; implementation in {@code src/atakShared}.
 *
 * <p>Contract quirk worth knowing: the container is one global slot, so
 * displaying a prompt replaces whatever any other tool was showing, and
 * {@link #closePrompt()} closes it no matter who opened it.
 */
public interface PromptCreator extends Creator {

    /**
     * Show {@code text} in the top-of-screen prompt line, replacing any
     * prompt currently showing. Stays up until replaced or closed.
     */
    void displayPrompt(String text);

    /**
     * Close the prompt line (whoever opened it). Harmless no-op when none is
     * showing.
     */
    void closePrompt();
}
