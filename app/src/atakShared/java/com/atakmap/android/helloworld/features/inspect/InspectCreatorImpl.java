package com.atakmap.android.helloworld.features.inspect;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.atakmap.android.helloworld.abstraction.SelfCheckResult;
import com.atakmap.android.helloworld.abstraction.SelfChecks;
import com.atakmap.android.importexport.CotEventFactory;
import com.atakmap.android.ipc.AtakBroadcast;
import com.atakmap.android.maps.MapItem;
import com.atakmap.android.maps.MapView;
import com.atakmap.android.toolbar.ToolManagerBroadcastReceiver;
import com.atakmap.android.util.AbstractMapItemSelectionTool;
import com.atakmap.coremap.cot.event.CotEvent;
import com.atakmap.coremap.log.Log;

/**
 * The only place ATAK's map-item selection tooling is touched. Three moving
 * parts hide behind the seam:
 * <ul>
 * <li>the {@link AbstractMapItemSelectionTool} subclass — its constructor
 * registers the tool with {@code ToolManagerBroadcastReceiver} under a string
 * identifier, and its {@code dispose()} unregisters it;</li>
 * <li>the start round trip — {@code ToolManagerBroadcastReceiver.startTool}
 * by identifier, results delivered as a "…Finished" broadcast on ATAK's
 * internal bus ({@link AtakBroadcast}), NOT as a return value;</li>
 * <li>the one-shot finish receiver, registered just before each start
 * (legacy ordering) and unregistered on first delivery.</li>
 * </ul>
 *
 * <p>Seam contract (mechanical churn absorbed): the legacy demo constructed
 * the tool eagerly in the DropDownReceiver constructor; this impl creates it
 * lazily on first {@link #startItemInspection}. Registration semantics are
 * identical — the tool only needs to exist before {@code startTool} names it.
 */
public final class InspectCreatorImpl implements InspectCreator {

    private static final String TAG = "InspectCreatorImpl";

    /** The tool's identity on the tool-manager bus (legacy strings, verbatim). */
    private static final String TOOL_IDENTIFIER = "com.atakmap.android.helloworld.InspectionMapItemSelectionTool";
    private static final String TOOL_FINISHED = "com.atakmap.android.helloworld.InspectionMapItemSelectionTool.Finished";

    /**
     * This class makes use of a compact class to aid with the selection of map items.   Prior to
     * 3.12, this all had to be manually done playing with the dispatcher and listening for map
     * events.
     */
    private static final class InspectionMapItemSelectionTool
            extends AbstractMapItemSelectionTool {
        InspectionMapItemSelectionTool(MapView mapView) {
            super(mapView,
                    TOOL_IDENTIFIER,
                    TOOL_FINISHED,
                    "Select Map Item on the screen",
                    "Invalid Selection");
        }

        @Override
        protected boolean isItem(MapItem mi) {
            return true;
        }

    }

    /** Lazily created on first start; lives until {@link #disposeTool()}. */
    private InspectionMapItemSelectionTool tool;

    /** Non-null only while a pick is in flight (one-shot, self-unregistering). */
    private BroadcastReceiver finishedReceiver;

    @Override
    public String id() {
        return "InspectCreator";
    }

    @Override
    public void startItemInspection(final InspectionListener listener) {
        if (finishedReceiver != null) {
            // One inspection at a time. The pane's toggle button makes this
            // unreachable in practice; the guard keeps the seam safe anyway.
            Log.d(TAG, "inspection already running, ignoring start");
            return;
        }

        if (tool == null)
            tool = new InspectionMapItemSelectionTool(MapView.getMapView());

        // Register for the finished broadcast BEFORE starting the tool
        // (legacy ordering) so a fast selection cannot slip past us.
        finishedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // One-shot: the tool ended (selection made, invalid, or
                // cancelled) — same broadcast either way; only a real
                // selection carries a "uid" extra.
                AtakBroadcast.getInstance().unregisterReceiver(this);
                finishedReceiver = null;
                listener.onInspectionFinished(
                        inspect(intent.getStringExtra("uid")));
            }
        };
        AtakBroadcast.getInstance().registerReceiver(
                finishedReceiver,
                new AtakBroadcast.DocumentedIntentFilter(TOOL_FINISHED));
        Bundle extras = new Bundle();
        ToolManagerBroadcastReceiver.getInstance().startTool(
                TOOL_IDENTIFIER,
                extras);
    }

    @Override
    public void stopItemInspection() {
        // requestEndTool() makes the tool broadcast its finished intent; the
        // one-shot receiver above unregisters itself and fires the listener
        // (with a null result). The legacy demo relied on the same round trip
        // for its cleanup — there is no synchronous cancel path.
        if (tool != null)
            tool.requestEndTool();
    }

    @Override
    public void disposeTool() {
        // Legacy bug fixed behind the seam: the old receiver never
        // unregistered a still-armed finish listener on dispose, leaking the
        // registration if the plugin unloaded mid-pick.
        if (finishedReceiver != null) {
            AtakBroadcast.getInstance().unregisterReceiver(finishedReceiver);
            finishedReceiver = null;
        }
        if (tool != null) {
            tool.dispose();
            tool = null;
        }
    }

    /**
     * Resolve a finished pick into a plugin-owned result. Null when nothing
     * (or nothing resolvable) was selected — the legacy demo silently did
     * nothing in those cases too.
     */
    private InspectionResult inspect(String uid) {
        if (uid == null)
            return null;

        MapItem mi = MapView.getMapView().getMapItem(uid);

        if (mi == null)
            return null;

        Log.d(TAG, "class: " + mi.getClass());
        Log.d(TAG, "type: " + mi.getType());

        // Not every item converts: "nevercot" items are deliberately never
        // persisted or shared as CoT; anything else that returns null here is
        // a genuine conversion failure.
        final CotEvent cotEvent = CotEventFactory
                .createCotEvent(mi);

        return new InspectionResult(
                cotEvent != null ? cotEvent.toString() : null,
                mi.hasMetaValue("nevercot"));
    }

    /**
     * PARTIAL by design: constructing an {@link AbstractMapItemSelectionTool}
     * exercises the real tool-registration path (its constructor registers
     * with ToolManagerBroadcastReceiver, dispose unregisters), but actually
     * STARTING a tool is user-visible — it posts the on-screen selection
     * prompt and hijacks map interaction — so the probe stops short of that.
     * The throwaway tool lives in the reserved test namespace and is disposed
     * in a finally.
     */
    @Override
    public SelfCheckResult selfCheck() {
        return SelfChecks.attempt(id(), "selection tool plumbing threw",
                () -> {
                    MapView mv = MapView.getMapView();
                    if (mv == null)
                        return SelfCheckResult.skipped(id(),
                                "MapView not ready");
                    if (ToolManagerBroadcastReceiver.getInstance() == null)
                        return SelfCheckResult.skipped(id(),
                                "ToolManagerBroadcastReceiver not ready");

                    AbstractMapItemSelectionTool probe = new AbstractMapItemSelectionTool(
                            mv,
                            "com.atakmap.android.helloworld.test.InspectionMapItemSelectionTool",
                            "com.atakmap.android.helloworld.test.InspectionMapItemSelectionTool.Finished",
                            "test prompt", "test invalid") {
                        @Override
                        protected boolean isItem(MapItem mi) {
                            return true;
                        }
                    };
                    try {
                        // Construction registered the probe tool under the
                        // test identifier — the same path the real tool takes.
                        return SelfCheckResult.partial(id(),
                                "selection tool registered+disposed under test id; "
                                        + "tool not started (on-screen prompt is user-visible)");
                    } finally {
                        probe.dispose();
                    }
                });
    }
}
