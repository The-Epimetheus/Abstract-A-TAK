package com.atakmap.android.helloworld.features.bump;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import com.atakmap.android.helloworld.abstraction.Disposable;
import com.atakmap.android.helloworld.features.prompt.PromptCreator;

/**
 * The bump-control feature's Controller: toggles accelerometer "tilt"
 * detection and narrates the detected tilt direction through the on-screen
 * prompt line. ATAK-free — {@code android.hardware} sensors are
 * boundary-legal, and the one ATAK touch (the TextContainer prompt) hides
 * behind {@link PromptCreator}.
 *
 * <p>The Controller — not the Humble shell — owns the
 * {@link SensorEventListener}, so the live registration is torn down by the
 * {@link Disposable} cascade when the plugin unloads (ATAK hot-reloads
 * plugins; a leaked sensor listener would survive into the next load).
 */
public class BumpController implements Disposable {

    private static final String TAG = "BumpController";

    private final Context pluginContext;
    private final PromptCreator promptCreator;

    /** Whether the tilt listener is currently registered. */
    private boolean listening;

    /**
     * The accelerometer listener, verbatim from the legacy demo: a tilt past
     * ~7 m/s² on an axis replaces the prompt text with the tilt direction.
     */
    private final SensorEventListener tiltListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(final SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                final float[] values = event.values;
                // Movement
                float x = values[0];
                float y = values[1];
                float z = values[2];

                float asr = (x * x + y * y + z * z)
                        / (SensorManager.GRAVITY_EARTH
                                * SensorManager.GRAVITY_EARTH);
                if (Math.abs(x) > 6 || Math.abs(y) > 6 || Math.abs(z) > 8)
                    Log.d(TAG, "gravity=" + SensorManager.GRAVITY_EARTH
                            + " x=" + x + " y=" + y + " z=" + z
                            + " asr=" + asr);
                if (y > 7) {
                    promptCreator.displayPrompt("Tilt Right");
                    Log.d(TAG, "tilt right");
                } else if (y < -7) {
                    promptCreator.displayPrompt("Tilt Left");
                    Log.d(TAG, "tilt left");
                } else if (x > 7) {
                    promptCreator.displayPrompt("Tilt Up");
                    Log.d(TAG, "tilt up");
                } else if (x < -7) {
                    promptCreator.displayPrompt("Tilt Down");
                    Log.d(TAG, "tilt down");
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                Log.d(TAG, "accuracy for the accelerometer: " + accuracy);
            }
        }
    };

    public BumpController(PromptCreator promptCreator,
            Context pluginContext) {
        this.promptCreator = promptCreator;
        this.pluginContext = pluginContext;
    }

    /**
     * Toggle tilt detection. Turning on: display the instruction prompt, then
     * register the accelerometer listener at {@code SENSOR_DELAY_NORMAL}.
     * Turning off: unregister, then close the prompt (legacy ordering
     * preserved on both sides).
     *
     * <p>{@code SensorManager} is a process-global system service, so the
     * plugin context resolves the same instance the legacy code reached
     * through its two different contexts (plugin context on toggle, host
     * context in dispose).
     *
     * @return true if tilt detection is now active — the pane wiring mirrors
     *         this into the button's selected state.
     */
    public synchronized boolean toggleTiltDetection() {
        SensorManager sensorManager = (SensorManager) pluginContext
                .getSystemService(Context.SENSOR_SERVICE);

        if (!listening) {
            promptCreator.displayPrompt(
                    "Tilt the phone to perform an action");

            sensorManager.registerListener(
                    tiltListener,
                    sensorManager.getDefaultSensor(
                            Sensor.TYPE_ACCELEROMETER),
                    SensorManager.SENSOR_DELAY_NORMAL);

        } else {
            sensorManager.unregisterListener(tiltListener);
            promptCreator.closePrompt();
        }
        listening = !listening;
        return listening;
    }

    /**
     * Unregister the tilt listener and close the prompt. Mirrors the legacy
     * {@code disposeImpl()} block, which ran unconditionally — both calls are
     * harmless no-ops when nothing is registered/showing — so this is
     * idempotent. Cascaded automatically by {@code PaneController.dispose()}.
     */
    @Override
    public synchronized void dispose() {
        SensorManager sensorManager = (SensorManager) pluginContext
                .getSystemService(Context.SENSOR_SERVICE);
        sensorManager.unregisterListener(tiltListener);
        promptCreator.closePrompt();
        listening = false;
    }
}
