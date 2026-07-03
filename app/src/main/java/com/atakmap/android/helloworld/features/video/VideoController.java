package com.atakmap.android.helloworld.features.video;

import android.content.Intent;

import com.atakmap.android.helloworld.features.broadcast.BroadcastCreator;

/**
 * The video feature's Controller: launches ATAK's video player on a demo
 * stream. ATAK-free — the version-sensitive {@code ConnectionEntry}
 * construction lives behind {@link VideoCreator}, and the intra-app broadcast
 * bus behind {@link BroadcastCreator}.
 */
public class VideoController {

    /** The action ATAK's video manager listens for to open its player. */
    public static final String DISPLAY_VIDEO = "com.atakmap.maps.video.DISPLAY";

    private final VideoCreator videoCreator;
    private final BroadcastCreator broadcastCreator;

    public VideoController(VideoCreator videoCreator,
            BroadcastCreator broadcastCreator) {
        this.videoCreator = videoCreator;
        this.broadcastCreator = broadcastCreator;
    }

    /**
     * Launch ATAK's video player on the Big Buck Bunny demo rtsp stream.
     *
     * <p>The "layers" extra names the {@code VideoViewLayer}s to overlay on
     * top of the player — "test-layer" is the one the map component registers
     * at load. The "cancelClose" extra is carried exactly as the legacy demo
     * sent it.
     */
    public void launchDemoVideo() {
        // ConnectionEntry moved com.atakmap.android.video -> gov.tak.api.video
        // in ATAK 5.7; VideoCreator builds it and puts it on the Intent, so
        // the versioned type never reaches this class.
        Intent i = new Intent(DISPLAY_VIDEO);
        videoCreator.attachStream(i, "big buck bunny",
                "rtsp://3.84.6.190:554/vod/mp4:BigBuckBunny_115k.mov");
        i.putExtra("layers", new String[] {
                "test-layer"
        });
        i.putExtra("cancelClose", "true");
        broadcastCreator.send(i);
    }
}
