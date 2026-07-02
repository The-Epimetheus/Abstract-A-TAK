package com.atakmap.android.helloworld.features.layerdownload;

import android.content.Intent;

import com.atakmap.android.helloworld.abstraction.SelfCheckResult;
import com.atakmap.android.helloworld.compat.LayerDownloadHelper;
import com.atakmap.android.layers.LayerDownloader;
import com.atakmap.android.layers.RasterUtils;
import com.atakmap.android.layers.wms.DownloadJob;
import com.atakmap.android.maps.MapItem;
import com.atakmap.android.maps.MapView;
import com.atakmap.android.maps.Shape;
import com.atakmap.map.layer.raster.DatasetDescriptor;
import com.atakmap.map.layer.raster.ImageDatasetDescriptor;
import com.atakmap.map.layer.raster.osm.OSMUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The only place {@code LayerDownloader} and the imagery query are touched. The
 * 5.5 breaks (downloader ctor + config API) stay in the banded internal
 * {@link LayerDownloadHelper} (src/bands/atakPre55 / src/bands/atak55plus); the imagery
 * query uses {@code RasterUtils.queryDatasets}, the version-stable form of an
 * API that already broke once at 5.1. This impl keeps the
 * {@code LayerDownloadHandle -> LayerDownloader} registry — callers hold
 * handles, never the ATAK object — and adapts {@code LayerDownloader.Callback}
 * events into {@link LayerDownloadPort} DTO calls.
 */
public final class LayerDownloadCreatorImpl implements LayerDownloadCreator {

    private static final String CALLBACK_KEY = "helloWorldLayerDownload";

    private final Map<LayerDownloadHandle, LayerDownloader> registry =
            new HashMap<>();

    @Override
    public String id() {
        return "LayerDownloadCreator";
    }

    @Override
    public LayerDownloadHandle startDownload(String title, String shapeUid,
            double expandDistanceMeters, LayerDownloadPort port) {
        MapView mv = MapView.getMapView();
        if (mv == null) {
            port.onUnavailable("MapView not ready");
            return null;
        }

        MapItem item = mv.getMapItem(shapeUid);
        if (!(item instanceof Shape)) {
            port.onUnavailable("selected item is not a shape");
            return null;
        }
        Shape shape = (Shape) item;

        // Find the first available mobile imagery configuration for the region.
        // getCurrentImagery(MapView,GeoBounds) was removed in ATAK 5.1; queryDatasets
        // (GeoBounds,boolean) is present in every version (byte-confirmed).
        List<ImageDatasetDescriptor> datasets = new ArrayList<>();
        for (DatasetDescriptor d : RasterUtils.queryDatasets(
                shape.getBounds(null), false)) {
            if (d instanceof ImageDatasetDescriptor)
                datasets.add((ImageDatasetDescriptor) d);
        }
        ImageDatasetDescriptor mobac = null;
        for (ImageDatasetDescriptor d : datasets) {
            if (d.getProvider().equals("mobac")) {
                mobac = d;
                break;
            }
        }
        if (mobac == null) {
            port.onUnavailable("no mobile imagery loaded for this region");
            return null;
        }

        String sourceURI = mobac.getUri();

        // Request one tile level above and below the current map resolution,
        // clamped to what the imagery source provides.
        double minAvailableRes = mobac.getMinResolution(null);
        int numLevels = Integer.parseInt(DatasetDescriptor.getExtraData(mobac,
                "_levelCount", "0"));
        int levelOffset = OSMUtils.mapnikTileLevel(minAvailableRes);
        int mapLevel = OSMUtils.mapnikTileLevel(mv.getMapResolution())
                - levelOffset;
        int maxLevel = Math.min(mapLevel + 1, numLevels - 1);
        int minLevel = Math.max(mapLevel - 1, 0);
        double minRes = OSMUtils.mapnikTileResolution(minLevel);
        double maxRes = OSMUtils.mapnikTileResolution(maxLevel);

        // The 5.5-banded part: construct + configure + start the downloader.
        LayerDownloader downloader = LayerDownloadHelper.create(mv);
        LayerDownloadHelper.configureAndStart(downloader, title, sourceURI,
                minRes, maxRes, shape, expandDistanceMeters, CALLBACK_KEY,
                adapt(port));

        // The region-select tool leaves a temporary shape on the map — clean it.
        if (shape.hasMetaValue("layerDownload"))
            shape.removeFromGroup();

        LayerDownloadHandle handle = new LayerDownloadHandle();
        registry.put(handle, downloader);
        return handle;
    }

    @Override
    public void stopDownload(LayerDownloadHandle handle) {
        LayerDownloader downloader = registry.get(handle);
        if (downloader != null)
            downloader.stopDownload();
    }

    @Override
    public void dispose(LayerDownloadHandle handle) {
        LayerDownloader downloader = registry.remove(handle);
        if (downloader != null)
            downloader.stopDownload();
    }

    /** Adapt ATAK's callback to the plugin-owned port (event -> DTO -> port). */
    private LayerDownloader.Callback adapt(final LayerDownloadPort port) {
        return new LayerDownloader.Callback() {
            @Override
            public void onDownloadStatus(LayerDownloader.DownloadStatus s) {
                port.onStatus(new LayerDownloadStatus(s.title, s.tileStatus,
                        s.layerStatus, s.tilesDownloaded, s.totalTiles,
                        s.levelTotalTiles, s.timeLeft));
            }

            @Override
            public void onJobStatus(LayerDownloader.JobStatus s) {
                port.onPhase(mapPhase(s.code));
            }

            @Override
            public void onMaxProgressUpdate(String title, int progress) {
                port.onMaxProgress(progress);
            }

            @Override
            public void onLevelProgressUpdate(String title, int progress) {
                port.onLevelProgress(progress);
            }

            // Added to LayerDownloader.Callback in ATAK 5.1. Declared WITHOUT
            // @Override so this shared source also compiles against 4.10/5.0
            // (where it is simply an extra method, not an override). No-op:
            // HelloWorld drives region selection through its own tool intent.
            public void onRegionSelectFinished(Intent intent) {
            }
        };
    }

    private static LayerDownloadPhase mapPhase(int code) {
        switch (code) {
            case DownloadJob.CONNECTING:
                return LayerDownloadPhase.CONNECTING;
            case DownloadJob.DOWNLOADING:
                return LayerDownloadPhase.DOWNLOADING;
            case DownloadJob.COMPLETE:
                return LayerDownloadPhase.COMPLETE;
            case DownloadJob.ERROR:
                return LayerDownloadPhase.ERROR;
            case DownloadJob.CANCELLED:
            default:
                return LayerDownloadPhase.CANCELLED;
        }
    }

    /**
     * Starting a real download is an external network effect, so this degrades:
     * it exercises the BANDED construction + configuration API on a downloader
     * that is never started. A wrong band binding still fails here at load.
     */
    @Override
    public SelfCheckResult selfCheck() {
        try {
            MapView mv = MapView.getMapView();
            if (mv == null)
                return SelfCheckResult.skipped(id(), "MapView not ready");
            String api = LayerDownloadHelper.probeConfigApi(mv);
            return SelfCheckResult.partial(id(),
                    api + " exercised; download not started (network effect)");
        } catch (Throwable t) {
            return SelfCheckResult.failed(id(), "banded downloader path threw", t);
        }
    }
}
