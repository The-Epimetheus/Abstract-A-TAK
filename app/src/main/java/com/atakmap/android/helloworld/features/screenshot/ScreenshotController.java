package com.atakmap.android.helloworld.features.screenshot;

import android.content.Context;
import android.widget.Toast;

import com.atakmap.android.helloworld.plugin.R;

/**
 * The map-screenshot feature's Controller: a thin pass-through (every feature
 * gets one — ADR-0005) that starts the capture workflow behind
 * {@link ScreenshotCreator} and toasts that it started. The workflow itself
 * toasts the finished output path when the asynchronous capture completes.
 */
public class ScreenshotController {

    private final ScreenshotCreator screenshotCreator;
    private final Context pluginContext;

    public ScreenshotController(ScreenshotCreator screenshotCreator,
            Context pluginContext) {
        this.screenshotCreator = screenshotCreator;
        this.pluginContext = pluginContext;
    }

    /**
     * Start the asynchronous high-quality capture of the current map
     * viewport, then toast that it started ("finished" — with the output
     * path — is toasted by the capture workflow itself).
     */
    public void captureMapScreenshot() {
        screenshotCreator.captureMapScreenshot();
        Toast.makeText(pluginContext, pluginContext.getString(
                R.string.map_screenshot_started),
                Toast.LENGTH_LONG).show();
    }
}
