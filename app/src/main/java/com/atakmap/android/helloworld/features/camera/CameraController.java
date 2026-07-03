package com.atakmap.android.helloworld.features.camera;

/**
 * The camera feature's Controller: the fly-through demo behavior, ATAK-free,
 * depending only on {@link CameraCreator}. The Pane controller forwards the
 * fly tap here.
 */
public class CameraController {

    private final CameraCreator cameraCreator;

    public CameraController(CameraCreator cameraCreator) {
        this.cameraCreator = cameraCreator;
    }

    /**
     * Fly through a list of synthetically generated points — they could just
     * as easily be points on a route. One point per second, off the UI thread.
     */
    public void flyThroughDemo() {
        new Thread(() -> {
            cameraCreator.zoomTo(.00001d, false);
            for (int i = 0; i < 20; ++i) {
                cameraCreator.panTo(42, -79 - (double) i / 100, false);
                try {
                    Thread.sleep(1000);
                } catch (Exception ignored) {
                }
            }
        }).start();
    }
}
