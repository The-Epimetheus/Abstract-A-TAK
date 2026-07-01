package com.atakmap.android.helloworld.compat;

import android.content.Intent;

import com.atakmap.android.video.StreamManagementUtils;

import gov.tak.api.video.ConnectionEntry;

/**
 * Compatibility-band impl for ATAK &gt;= 5.7, where {@code ConnectionEntry} moved to
 * {@code gov.tak.api.video} (it was {@code com.atakmap.android.video} in &lt;=5.6 —
 * see the {@code src/atakPre57} twin). {@code StreamManagementUtils} stayed in
 * {@code com.atakmap.android.video}. Puts the entry onto the Intent directly.
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
