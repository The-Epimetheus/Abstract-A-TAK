package com.atakmap.android.helloworld.features.prompt;

import com.atakmap.android.helloworld.abstraction.SelfCheckResult;
import com.atakmap.android.helloworld.abstraction.SelfChecks;
import com.atakmap.android.toolbar.widgets.TextContainer;

/**
 * The only place ATAK's {@code TextContainer} is touched.
 * {@code getTopInstance()} (not {@code getInstance()}) is the container bound
 * to the top-level map widget hierarchy — the one the legacy bump demo
 * prompted through. The container is a single global slot shared with ATAK's
 * own tools: display replaces whatever is showing, close closes
 * unconditionally.
 */
public final class PromptCreatorImpl implements PromptCreator {

    @Override
    public String id() {
        return "PromptCreator";
    }

    @Override
    public void displayPrompt(String text) {
        TextContainer.getTopInstance().displayPrompt(text);
    }

    @Override
    public void closePrompt() {
        TextContainer.getTopInstance().closePrompt();
    }

    /**
     * PARTIAL by design: displaying a real prompt would flash user-visible
     * text over the map, so the probe only resolves the top
     * {@code TextContainer} — the ATAK symbol this impl links against.
     */
    @Override
    public SelfCheckResult selfCheck() {
        return SelfChecks.attempt(id(), "TextContainer resolution threw",
                () -> {
                    TextContainer container = TextContainer.getTopInstance();
                    if (container == null)
                        return SelfCheckResult.skipped(id(),
                                "TextContainer top instance not ready");
                    return SelfCheckResult.partial(id(),
                            "top TextContainer resolved; no prompt displayed"
                                    + " (would be user-visible)");
                });
    }
}
