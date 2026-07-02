package com.atakmap.android.helloworld.compat;

import com.atakmap.android.layers.LayerDownloader;
import com.atakmap.android.maps.MapView;
import com.atakmap.android.maps.Shape;

/**
 * Compatibility-band impl for ATAK &lt;= 5.4, where {@link LayerDownloader} is
 * configured via individual setters and started with {@code startDownload()}.
 * ATAK 5.5 replaced this with a {@code RequestBuilder} + {@code startDownload(rb)}
 * (see the {@code src/bands/atak55plus} twin).
 *
 * <p>Lives in {@code src/bands/atakPre55} (added to the atak410..atak540 flavors).
 *
 * <p>INTERNAL to the LayerDownloadCreator seam: called only from
 * {@code LayerDownloadCreatorImpl} (src/atakShared), never from {@code src/main}.
 */
public final class LayerDownloadHelper {

    private LayerDownloadHelper() {}

    /** LayerDownloader(MapView) — the only ctor present in ATAK <= 5.1; also valid <=5.4. */
    public static LayerDownloader create(MapView mapView) {
        return new LayerDownloader(mapView);
    }

    /**
     * selfCheck probe: exercises this band's config API (the setters) on a fresh,
     * never-started downloader so a wrong band binding fails HERE at load. Returns
     * a description of the API family exercised.
     */
    public static String probeConfigApi(MapView mapView) {
        LayerDownloader probe = create(mapView);
        probe.setTitle("helloworld.selfcheck.probe");
        probe.setResolution(0, 0);
        probe.setExpandDistance(0);
        return "LayerDownloader(MapView) + per-setter config (pre-5.5 band)";
    }

    public static void configureAndStart(LayerDownloader downloader, String title,
            String sourceURI, double minRes, double maxRes, Shape shape, double distM,
            String callbackKey, LayerDownloader.Callback callback) {
        downloader.setTitle(title);
        downloader.setSourceURI(sourceURI);
        downloader.setResolution(minRes, maxRes);
        downloader.setShape(shape);
        downloader.setExpandDistance(distM);
        downloader.setCallback(callback);
        downloader.startDownload();
    }
}
