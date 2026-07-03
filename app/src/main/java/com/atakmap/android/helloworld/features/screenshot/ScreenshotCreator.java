package com.atakmap.android.helloworld.features.screenshot;

import com.atakmap.android.helloworld.abstraction.Creator;

/**
 * Abstraction over ATAK's high-quality map capture workflow
 * ({@code TileCapture} + {@code ImageryCaptureTask} + {@code MapItemCapturePP}
 * + {@code TiledCanvas}) — a capture pipeline that renders map imagery tile by
 * tile and then paints the map items on top, so very large captures (2x, 3x
 * display resolution) never hold the whole image in memory. All four types are
 * ATAK SDK types that must not leak into {@code src/main}. Interface here;
 * implementation in {@code src/atakShared}.
 */
public interface ScreenshotCreator extends Creator {

    /**
     * Kick off an asynchronous high-quality capture of the current map
     * viewport (whatever the user is looking at, at 1x display resolution).
     * When the capture finishes it writes
     * {@code HelloWorld-Screenshot-Example.jpg} under ATAK's {@code tmp}
     * directory and toasts the finished file path itself — the caller only
     * needs to announce that the capture started. No-op (logged) if the map
     * view has not been created yet.
     */
    void captureMapScreenshot();
}
