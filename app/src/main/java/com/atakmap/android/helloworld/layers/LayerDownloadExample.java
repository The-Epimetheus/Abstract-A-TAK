
package com.atakmap.android.helloworld.layers;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.atakmap.android.helloworld.features.layerdownload.LayerDownloadCreator;
import com.atakmap.android.helloworld.features.layerdownload.LayerDownloadHandle;
import com.atakmap.android.helloworld.features.layerdownload.LayerDownloadPhase;
import com.atakmap.android.helloworld.features.layerdownload.LayerDownloadPort;
import com.atakmap.android.helloworld.features.layerdownload.LayerDownloadStatus;
import com.atakmap.android.helloworld.plugin.R;
import com.atakmap.android.ipc.AtakBroadcast;
import com.atakmap.android.ipc.AtakBroadcast.DocumentedIntentFilter;
import com.atakmap.android.layers.RegionShapeTool;
import com.atakmap.android.maps.MapView;
import com.atakmap.android.toolbar.ToolManagerBroadcastReceiver;

/**
 * Demonstrates workflow for selecting and downloading map tiles
 * <p>
 * 1) Start the region selection tool
 * 2) Once the user selects a region, the currently loaded layer will begin downloading
 * 3) The user is shown download progress in a dialog until completion
 * <p>
 * All LayerDownloader interaction goes through {@link LayerDownloadCreator}
 * (its ctor + config API diverged at 5.5 — banded behind that seam); progress
 * arrives through {@link LayerDownloadPort} as Plugin DTOs, and the live
 * download is held as a {@link LayerDownloadHandle}, never the ATAK object.
 */
public class LayerDownloadExample extends BroadcastReceiver
        implements LayerDownloadPort, DialogInterface.OnCancelListener {

    // Intent action fired when the region selection tool is finished
    private static final String TOOL_FINISH = "com.atakmap.android.helloworld.layers.LayerDownload_TOOL_FINISH";

    private final MapView _mapView;
    private final Context _context, _plugin;
    private final LayerDownloadCreator _creator;
    private final ProgressDialog _progDialog;
    private LayerDownloadHandle _download;

    public LayerDownloadExample(MapView mapView, Context plugin,
            LayerDownloadCreator creator) {
        _mapView = mapView;
        _context = mapView.getContext();
        _plugin = plugin;
        _creator = creator;

        // Register intent callback for the selection tool
        AtakBroadcast.getInstance().registerReceiver(this,
                new DocumentedIntentFilter(TOOL_FINISH));

        // Download progress dialog
        _progDialog = new ProgressDialog(_context);
        _progDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        _progDialog.setCanceledOnTouchOutside(false);
        _progDialog.setOnCancelListener(this);
        _progDialog
                .setMessage(_plugin.getString(R.string.job_status_connecting));
        _progDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
                _plugin.getString(R.string.cancel_btn),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int w) {
                        // Cancel button
                        _progDialog.cancel();
                    }
                });
    }

    public void dispose() {
        _progDialog.dismiss();
        AtakBroadcast.getInstance().unregisterReceiver(this);
        if (_download != null) {
            _creator.dispose(_download);
            _download = null;
        }
    }

    /**
     * Start the example workflow
     */
    public void start() {

        // Callback intent fired when the tool finishes
        Bundle b = new Bundle();
        b.putParcelable("callback", new Intent(TOOL_FINISH));

        // Start the region select tool
        // The user will be prompted with 4 options for selecting a shape on
        // the map. The shape will be used for determining which tiles to
        // download from the online map layer.
        ToolManagerBroadcastReceiver.getInstance().startTool(
                RegionShapeTool.TOOL_ID, b);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null)
            return;

        // Region selection tool is finished
        if (action.equals(TOOL_FINISH)) {

            // The UID of the shape the user selected/created
            String uid = intent.getStringExtra("uid");

            // Route expansion distance in meters
            // When selecting a route from the map, the user is prompted to
            // set how far outside the route to download map tiles.
            double distM = intent.getDoubleExtra("expandDistance", 0);

            // Enter download workflow: the Creator resolves the shape by UID,
            // finds the loaded mobile imagery, and starts the tile download.
            _download = _creator.startDownload("HelloWorld", uid, distM, this);
            if (_download != null) {
                _progDialog.setTitle(
                        _plugin.getString(R.string.map_layer_download));
                _progDialog.show();
            }
        }
    }

    /* ------------ LayerDownloadPort: progress DTOs from the Creator ------------ */

    @Override
    public void onUnavailable(String reason) {
        Toast.makeText(_context,
                _plugin.getString(R.string.no_mobile_imagery_found),
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onStatus(LayerDownloadStatus status) {
        // Show the current tile download progress
        _progDialog.setProgress(status.tilesDownloaded);
        _progDialog.setSecondaryProgress(status.levelTotalTiles);
        _progDialog.setMax(status.totalTiles);

        // Show status including the number of tiles downloaded, the number of
        // levels downloaded, and the estimated download time remaining
        _progDialog.setMessage(
                _plugin.getString(R.string.job_status_downloading) + "\n"
                        + _plugin.getString(R.string.download_tiles_progress,
                                status.tileStatus)
                        + ", "
                        + _plugin.getString(
                                R.string.download_layers_progress,
                                status.layerStatus)
                        + "\n"
                        + _plugin.getString(R.string.download_time_left,
                                formatTimeRemaining(status.timeLeftMillis)));
    }

    @Override
    public void onPhase(LayerDownloadPhase phase) {
        int msg;
        switch (phase) {
            case CONNECTING:
                msg = R.string.job_status_connecting;
                break;
            case DOWNLOADING:
                msg = R.string.job_status_downloading;
                break;
            case COMPLETE:
                Toast.makeText(_context, _plugin.getString(
                        R.string.map_layer_download_complete),
                        Toast.LENGTH_LONG).show();
                _progDialog.dismiss();
                return;
            case ERROR:
                Toast.makeText(_context, _plugin.getString(
                        R.string.map_layer_download_error),
                        Toast.LENGTH_LONG).show();
                _progDialog.dismiss();
                return;
            default:
            case CANCELLED:
                return;
        }
        _progDialog.setMessage(_plugin.getString(msg));
    }

    @Override
    public void onMaxProgress(int max) {
        _progDialog.setMax(max);
    }

    @Override
    public void onLevelProgress(int levelMax) {
        _progDialog.setSecondaryProgress(levelMax);
    }

    /**
     * Progress dialog has been canceled out
     * @param dialog Dialog
     */
    @Override
    public void onCancel(DialogInterface dialog) {
        // Stop the tile download
        if (_download != null)
            _creator.stopDownload(_download);
    }

    /** "1h 2m 3s"-style rendering of an estimated-time-remaining in millis. */
    private static String formatTimeRemaining(long millis) {
        long s = Math.max(0, millis / 1000);
        long h = s / 3600, m = (s % 3600) / 60;
        if (h > 0)
            return h + "h " + m + "m " + (s % 60) + "s";
        if (m > 0)
            return m + "m " + (s % 60) + "s";
        return (s % 60) + "s";
    }
}
