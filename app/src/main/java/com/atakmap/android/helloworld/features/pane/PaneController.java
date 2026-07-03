package com.atakmap.android.helloworld.features.pane;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.atakmap.android.helloworld.abstraction.Disposable;
import com.atakmap.android.helloworld.features.attachment.AttachmentController;
import com.atakmap.android.helloworld.features.bump.BumpController;
import com.atakmap.android.helloworld.features.camera.CameraController;
import com.atakmap.android.helloworld.features.capture.CameraCaptureController;
import com.atakmap.android.helloworld.features.contentprovider.ContentProviderController;
import com.atakmap.android.helloworld.features.coordentry.CoordinateEntryController;
import com.atakmap.android.helloworld.features.datapackage.DataPackageController;
import com.atakmap.android.helloworld.features.elevation.ElevationController;
import com.atakmap.android.helloworld.features.emergency.EmergencyController;
import com.atakmap.android.helloworld.features.inspect.InspectController;
import com.atakmap.android.helloworld.features.iss.IssController;
import com.atakmap.android.helloworld.features.layers.LayersController;
import com.atakmap.android.helloworld.features.location.LocationController;
import com.atakmap.android.helloworld.features.lrf.LrfController;
import com.atakmap.android.helloworld.features.mapcapture.MapCaptureController;
import com.atakmap.android.helloworld.features.marker.MarkerController;
import com.atakmap.android.helloworld.features.menu.MenuController;
import com.atakmap.android.helloworld.features.notification.NotificationController;
import com.atakmap.android.helloworld.features.overlayview.OverlayViewController;
import com.atakmap.android.helloworld.features.route.RouteController;
import com.atakmap.android.helloworld.features.screenshot.ScreenshotController;
import com.atakmap.android.helloworld.features.search.SearchWidgetController;
import com.atakmap.android.helloworld.features.sensor.SensorFovController;
import com.atakmap.android.helloworld.features.shape.ShapesController;
import com.atakmap.android.helloworld.features.speech.SpeechController;
import com.atakmap.android.helloworld.features.stream.StreamController;
import com.atakmap.android.helloworld.features.toolbar.ToolbarController;
import com.atakmap.android.helloworld.features.transfer.TransferController;
import com.atakmap.android.helloworld.features.video.VideoController;
import com.atakmap.android.helloworld.features.webview.WebViewController;
import com.atakmap.android.helloworld.plugin.R;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The Pane controller: owns the hello-world pane's view wiring. The Humble
 * shell inflates the pane and hands it here; this class binds the button
 * listeners and dispatches each tap to the right feature Controller (or to the
 * shell, through {@link PaneShell}, for operations on the live DropDown).
 * ATAK-free like any Controller; Android {@code View}s are boundary-legal.
 *
 * <p>Wiring migrates here cluster-by-cluster, top-to-bottom through the
 * legacy receiver — a listener still missing here has not migrated yet.
 */
public class PaneController {

    private final Context pluginContext;
    private final RouteController routeController;
    private final CameraController cameraController;
    private final SearchWidgetController searchWidgetController;
    private final OverlayViewController overlayViewController;
    private final MarkerController markerController;
    private final EmergencyController emergencyController;
    private final ShapesController shapesController;
    private final LocationController locationController;
    private final ElevationController elevationController;
    private final ContentProviderController contentProviderController;
    private final NotificationController notificationController;
    private final VideoController videoController;
    private final ToolbarController toolbarController;
    private final AttachmentController attachmentController;
    private final WebViewController webViewController;
    private final ScreenshotController screenshotController;
    private final LayersController layersController;
    private final CoordinateEntryController coordinateEntryController;
    private final TransferController transferController;
    private final DataPackageController dataPackageController;
    private final InspectController inspectController;
    private final CameraCaptureController cameraCaptureController;
    private final BumpController bumpController;
    private final SpeechController speechController;
    private final MenuController menuController;
    private final IssController issController;
    private final SensorFovController sensorFovController;
    private final LrfController lrfController;
    private final StreamController streamController;
    private final MapCaptureController mapCaptureController;

    /** Every feature Controller, for the {@link #dispose()} cascade. */
    private final List<Object> allControllers;

    public PaneController(Context pluginContext,
            RouteController routeController,
            CameraController cameraController,
            SearchWidgetController searchWidgetController,
            OverlayViewController overlayViewController,
            MarkerController markerController,
            EmergencyController emergencyController,
            ShapesController shapesController,
            LocationController locationController,
            ElevationController elevationController,
            ContentProviderController contentProviderController,
            NotificationController notificationController,
            VideoController videoController,
            ToolbarController toolbarController,
            AttachmentController attachmentController,
            WebViewController webViewController,
            ScreenshotController screenshotController,
            LayersController layersController,
            CoordinateEntryController coordinateEntryController,
            TransferController transferController,
            DataPackageController dataPackageController,
            InspectController inspectController,
            CameraCaptureController cameraCaptureController,
            BumpController bumpController,
            SpeechController speechController,
            MenuController menuController,
            IssController issController,
            SensorFovController sensorFovController,
            LrfController lrfController,
            StreamController streamController,
            MapCaptureController mapCaptureController) {
        this.pluginContext = pluginContext;
        this.routeController = routeController;
        this.cameraController = cameraController;
        this.searchWidgetController = searchWidgetController;
        this.overlayViewController = overlayViewController;
        this.markerController = markerController;
        this.emergencyController = emergencyController;
        this.shapesController = shapesController;
        this.locationController = locationController;
        this.elevationController = elevationController;
        this.contentProviderController = contentProviderController;
        this.notificationController = notificationController;
        this.videoController = videoController;
        this.toolbarController = toolbarController;
        this.attachmentController = attachmentController;
        this.webViewController = webViewController;
        this.screenshotController = screenshotController;
        this.layersController = layersController;
        this.coordinateEntryController = coordinateEntryController;
        this.transferController = transferController;
        this.dataPackageController = dataPackageController;
        this.inspectController = inspectController;
        this.cameraCaptureController = cameraCaptureController;
        this.bumpController = bumpController;
        this.speechController = speechController;
        this.menuController = menuController;
        this.issController = issController;
        this.sensorFovController = sensorFovController;
        this.lrfController = lrfController;
        this.streamController = streamController;
        this.mapCaptureController = mapCaptureController;
        this.allControllers = Arrays.asList(routeController, cameraController,
                searchWidgetController, overlayViewController,
                markerController, emergencyController, shapesController,
                locationController, elevationController,
                contentProviderController, notificationController,
                videoController, toolbarController, attachmentController,
                webViewController, screenshotController, layersController,
                coordinateEntryController, transferController,
                dataPackageController, inspectController,
                cameraCaptureController, bumpController, speechController,
                menuController, issController, sensorFovController,
                lrfController, streamController, mapCaptureController);
    }

    /**
     * Tear down every feature Controller that holds live registrations
     * (implements {@link Disposable}). The Humble shell forwards its
     * {@code disposeImpl()} here so Controllers can own their own lifecycle.
     */
    public void dispose() {
        for (Object controller : allControllers) {
            if (controller instanceof Disposable)
                ((Disposable) controller).dispose();
        }
    }

    /**
     * The shell forwards {@code onDropDownClose()} here. Legacy behavior,
     * verbatim: if the map-item inspector is still running when the pane
     * closes, turn it off and clear the button's toggled state (button state
     * is View state — boundary-legal in this Controller).
     */
    public void onPaneClose(View pane) {
        // make sure that if the Map Item inspector is running
        // turn off the map item inspector
        final Button itemInspect = pane.findViewById(R.id.itemInspect);
        boolean val = itemInspect.isSelected();
        if (val) {
            itemInspect.setSelected(false);
            inspectController.stopInspection();
        }
    }

    /** Bind every migrated listener onto the freshly inflated pane. */
    public void attach(View pane, PaneShell shell) {
        wireLongClickHints(pane, shell);
        wireResizeButtons(pane, shell);
        wirePaneSwitchers(pane, shell);
        wireRouteButtons(pane, shell);
        wireCameraButtons(pane);
        wireBroadcastDemos(pane);
        wireMarkerButtons(pane);
        wireEmergencyButtons(pane);
        wireShapeButtons(pane, shell);
        wireLocationButtons(pane);
        wireElevationButtons(pane);
        wireContentProviderButtons(pane);
        wireNotificationButtons(pane, shell);
        wireVideoButtons(pane);
        wireToolbarButtons(pane);
        wireAttachmentButtons(pane);
        wireWebViewButtons(pane, shell);
        wireScreenshotButtons(pane);
        wireLayerButtons(pane);
        wireCoordinateEntryButtons(pane);
        wireFileSendButtons(pane, shell);
        wireDataPackageButtons(pane, shell);
        wireInspectButtons(pane, shell);
        wireCameraCaptureButton(pane, shell);
        wireBumpButtons(pane);
        wireSpeechButtons(pane, shell);
        wireCustomMarkerAndMenuButtons(pane);
        wireIssButtons(pane);
        wireSensorFovButtons(pane);
        wireLrfButtons(pane);
        wireStreamButtons(pane, shell);
        wireMapCaptureAndSpinner(pane);
    }

    /* ------------------- long-press hints (every button) ------------------- */

    /**
     * Long-pressing any demo button toasts a one-line description of what it
     * does — the pane's built-in documentation.
     */
    private void wireLongClickHints(View pane, PaneShell shell) {
        final Map<Integer, String> hints = new LinkedHashMap<>();
        hints.put(R.id.smallerButton, str(R.string.smallerButton));
        hints.put(R.id.largerButton, str(R.string.largerButton));
        hints.put(R.id.showSearchIcon, str(R.string.showSeachIcon));
        hints.put(R.id.fly, str(R.string.fly));
        hints.put(R.id.specialWheelMarker, str(R.string.specialWheelMarker));
        hints.put(R.id.addAnAircraft, str(R.string.addAnAircraft));
        hints.put(R.id.svgMarker, str(R.string.svgMarker));
        hints.put(R.id.staleoutMarker, str(R.string.staleoutMarker));
        hints.put(R.id.addStream, str(R.string.addStream));
        hints.put(R.id.removeStream, str(R.string.removeStream));
        hints.put(R.id.itemInspect, str(R.string.itemInspect));
        hints.put(R.id.customType, str(R.string.customType));
        hints.put(R.id.customMenuDefault, str(R.string.customMenuDefault));
        hints.put(R.id.issLocation, str(R.string.issLocation));
        hints.put(R.id.sensorFOV, str(R.string.sensorFOV));
        hints.put(R.id.listRoutes, str(R.string.listRoutes));
        hints.put(R.id.addXRoute, str(R.string.addXRoute));
        hints.put(R.id.reXRoute, str(R.string.reXRoute));
        hints.put(R.id.dropRoute, str(R.string.dropRoute));
        hints.put(R.id.emergency, str(R.string.emergency));
        hints.put(R.id.no_emergency, str(R.string.no_emergency));
        hints.put(R.id.addRectangle, str(R.string.addRectangle));
        hints.put(R.id.drawShapes, str(R.string.drawShapes));
        hints.put(R.id.groupAdd,
                "Add a shape to a custom group called MyCustomGroup for rendering in the overlay manager");
        hints.put(R.id.associations,
                "Demonstrate the proper way to add two map items and an association");
        hints.put(R.id.rbcircle, str(R.string.rbcircle));
        hints.put(R.id.externalGps, str(R.string.externalGps));
        hints.put(R.id.surfaceAtCenter, str(R.string.surfaceAtCenter));
        hints.put(R.id.fakeContentProvider, str(R.string.fakeContentProvider));
        hints.put(R.id.pluginNotification, str(R.string.pluginNotification));
        hints.put(R.id.notificationSpammer, str(R.string.notificationSpammer));
        hints.put(R.id.notificationWithOptions,
                str(R.string.notificationWithOptions));
        hints.put(R.id.videoLauncher, str(R.string.videoLauncher));
        hints.put(R.id.addToolbarItem, str(R.string.addToolbarItem));
        hints.put(R.id.cameraLauncher, str(R.string.cameraLauncher));
        hints.put(R.id.imageAttach, str(R.string.imageAttach));
        hints.put(R.id.webView, str(R.string.webView));
        hints.put(R.id.addLayer, str(R.string.addLayer));
        hints.put(R.id.bumpControl, str(R.string.bumpControl));
        hints.put(R.id.speechToActivity, str(R.string.speechToActivity));
        hints.put(R.id.btnHookNavigationEvents, str(R.string.hookNavigation));
        hints.put(R.id.downloadMapLayer, str(R.string.download_map_layer_msg));
        hints.put(R.id.mapScreenshot, str(R.string.map_screenshot_desc));

        View.OnLongClickListener hinter = v -> {
            String hint = hints.get(v.getId());
            if (hint != null)
                toast(shell.hostContext(), hint);
            return true;
        };
        for (Integer id : hints.keySet()) {
            View button = pane.findViewById(id);
            if (button != null)
                button.setOnLongClickListener(hinter);
        }
    }

    /* --------------------- pane sizing / pane switching --------------------- */

    private void wireResizeButtons(View pane, PaneShell shell) {
        // Demonstrates programmatically changing the size of the drop down.
        pane.findViewById(R.id.smallerButton).setOnClickListener(
                v -> shell.resizePane(1d / 3d, 1d));
        pane.findViewById(R.id.largerButton).setOnClickListener(
                v -> shell.resizePane(1d, 0.5d));
    }

    private void wirePaneSwitchers(View pane, PaneShell shell) {
        pane.findViewById(R.id.recyclerViewBtn).setOnClickListener(
                v -> shell.showRecyclerPane());
        pane.findViewById(R.id.tabViewBtn).setOnClickListener(
                v -> shell.showTabPane());
        pane.findViewById(R.id.dropdownBtn).setOnClickListener(
                v -> shell.pushNavigationStackPane());
    }

    /* ------------------------------ route demo ------------------------------ */

    private void wireRouteButtons(View pane, PaneShell shell) {
        // Taps forward to RouteController with the map center as primitives —
        // the Controller holds the behavior (Humble-Object split).
        pane.findViewById(R.id.addXRoute).setOnClickListener(v -> {
            double[] c = shell.mapCenterLatLon();
            routeController.createDemoRoute(c[0], c[1]);
        });

        pane.findViewById(R.id.reXRoute).setOnClickListener(v -> {
            double[] c = shell.mapCenterLatLon();
            if (!routeController.extendDemoRoute(c[0], c[1]))
                toast(shell.hostContext(), "No Route added during this run");
        });

        pane.findViewById(R.id.dropRoute).setOnClickListener(v -> {
            routeController.createFlyingRoute();
            shell.hidePane();
        });

        // Programmatically list all routes on the map in a picker dialog.
        pane.findViewById(R.id.listRoutes).setOnClickListener(v -> {
            AlertDialog.Builder builderSingle = new AlertDialog.Builder(
                    shell.hostContext());
            builderSingle.setTitle("Select a Route");
            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                    pluginContext,
                    android.R.layout.select_dialog_singlechoice);
            for (String title : routeController.completeRouteTitles()) {
                arrayAdapter.add(title);
            }
            builderSingle.setNegativeButton("Cancel",
                    (dialog, which) -> dialog.dismiss());
            builderSingle.setAdapter(arrayAdapter, null);
            builderSingle.show();
        });

        // RouteController registers itself as a RouteNavPort with RouteCreator;
        // the impl adapts ATAK's two navigation listener interfaces onto it.
        final Button hookNav = pane.findViewById(R.id.btnHookNavigationEvents);
        hookNav.setText(routeController.isNavigationHooked()
                ? "UnHook into navigation events"
                : "Hook into navigation events");
        hookNav.setOnClickListener(v -> {
            if (routeController.toggleNavigationEvents()) {
                hookNav.setText("Unhook navigation events");
                toast(shell.hostContext(),
                        "Start navigation on a route to start getting your toasts for events");
            } else {
                hookNav.setText("Hook into navigation events");
                toast(shell.hostContext(),
                        "You should no longer get toasts for events");
            }
        });
    }

    /* --------------------------- camera / broadcasts ------------------------ */

    private void wireCameraButtons(View pane) {
        // Programmatically fly through a list of points.
        pane.findViewById(R.id.fly).setOnClickListener(
                v -> cameraController.flyThroughDemo());
    }

    private void wireBroadcastDemos(View pane) {
        // Custom map widget demo — the widget listens for the broadcast.
        pane.findViewById(R.id.showSearchIcon).setOnClickListener(
                v -> searchWidgetController.showSearchWidget());
        // Map-anchored overlay view demo.
        pane.findViewById(R.id.overlayViewBtn).setOnClickListener(
                v -> overlayViewController.toggleOverlayView());
    }

    /* --------------------------- markers / emergency ------------------------ */

    private void wireMarkerButtons(View pane) {
        pane.findViewById(R.id.specialWheelMarker).setOnClickListener(
                v -> markerController.placeUnitAtCenter());
        pane.findViewById(R.id.addAnAircraft).setOnClickListener(
                v -> markerController.placeAircraftAtCenter());
        pane.findViewById(R.id.svgMarker).setOnClickListener(
                v -> markerController.placeSvgMarkerAtCenter());
        pane.findViewById(R.id.staleoutMarker).setOnClickListener(
                v -> markerController.placeStaleoutAircraftAtCenter());
    }

    private void wireEmergencyButtons(View pane) {
        pane.findViewById(R.id.emergency).setOnClickListener(
                v -> emergencyController.startEmergency());
        pane.findViewById(R.id.no_emergency).setOnClickListener(
                v -> emergencyController.cancelEmergency());
    }

    /* ----------------------------- drawing shapes ---------------------------- */

    private void wireShapeButtons(View pane, PaneShell shell) {
        pane.findViewById(R.id.rbcircle).setOnClickListener(v -> {
            if (!shapesController.placeDetectMarkerWithAccuracyEllipse())
                toast(shell.hostContext(), "marker already placed");
        });
        pane.findViewById(R.id.addRectangle).setOnClickListener(
                v -> shapesController.addRectangleDemo());
        pane.findViewById(R.id.drawShapes).setOnClickListener(
                v -> shapesController.drawShapesShowcase());
        pane.findViewById(R.id.groupAdd).setOnClickListener(
                v -> shapesController.addCustomGroupWithShapeAndMarker());
        pane.findViewById(R.id.associations).setOnClickListener(
                v -> shapesController.addAssociationDemo());
    }

    /* ------------------- mock GPS / elevation / provider -------------------- */

    private void wireLocationButtons(View pane) {
        pane.findViewById(R.id.externalGps).setOnClickListener(
                v -> locationController.simulateGpsFix());
    }

    private void wireElevationButtons(View pane) {
        pane.findViewById(R.id.surfaceAtCenter).setOnClickListener(
                v -> elevationController.showElevationAtCenter());
    }

    private void wireContentProviderButtons(View pane) {
        pane.findViewById(R.id.fakeContentProvider).setOnClickListener(
                v -> contentProviderController.exerciseBirthdayProvider());
    }

    /* ------------------------------ notifications --------------------------- */

    private void wireNotificationButtons(View pane, PaneShell shell) {
        // The plugin's own foreground service — the real pattern.
        pane.findViewById(R.id.pluginNotification).setOnClickListener(
                v -> notificationController.startPluginNotificationService(
                        shell.hostContext()));
        // NW ROM crash reproducer — see the Controller's warning.
        pane.findViewById(R.id.notificationSpammer).setOnClickListener(
                v -> notificationController.spamStatusNotifications());
        // Action notification that re-enters the plugin by broadcast.
        pane.findViewById(R.id.notificationWithOptions).setOnClickListener(
                v -> notificationController.postFakePhoneCallNotification());
    }

    /* ------------------------------- video demo ------------------------------ */

    private void wireVideoButtons(View pane) {
        // Launch ATAK's video player on a demo rtsp stream; "test-layer" is
        // the VideoViewLayer the map component registered at load.
        pane.findViewById(R.id.videoLauncher).setOnClickListener(
                v -> videoController.launchDemoVideo());
    }

    /* ------------------------------- toolbar -------------------------------- */

    private void wireToolbarButtons(View pane) {
        // The Controller listens for the TowTruck item's click broadcast for
        // the life of the plugin (the legacy receiver registered at
        // construction — the same moment, since the shell attaches from its
        // constructor); the dispose() cascade tears it down.
        toolbarController.listenForFordToolClicks();

        // Toggle a "Ford TowTruck" item on ATAK's toolbar (overflow menu);
        // the button's selected state tracks whether it is currently added.
        pane.findViewById(R.id.addToolbarItem).setOnClickListener(v -> {
            if (v.isSelected())
                toolbarController.hideFordTowTruckItem();
            else
                toolbarController.showFordTowTruckItem();
            v.setSelected(!v.isSelected());
        });

        // Bump the badge count on the plugin's own action-bar icon
        // (HelloWorldTool listens for this broadcast).
        pane.findViewById(R.id.addCountToIcon).setOnClickListener(
                v -> toolbarController.bumpIconCount());
    }

    /* ---------------------------- image attachment --------------------------- */

    private void wireAttachmentButtons(View pane) {
        // Drop a marker at the map center and file a host-asset image under
        // its attachments folder (atak/attachments/<uid>/).
        pane.findViewById(R.id.imageAttach).setOnClickListener(
                v -> attachmentController.attachImageToNewMarker());
    }

    /* -------------------------------- web view ------------------------------- */

    private void wireWebViewButtons(View pane, PaneShell shell) {
        // Opens the separate webview drop-down (the legacy
        // WebViewDropDownReceiver listens for this broadcast). Retain first so
        // this pane survives underneath and is still there when the webview
        // pane closes — same ordering as the legacy button.
        pane.findViewById(R.id.webView).setOnClickListener(v -> {
            shell.retainPane();
            webViewController.showWebView();
        });
    }

    /* ----------------------------- map screenshot --------------------------- */

    private void wireScreenshotButtons(View pane) {
        // High-quality map capture via ATAK's tile-capture workflow. The
        // Controller toasts "started"; the asynchronous capture toasts the
        // finished output path itself when it completes.
        pane.findViewById(R.id.mapScreenshot).setOnClickListener(
                v -> screenshotController.captureMapScreenshot());
    }

    /* ------------------------------- map layers ------------------------------ */

    private void wireLayerButtons(View pane) {
        // Toggle-style buttons: the Android selected state tracks whether the
        // demo layer is on the map; the Controller reports whether the toggle
        // actually happened (staging the layer image can fail).
        final Button addLayer = pane.findViewById(R.id.addLayer);
        addLayer.setOnClickListener(v -> {
            if (layersController.toggleExampleLayer(addLayer.isSelected()))
                addLayer.setSelected(!addLayer.isSelected());
        });

        final Button addMultiLayer = pane.findViewById(R.id.addMultiLayer);
        addMultiLayer.setOnClickListener(v -> {
            if (layersController.toggleMultiLayers(addMultiLayer.isSelected()))
                addMultiLayer.setSelected(!addMultiLayer.isSelected());
        });

        // The heat map flips its selected state first, then acts on the NEW
        // state (legacy quirk, preserved).
        final Button addHeatMap = pane.findViewById(R.id.addHeatMap);
        addHeatMap.setOnClickListener(v -> {
            v.setSelected(!v.isSelected());
            layersController.toggleHeatMap(v.isSelected());
        });
    }

    /* ---------------------------- coordinate entry --------------------------- */

    private void wireCoordinateEntryButtons(View pane) {
        // ATAK's stock coordinate-entry dialog, pre-filled with the map
        // center; the committed point comes back through the Creator's
        // callback port (on the UI thread) and is toasted host-formatted.
        pane.findViewById(R.id.coordinateEntry).setOnClickListener(
                v -> coordinateEntryController.promptForCoordinate());
    }

    /* --------------------------- file import / send -------------------------- */

    private void wireFileSendButtons(View pane, PaneShell shell) {
        // Ask ATAK's import manager to open its own file browser; a picked
        // .hwi file lands in the plugin's registered importer.
        pane.findViewById(R.id.importFile).setOnClickListener(
                v -> transferController.requestUserImport(shell.hostContext()));
        // Stage the sample .hwi under ATAK's data root and offer it through
        // ATAK's send dialog.
        pane.findViewById(R.id.sendFile).setOnClickListener(
                v -> transferController.sendSampleFile(shell.hostContext()));
        // Prompt-driven data package around ATAK's own support.inf file.
        pane.findViewById(R.id.filePromptDataPackage).setOnClickListener(
                v -> transferController.exportSupportFileDataPackage());
    }

    /* ----------------------------- data packages ---------------------------- */

    private void wireDataPackageButtons(View pane, PaneShell shell) {
        // Build a package of the markers/shapes around self, then round-trip
        // it back through the import path (toasts if the import declines).
        pane.findViewById(R.id.generateDataPackage).setOnClickListener(
                v -> dataPackageController.generateItemsAroundMePackage(
                        shell.hostContext()));
        // Seed ATAK's Mission Package export UI with a file to package up.
        pane.findViewById(R.id.filePromptDataPackage).setOnClickListener(
                v -> dataPackageController.promptFileDataPackage(
                        shell.hostContext()));
    }

    /* --------------------------- map item inspector ------------------------- */

    private void wireInspectButtons(View pane, PaneShell shell) {
        // Toggle the map-item inspector tool: selected == inspector running.
        // The button's toggled state is View state (boundary-legal here); the
        // Controller clears it through the callback when the tool ends for
        // any reason (selection made, invalid selection, or cancelled).
        final Button itemInspect = pane.findViewById(R.id.itemInspect);
        itemInspect.setOnClickListener(v -> {
            boolean val = itemInspect.isSelected();
            if (val) {
                inspectController.stopInspection();
            } else {
                inspectController.startInspection(shell.hostContext(),
                        () -> itemInspect.setSelected(false));
            }
            itemInspect.setSelected(!val);
        });
    }

    /* ------------------------ device camera capture ------------------------- */

    private void wireCameraCaptureButton(View pane, PaneShell shell) {
        // Launch the plugin's own CameraActivity to take a photo; the
        // Controller registers for the Bitmap it broadcasts back. Both the
        // registration and startActivity need the HOST context. A
        // still-pending registration is torn down by the Controller's
        // dispose() (Disposable cascade).
        pane.findViewById(R.id.cameraLauncher).setOnClickListener(
                v -> cameraCaptureController.launchCamera(shell.hostContext()));
    }

    /* ----------------------------- bump control ----------------------------- */

    private void wireBumpButtons(View pane) {
        // Accelerometer tilt-detection toggle. The Controller owns the
        // SensorEventListener and the prompt lifecycle (Disposable — the
        // dispose() cascade unregisters on unload); the button's selected
        // state mirrors whether detection is active.
        final Button bumpControl = pane.findViewById(R.id.bumpControl);
        bumpControl.setOnClickListener(
                v -> bumpControl.setSelected(
                        bumpController.toggleTiltDetection()));
    }

    /* ------------------------------ speech demo ------------------------------ */

    private void wireSpeechButtons(View pane, PaneShell shell) {
        // Launches the plugin's speech-to-intent activity; the result comes
        // back to SpeechController's one-shot broadcast listener, whose
        // switch dispatches the recognized action.
        pane.findViewById(R.id.speechToActivity).setOnClickListener(
                v -> speechController.launchSpeechToActivity(
                        shell.hostContext()));
    }

    /* -------------------- custom marker / custom menus ---------------------- */

    private void wireCustomMarkerAndMenuButtons(View pane) {
        // A marker with a fully custom CoT type; its label turns red 10s later.
        pane.findViewById(R.id.customType).setOnClickListener(
                v -> markerController.placeCustomTypeMarker());

        // Override the menu of EVERY friendly (a-f) marker with the hostile
        // (a-h) one; the button's selected state tracks the toggle.
        pane.findViewById(R.id.customMenuDefault).setOnClickListener(v -> {
            if (v.isSelected())
                menuController.restoreFriendlyMenu();
            else
                menuController.overrideFriendlyMenuWithHostile();
            v.setSelected(!v.isSelected());
        });

        // The widget API behind the custom menus moved to gov.tak.* in 5.5;
        // MenuCreator hides the banded factory and the registration entirely.
        pane.findViewById(R.id.customMenuFactory).setOnClickListener(v -> {
            if (v.isSelected())
                menuController.disableCustomRadialMenus();
            else
                menuController.enableCustomRadialMenus();
            v.setSelected(!v.isSelected());
        });
    }

    /* ------------------------------ ISS tracker ----------------------------- */

    private void wireIssButtons(View pane) {
        // Toggle the 3-second ISS position poll; the Controller owns the
        // Timer and the plot/move behavior (Disposable — the dispose cascade
        // reaps the Timer on unload).
        final Button issLocation = pane.findViewById(R.id.issLocation);
        issLocation.setSelected(issController.isTracking());
        issLocation.setOnClickListener(v -> issLocation
                .setSelected(issController.toggleIssTracking()));

        // ATAK 4.1 disables the cleartext block
        // The current ISS plotting site uses cleartext http connection and offers no https ability.
        // Since this is not allowed on Android 9 or higher, hide the capability until the web site
        // offers https
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
        //     issLocation.setVisibility(View.GONE);
    }

    /* ------------------------------ sensor FOV ------------------------------ */

    private void wireSensorFovButtons(View pane) {
        // Camera marker + field-of-view cone at the map center; re-tapping
        // recenters the camera and randomizes the cone (see the Controller).
        pane.findViewById(R.id.sensorFOV).setOnClickListener(
                v -> sensorFovController.createOrModifySensorFov());
    }

    /* -------------------------- laser range finder --------------------------- */

    private void wireLrfButtons(View pane) {
        // Toggle the LRF point tool: arm it (ATAK prompts "Fire Laser Range
        // Finder"), or press again to end the active tool.
        final Button lrfTool = pane.findViewById(R.id.lrfTool);
        lrfTool.setOnClickListener(v -> {
            final boolean selected = lrfTool.isSelected();
            if (selected)
                lrfController.endTool();
            else
                lrfController.startTool();
            lrfTool.setSelected(!selected);
        });
        // A reading self-ends the tool; clear the button's toggle to match.
        lrfController.setOnToolFinished(() -> lrfTool.setSelected(false));

        // Simulate an LRF hardware reading arriving on the local input.
        pane.findViewById(R.id.lrfFire).setOnClickListener(
                v -> lrfController.fireSimulatedReading());
    }

    /* ---------------------------- TAK server streams ------------------------- */

    private void wireStreamButtons(View pane, PaneShell shell) {
        // Import any server-connection mission packages staged in
        // <external-storage>/serverconnections; match/import runs through
        // ImportCreator (the v1 import path was removed in ATAK 5.8 —
        // no-op stub there).
        pane.findViewById(R.id.addStream).setOnClickListener(
                v -> streamController.importServerConnections(
                        shell.hostContext()));
        // Remove every configured TAK server stream ("**" wildcard) —
        // destructive on purpose; that is the demo.
        pane.findViewById(R.id.removeStream).setOnClickListener(
                v -> streamController.removeAllStreams());
    }

    /* ---------------- offscreen map render / spinner example ---------------- */

    private void wireMapCaptureAndSpinner(View pane) {
        // Offscreen map render (the legacy "blind cast"): a second GL surface
        // inside the pane mirroring the live map. Attaching builds the EGL/GL
        // pipeline eagerly at pane-inflation time — exactly when the legacy
        // receiver constructed it; the button only toggles the mirror loop.
        final LinearLayout secondRender = pane.findViewById(R.id.secondRender);
        final Button getImage = pane.findViewById(R.id.getImage);
        mapCaptureController.attachRenderTarget(secondRender);
        getImage.setOnClickListener(v -> {
            if (getImage.isSelected()) {
                mapCaptureController.setCapturing(false);
                secondRender.setVisibility(View.GONE);
            } else {
                mapCaptureController.setCapturing(true);
                secondRender.setVisibility(View.VISIBLE);
            }
            getImage.setSelected(!getImage.isSelected());
        });

        // Dark themed spinners need some text color correction. Pure pane
        // styling — no feature behavior, no ATAK call — so it lives in the
        // pane wiring itself. The legacy listener base class was ATAK's
        // SimpleItemSelectedListener, which is just this Android interface
        // with an empty onNothingSelected; inlining it keeps src/main
        // ATAK-free with identical behavior. The view itself MUST be a
        // PluginSpinner in the layout XML (see hello_world_layout.xml).
        final Spinner spinner = pane.findViewById(R.id.spinner1);
        spinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent,
                            View view,
                            int position, long id) {
                        if (view instanceof TextView)
                            ((TextView) view).setTextColor(Color.WHITE);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        // SimpleItemSelectedListener's no-op, inlined.
                    }
                });
        spinner.setSelection(0);
    }

    /* -------------------------------- helpers ------------------------------- */

    private String str(int resId) {
        return pluginContext.getString(resId);
    }

    private void toast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }
}
