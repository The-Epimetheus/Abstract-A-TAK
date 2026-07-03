package com.atakmap.android.helloworld.features.lrf;

import com.atakmap.android.helloworld.abstraction.Creator;

/**
 * Abstraction over ATAK's laser-range-finder plumbing: the tool-manager
 * registration/lifecycle of the plugin's {@code LRFPointTool} and the
 * {@code com.atakmap.android.lrf.LocalRangeFinderInput} singleton that feeds
 * readings to whoever registered a {@code RangeFinderAction}.
 *
 * <p>Partly an insurance seam: {@code startTool}/{@code endCurrentTool} add
 * little over {@code ToolManagerBroadcastReceiver}, but the {@code lrf}
 * package is a niche corner of the SDK (hardware-integration surface, exactly
 * the kind that gets reshaped without much ceremony), and the seam also keeps
 * {@code GeoCalculations}/{@code CoordinatedTime} out of {@code src/main}.
 * Interface here; implementation in {@code src/atakShared}.
 */
public interface LrfCreator extends Creator {

    /**
     * Construct and register the plugin's LRF point tool with ATAK's tool
     * manager, adapting its callback onto {@code port}. Idempotent while
     * registered. The tool stays registered until {@link #disposePointTool()}
     * — starting it is a separate step ({@link #startPointTool()}).
     */
    void registerPointTool(LrfPort port);

    /**
     * Dispose the point tool registered by {@link #registerPointTool}.
     * Idempotent; safe to call when never registered.
     */
    void disposePointTool();

    /**
     * Arm the LRF point tool (ATAK shows its "Fire Laser Range Finder"
     * prompt; the tool registers for range-finder input until a reading
     * arrives or the tool is ended).
     */
    void startPointTool();

    /**
     * End whatever tool is currently active — legacy semantics: the demo
     * button assumes the LRF point tool is the active one.
     */
    void endCurrentTool();

    /**
     * Feed a simulated reading into ATAK's local range-finder input exactly
     * as if it had arrived from LRF hardware, so every registered listener
     * (not just this plugin's tool) sees it.
     *
     * @param distance  range in meters
     * @param azimuth   azimuth in degrees
     * @param elevation elevation/inclination in degrees
     */
    void simulateRangeFinderReading(double distance, double azimuth,
            double elevation);
}
