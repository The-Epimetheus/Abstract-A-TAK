package com.atakmap.android.helloworld.features.speech;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.atakmap.android.helloworld.features.broadcast.BroadcastCreator;
import com.atakmap.android.helloworld.features.camera.CameraCreator;
import com.atakmap.android.helloworld.features.emergency.EmergencyCreator;
import com.atakmap.android.helloworld.features.marker.MarkerCreator;
import com.atakmap.android.helloworld.features.marker.MarkerSpec;
import com.atakmap.android.helloworld.speechtotext.SpeechToActivity;

import java.util.HashMap;

/**
 * The speech feature's Controller. Launches the plugin's speech-to-intent
 * activity ({@link SpeechToActivity}) and holds the demo behavior: the
 * dispatch switch that turns each recognized activity bundle into a map
 * action, and the MGRS speech-marker builder from the retired
 * speech-to-text path.
 *
 * <p>ATAK-free like every Controller, with one honest asymmetry: the speech
 * action classes ({@code speechtotext/*}) are unmigrated debt that demand a
 * raw ATAK {@code MapView} at construction, so every case that fires one
 * hops through {@link SpeechCreator} instead of doing the work here.
 * {@link SpeechToActivity} itself is plugin-owned (boundary-legal to name)
 * and runs outside the ATAK classloader, so this class may use its bundle
 * constants and its one-shot result listener directly.
 */
public class SpeechController {

    private static final String TAG = "SpeechController";

    private final SpeechCreator speechCreator;
    private final MarkerCreator markerCreator;
    private final CameraCreator cameraCreator;
    private final BroadcastCreator broadcastCreator;
    private final EmergencyCreator emergencyCreator;
    private final Context pluginContext;

    /**
     * This receives the intent from SpeechToActivity.
     * It uses the info from the activityInfoBundle to decide
     * what to do next. The bundle always contains a destination
     * and an activity intent. Other stuff is added on a case-by-case basis.
     * See SpeechToActivity for more details.
     *
     * <p>The listener is one-shot: it unregisters itself after delivering a
     * result, and {@link #launchSpeechToActivity} re-registers it on every
     * tap (legacy behavior, preserved). It exposes no unregister API, so if
     * the plugin unloads between tap and result the registration leaks —
     * exactly as the legacy receiver leaked it; fixing that means touching
     * the deferred speech classes.
     */
    private final SpeechToActivity.SpeechDataListener speechListener = new SpeechToActivity.SpeechDataListener();
    private final SpeechToActivity.SpeechDataReceiver speechReceiver = this::onSpeechResult;

    public SpeechController(SpeechCreator speechCreator,
            MarkerCreator markerCreator,
            CameraCreator cameraCreator,
            BroadcastCreator broadcastCreator,
            EmergencyCreator emergencyCreator,
            Context pluginContext) {
        this.speechCreator = speechCreator;
        this.markerCreator = markerCreator;
        this.cameraCreator = cameraCreator;
        this.broadcastCreator = broadcastCreator;
        this.emergencyCreator = emergencyCreator;
        this.pluginContext = pluginContext;
    }

    /**
     * Register the one-shot result listener and launch the speech-to-intent
     * activity.
     *
     * @param hostContext the host activity context from the pane wiring
     *            ({@code shell.hostContext()}) — the same object the legacy
     *            receiver cached as {@code (Activity) mapView.getContext()},
     *            needed both for the receiver registration and for
     *            {@code startActivityForResult}.
     */
    public void launchSpeechToActivity(Context hostContext) {
        speechListener.register(hostContext, speechReceiver);
        // this makes use of an activity that cannot know anything
        // about ATAK.   This is the same problem as we have with
        // notifications.  They run outside of the current ATAK
        // classloader paradigm.
        Intent intent = new Intent();
        intent.setClassName("com.atakmap.android.helloworld.plugin",
                "com.atakmap.android.helloworld.speechtotext.SpeechToActivity");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("EXTRA_MESSAGE", "");
        ((Activity) hostContext).startActivityForResult(intent, 0);
    }

    /**
     * This receives the activityInfoBundle from SpeechToActivity. The switch
     * case decides what to call — verbatim from the legacy receiver; the
     * ATAK-touching cases dispatch through {@link SpeechCreator}.
     *
     * @param activityInfoBundle - Bundle containing the activity intent,
     *            destination, origin, marker type and more.
     */
    public void onSpeechResult(Bundle activityInfoBundle) {
        switch (activityInfoBundle
                .getInt(SpeechToActivity.ACTIVITY_INTENT)) {
            //This case is for drawing and navigating routes
            case SpeechToActivity.NAVIGATE_INTENT:
                speechCreator.navigateTo(
                        activityInfoBundle
                                .getString(SpeechToActivity.DESTINATION),
                        activityInfoBundle
                                .getBoolean(SpeechToActivity.QUICK_INTENT));
                break;
            // This case is for plotting down markers
            case SpeechToActivity.PLOT_INTENT:
                speechCreator.plotPoint(activityInfoBundle
                        .getString(SpeechToActivity.DESTINATION));
                break;
            //This case is for bloodhounding to markers,routes, or addresses
            case SpeechToActivity.BLOODHOUND_INTENT:
                speechCreator.bloodhound(activityInfoBundle
                        .getString(SpeechToActivity.DESTINATION));
                break;
            //This case is for launching the 9 Line window on a target
            case SpeechToActivity.NINE_LINE_INTENT:
                speechCreator.openNineLine(activityInfoBundle
                        .getString(SpeechToActivity.DESTINATION));
                break;
            //DOESNT WORK//This case is to open the compass on your self marker
            case SpeechToActivity.COMPASS_INTENT:
                speechCreator.openCompassOnSelf();
                break;
            //This case toggles the brightness slider
            case SpeechToActivity.BRIGHTNESS_INTENT:
                speechCreator.adjustBrightness(activityInfoBundle
                        .getString(SpeechToActivity.DESTINATION));
                break;
            //this case deletes a shape, marker, or route
            case SpeechToActivity.DELETE_INTENT:
                speechCreator.deleteItem(activityInfoBundle
                        .getString(SpeechToActivity.DESTINATION));
                break;
            //this case opens the hostiles window from fire tools
            case SpeechToActivity.SHOW_HOSTILES_INTENT:
                broadcastCreator.send(new Intent().setAction(
                        "com.atakmap.android.maps.MANAGE_HOSTILES"));
                break;
            //This case opens a markers detail menu
            case SpeechToActivity.OPEN_DETAILS_INTENT:
                speechCreator.openDetails(activityInfoBundle
                        .getString(SpeechToActivity.DESTINATION));
                break;
            //this case starts an emergency
            case SpeechToActivity.EMERGENCY_INTENT:
                emergencyCreator.startAlertOfType(activityInfoBundle
                        .getString(SpeechToActivity.EMERGENCY_TYPE));
                break;
            //This case draws a R&B line between 2 map items
            case SpeechToActivity.LINK_INTENT:
                speechCreator.linkItems(activityInfoBundle
                        .getString(SpeechToActivity.DESTINATION));
                break;
            //This case launches the camera
            case SpeechToActivity.CAMERA_INTENT:
                speechCreator.launchQuickPic();
                break;
            default:
                // Legacy toasted on the host context; the plugin context
                // works for toasts (RouteController idiom) and keeps this
                // class free of a retained host reference.
                Toast.makeText(pluginContext,
                        "I did not understand please try again",
                        Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Build a unit marker from a spoken MGRS coordinate and fly the camera
     * to it — the handler of the retired speech-to-text path, migrated for
     * its teaching value. Its launcher (the {@code speechToText} button) was
     * already commented out in the legacy receiver ("functionality
     * implemented into SpeechToActivity: SpeechPointDropper specifically"),
     * so nothing invokes this today; it stays public so a revived
     * {@code SpeechToTextActivity} flow can call straight into it.
     *
     * @param s recognized speech fields: {@code numericGrid},
     *            {@code alphaGrid}, {@code squareID}, {@code easting},
     *            {@code northing}, and {@code markerType} (whose first
     *            letter — U/N/F/H — selects the CoT affiliation).
     */
    public void createSpeechMarker(HashMap<String, String> s) {
        final double[] mgrsPoint;
        try {
            mgrsPoint = speechCreator.mgrsToLatLon(
                    s.get("numericGrid") + s.get("alphaGrid"),
                    s.get("squareID"),
                    s.get("easting"),
                    s.get("northing"));
        } catch (IllegalArgumentException e) {
            String msg = "An error has occurred getting the MGRS point";
            Log.e(TAG, msg, e);
            Toast.makeText(pluginContext, msg, Toast.LENGTH_LONG).show();
            return;
        }

        final String type;
        switch (s.get("markerType").charAt(0)) {
            case 'U':
                type = "a-u-G-U-C-F";
                break;
            case 'N':
                type = "a-n-G-U-C-F";
                break;
            case 'F':
                type = "a-f-G-U-C-F";
                break;
            case 'H':
            default:
                type = "a-h-G-U-C-F";
                break;
        }

        // Legacy started this fly-to thread before finishing the marker; the
        // zoom-then-pan pair and the trailing one-second sleep are verbatim.
        new Thread(new Runnable() {
            public void run() {
                cameraCreator.zoomTo(.00001d, false);
                cameraCreator.panTo(mgrsPoint[0], mgrsPoint[1], false);

                try {
                    Thread.sleep(1000);
                } catch (Exception ignored) {
                }

            }
        }).start();

        // persist() (not persistAndAnnounce()) so the impl persists AFTER
        // the meta fields land — the legacy order; the COT_PLACED
        // announcement is then sent from here, exactly as the legacy block
        // finished.
        String uid = markerCreator.placeMarker(MarkerSpec
                .at(mgrsPoint[0], mgrsPoint[1])
                .type(type)
                .title("Speech Marker")
                .meta("readiness", true)
                .meta("archive", true)
                .meta("how", "h-g-i-g-o")
                .meta("editable", true)
                .meta("movable", true)
                .meta("removable", true)
                .meta("entry", "user")
                .meta("callsign", "Speech Marker")
                .groupPath("Cursor on Target", s.get("markerType"))
                .persist()
                .build());
        if (uid == null) {
            Log.w(TAG, "map not ready; speech marker not placed");
            return;
        }
        Log.d(TAG, "creating a new unit marker for: " + uid);

        Intent new_cot_intent = new Intent();
        new_cot_intent.setAction("com.atakmap.android.maps.COT_PLACED");
        new_cot_intent.putExtra("uid", uid);
        broadcastCreator.send(new_cot_intent);
    }
}
