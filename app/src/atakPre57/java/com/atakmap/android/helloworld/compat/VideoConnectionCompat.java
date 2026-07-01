package com.atakmap.android.helloworld.compat;

import android.content.Intent;

import com.atakmap.android.video.ConnectionEntry;
import com.atakmap.android.video.StreamManagementUtils;

/**
 * Compatibility-band impl for ATAK &lt;= 5.6, where {@code ConnectionEntry} lives in
 * {@code com.atakmap.android.video}. In 5.7+ it moved to {@code gov.tak.api.video}
 * (see the {@code src/atak57plus} twin). The helper puts the entry onto the Intent
 * itself so the caller (a core class in src/main) never names the versioned type.
 */
public final class VideoConnectionCompat {

    private VideoConnectionCompat() {}

    public static void putConnectionEntry(Intent i) {
        ConnectionEntry ce = StreamManagementUtils.createConnectionEntryFromUrl(
                "big buck bunny",
                "rtsp://3.84.6.190:554/vod/mp4:BigBuckBunny_115k.mov");
        i.putExtra("CONNECTION_ENTRY", ce);
    }
}
