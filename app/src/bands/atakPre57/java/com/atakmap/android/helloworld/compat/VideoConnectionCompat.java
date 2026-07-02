package com.atakmap.android.helloworld.compat;

import android.content.Intent;

import com.atakmap.android.video.ConnectionEntry;
import com.atakmap.android.video.StreamManagementUtils;

/**
 * Compatibility-band impl for ATAK &lt;= 5.6, where {@code ConnectionEntry} lives in
 * {@code com.atakmap.android.video}. In 5.7+ it moved to {@code gov.tak.api.video}
 * (see the {@code src/bands/atak57plus} twin). The helper puts the entry onto the Intent
 * itself so the caller never names the versioned type.
 *
 * <p>INTERNAL to the VideoCreator seam: called only from {@code VideoCreatorImpl}
 * (src/atakShared), never from {@code src/main}.
 */
public final class VideoConnectionCompat {

    private VideoConnectionCompat() {}

    public static void putConnectionEntry(Intent i, String alias, String url) {
        ConnectionEntry ce = StreamManagementUtils.createConnectionEntryFromUrl(
                alias, url);
        i.putExtra("CONNECTION_ENTRY", ce);
    }
}
