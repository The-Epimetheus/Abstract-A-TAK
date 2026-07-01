package com.atakmap.android.helloworld.features.cot;

import com.atakmap.android.helloworld.abstraction.Creator;

/**
 * Abstraction over Cursor-on-Target construction. Business logic asks for a CoT
 * event via a {@link CotSpec} DTO and gets back plugin-neutral data ({@code String}
 * XML) — it never touches ATAK's {@code CotEvent}/{@code CotPoint}/{@code CotDetail}.
 *
 * <p>Interface lives in {@code src/main} (ATAK-free); the implementation lives in
 * {@code src/atakShared} because the CoT construction API is byte-confirmed stable
 * across every supported ATAK version (4.10 -> 5.8).
 */
public interface CotCreator extends Creator {

    /**
     * Build a CoT event from the spec and return its serialized XML. Side-effect
     * free — nothing is dispatched — so it is safe to call anywhere, including the
     * load-time systems check.
     */
    String buildCotXml(CotSpec spec);
}
