package com.atakmap.android.helloworld.features.video;

import android.content.Intent;

import com.atakmap.android.helloworld.abstraction.SelfCheckResult;
import com.atakmap.android.helloworld.compat.VideoConnectionCompat;

/**
 * The only place a video {@code ConnectionEntry} is produced. The package move
 * ({@code com.atakmap.android.video} &le;5.6 vs {@code gov.tak.api.video} 5.7+)
 * lives in the banded internal {@link VideoConnectionCompat}
 * (src/bands/atakPre57 / src/bands/atak57plus); this impl stays in {@code src/atakShared}.
 */
public final class VideoCreatorImpl implements VideoCreator {

    @Override
    public String id() {
        return "VideoCreator";
    }

    @Override
    public void attachStream(Intent intent, String alias, String url) {
        VideoConnectionCompat.putConnectionEntry(intent, alias, url);
    }

    /**
     * Real op done and undone: builds a ConnectionEntry through the banded path
     * and attaches it to a scratch Intent that is then discarded — nothing is
     * broadcast, so a wrong band binding fails here at load with zero side effects.
     */
    @Override
    public SelfCheckResult selfCheck() {
        try {
            Intent scratch = new Intent("helloworld.selfcheck.scratch");
            VideoConnectionCompat.putConnectionEntry(scratch,
                    "helloworld.selfcheck.probe", "rtsp://127.0.0.1/selfcheck");
            if (!scratch.hasExtra("CONNECTION_ENTRY"))
                return SelfCheckResult.failed(id(),
                        "banded entry builder attached nothing", null);
            return SelfCheckResult.full(id(),
                    "built + attached a ConnectionEntry to a scratch intent (discarded)");
        } catch (Throwable t) {
            return SelfCheckResult.failed(id(), "banded ConnectionEntry path threw", t);
        }
    }
}
