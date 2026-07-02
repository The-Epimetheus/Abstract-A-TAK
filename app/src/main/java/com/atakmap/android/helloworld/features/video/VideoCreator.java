package com.atakmap.android.helloworld.features.video;

import android.content.Intent;

import com.atakmap.android.helloworld.abstraction.Creator;

/**
 * Abstraction over ATAK's video-stream connection handoff. Version-sensitive:
 * {@code ConnectionEntry} moved from {@code com.atakmap.android.video} to
 * {@code gov.tak.api.video} in ATAK 5.7 — absorbed by the atakPre57/atak57plus
 * compatibility band behind this seam.
 *
 * <p>Interface in {@code src/main} (ATAK-free — {@link Intent} is Android); the
 * implementation lives in {@code src/atakShared} and delegates the versioned
 * entry construction to the banded internal helper.
 */
public interface VideoCreator extends Creator {

    /**
     * Build a stream connection entry for {@code url} (e.g. an rtsp:// URI)
     * under the given alias and attach it to {@code intent} so the intent can
     * launch ATAK's video player. The versioned {@code ConnectionEntry} type
     * never crosses this seam.
     */
    void attachStream(Intent intent, String alias, String url);
}
