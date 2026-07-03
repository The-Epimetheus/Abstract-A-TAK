package com.atakmap.android.helloworld.features.capture;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;

import com.atakmap.android.helloworld.CameraActivity;
import com.atakmap.android.helloworld.abstraction.Disposable;

/**
 * The device-camera capture feature's Controller: launches the plugin's own
 * {@link CameraActivity} to take a photo and receives the {@link Bitmap} it
 * broadcasts back. Everything here is stock Android — the activity, the
 * broadcast, the {@code startActivity} — so there is no Creator behind this
 * one; the Controller exists for the uniform tap → Controller shape
 * (ADR-0005). Not to be confused with {@code features/camera}, which drives
 * the MAP camera.
 *
 * <p>The launch needs the HOST context (an Activity context that can start
 * activities and receive the photo broadcast), so the pane wiring passes
 * {@code shell.hostContext()} per call rather than this class holding it.
 */
public class CameraCaptureController implements Disposable {

    private static final String TAG = "CameraCaptureController";

    // Receives the photo Bitmap broadcast back by CameraActivity. The
    // listener is one-shot: it unregisters itself inside onReceive.
    private final CameraActivity.CameraDataListener cdl = new CameraActivity.CameraDataListener();
    private final CameraActivity.CameraDataReceiver cdr = new CameraActivity.CameraDataReceiver() {
        public void onCameraDataReceived(Bitmap b) {
            Log.d(TAG, "==========img received======>" + b);
            b.recycle();
        }
    };

    /**
     * The host context a registration is pending against, retained only so
     * {@link #dispose()} can unregister; cleared once used.
     */
    private Context registeredContext;

    /**
     * Register for the photo broadcast, then launch the plugin's
     * CameraActivity to take the picture.
     *
     * @param hostContext the HOST (ATAK activity) context — plugin contexts
     *            cannot start activities or host the broadcast registration.
     */
    public void launchCamera(Context hostContext) {
        cdl.register(hostContext, cdr);
        registeredContext = hostContext;
        // this makes use of an activity that cannot know anything
        // about ATAK.   This is the same problem as we have with
        // notifications.  They run outside of the current ATAK
        // classloader paradigm.
        Intent intent = new Intent();
        intent.setClassName("com.atakmap.android.helloworld.plugin",
                "com.atakmap.android.helloworld.CameraActivity");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        hostContext.startActivity(intent);
    }

    /**
     * Tear down a still-pending registration (tapped the button, photo never
     * came back). Legacy fix: the old receiver never unregistered this
     * listener on dispose, leaking a BroadcastReceiver in the host process
     * across plugin hot-reloads. Idempotent — the listener's own unregister
     * no-ops when it already fired.
     */
    @Override
    public void dispose() {
        Context c = registeredContext;
        registeredContext = null;
        if (c != null)
            cdl.unregister(c);
    }
}
