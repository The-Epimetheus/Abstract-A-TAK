package com.atakmap.android.helloworld.compat;

import com.atakmap.android.layers.LayerDownloader;
import com.atakmap.android.maps.MapView;
import com.atakmap.android.maps.Shape;

/**
 * Compatibility-band impl for ATAK &gt;= 5.5, where {@link LayerDownloader} is
 * configured via a {@code RequestBuilder} and started with
 * {@code startDownload(RequestBuilder)}; the &lt;=5.4 per-setter API and
 * {@code startDownload()} were removed. The callback is registered on the
 * downloader with a key.
 *
 * <p>Lives in {@code src/bands/atak55plus} (added to the atak551..atak580 flavors);
 * identical FQN to the {@code src/bands/atakPre55} twin, so exactly one compiles per APK.
 *
 * <p>INTERNAL to the LayerDownloadCreator seam: called only from
 * {@code LayerDownloadCreatorImpl} (src/atakShared), never from {@code src/main}.
 */
public final class LayerDownloadHelper {

    private LayerDownloadHelper() {}

    /** LayerDownloader(MapView) was removed in 5.5; only LayerDownloader(Context) remains. */
    public static LayerDownloader create(MapView mapView) {
        return new LayerDownloader(mapView.getContext());
    }

    /**
     * selfCheck probe: exercises this band's config API (the RequestBuilder) on a
     * fresh, never-started downloader so a wrong band binding fails HERE at load.
     * Returns a description of the API family exercised.
     */
    public static String probeConfigApi(MapView mapView) {
        create(mapView); // LayerDownloader(Context) ctor — the other 5.5 break
        new LayerDownloader.RequestBuilder()
                .setTitle("helloworld.selfcheck.probe")
                .setResolution(0, 0)
                .setExpandDistance(0);
        return "LayerDownloader(Context) + RequestBuilder config (5.5+ band)";
    }

    public static void configureAndStart(LayerDownloader downloader, String title,
            String sourceURI, double minRes, double maxRes, Shape shape, double distM,
            String callbackKey, LayerDownloader.Callback callback) {
        LayerDownloader.RequestBuilder rb = new LayerDownloader.RequestBuilder()
                .setTitle(title)
                .setSourceURI(sourceURI)
                .setResolution(minRes, maxRes)
                .setShape(shape)
                .setExpandDistance(distM)
                .setCallback(callbackKey);
        downloader.setCallback(callbackKey, callback);
        downloader.startDownload(rb);
    }
}
