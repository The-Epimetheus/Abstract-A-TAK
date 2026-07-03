package com.atakmap.android.helloworld.features.lrf;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.atakmap.android.helloworld.abstraction.Disposable;

/**
 * The laser-range-finder feature's Controller, ATAK-free: arming/ending the
 * LRF point tool, simulating a hardware reading, and surfacing readings as a
 * log line + toast. It is its own {@link LrfPort}; the tool-manager and
 * {@code LocalRangeFinderInput} plumbing lives behind {@link LrfCreator}.
 *
 * <p>{@link Disposable}: the point tool is a live tool-manager registration,
 * created at construction (the same moment the legacy receiver's constructor
 * created it — this Controller is built when the Humble shell is) and torn
 * down by the PaneController dispose cascade.
 */
public class LrfController implements LrfPort, Disposable {

    private static final String TAG = "LrfController";

    private final LrfCreator lrfCreator;
    private final Context pluginContext;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    /**
     * Set by the pane wiring on each attach: clears the lrfTool button's
     * toggle when a reading self-ends the tool.
     */
    private volatile Runnable onToolFinished;

    public LrfController(LrfCreator lrfCreator, Context pluginContext) {
        this.lrfCreator = lrfCreator;
        this.pluginContext = pluginContext;
        // Register the point tool up front (legacy parity: the receiver's
        // constructor did) so the tool identifier is startable before the
        // pane ever opens.
        lrfCreator.registerPointTool(this);
    }

    /** @see #onToolFinished */
    public void setOnToolFinished(Runnable onToolFinished) {
        this.onToolFinished = onToolFinished;
    }

    /** Arm the LRF point tool (ATAK prompts "Fire Laser Range Finder"). */
    public void startTool() {
        lrfCreator.startPointTool();
    }

    /** End the active tool (the demo assumes that is the LRF point tool). */
    public void endTool() {
        lrfCreator.endCurrentTool();
    }

    /**
     * Simulate an LRF hardware reading with random distance/azimuth/elevation,
     * fed through ATAK's local range-finder input so every registered listener
     * sees it. With the point tool armed, the reading comes back through
     * {@link #onRangeFinderResults}.
     */
    public void fireSimulatedReading() {
        double distance = Math.random() * 1000;
        double azimuth = Math.random() * 360;
        double elevation = Math.random() * 10;

        lrfCreator.simulateRangeFinderReading(distance, azimuth, elevation);
    }

    /* ------------- LrfPort: readings, surfaced as log + toast ------------- */

    @Override
    public void onRangeFinderResults(final double distance,
            final double azimuth, final double inclination, boolean success,
            String pointFromSelf) {
        Log.d(TAG, distance + ", " +
                azimuth + ", " +
                inclination +
                " point from self: " + pointFromSelf);
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(pluginContext, distance + ", " +
                        azimuth + ", " + inclination, Toast.LENGTH_SHORT)
                        .show();
                Runnable finished = onToolFinished;
                if (finished != null)
                    finished.run();
            }
        });
    }

    /* ------------------------------ lifecycle ----------------------------- */

    @Override
    public void dispose() {
        lrfCreator.disposePointTool();
        onToolFinished = null;
    }
}
