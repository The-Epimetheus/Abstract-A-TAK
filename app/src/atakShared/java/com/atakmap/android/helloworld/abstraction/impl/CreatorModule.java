package com.atakmap.android.helloworld.abstraction.impl;

import android.content.Context;

import com.atakmap.android.helloworld.abstraction.Creator;
import com.atakmap.android.helloworld.abstraction.ShellProbe;
import com.atakmap.android.helloworld.abstraction.SystemsCheck;
import com.atakmap.android.helloworld.features.attachment.AttachmentController;
import com.atakmap.android.helloworld.features.attachment.AttachmentCreator;
import com.atakmap.android.helloworld.features.attachment.AttachmentCreatorImpl;
import com.atakmap.android.helloworld.features.broadcast.BroadcastCreator;
import com.atakmap.android.helloworld.features.broadcast.BroadcastCreatorImpl;
import com.atakmap.android.helloworld.features.bump.BumpController;
import com.atakmap.android.helloworld.features.camera.CameraController;
import com.atakmap.android.helloworld.features.camera.CameraCreator;
import com.atakmap.android.helloworld.features.camera.CameraCreatorImpl;
import com.atakmap.android.helloworld.features.capture.CameraCaptureController;
import com.atakmap.android.helloworld.features.contentprovider.ContentProviderController;
import com.atakmap.android.helloworld.features.coordentry.CoordinateEntryController;
import com.atakmap.android.helloworld.features.coordentry.CoordinateEntryCreator;
import com.atakmap.android.helloworld.features.coordentry.CoordinateEntryCreatorImpl;
import com.atakmap.android.helloworld.features.cot.CotCreatorImpl;
import com.atakmap.android.helloworld.features.datapackage.DataPackageController;
import com.atakmap.android.helloworld.features.datapackage.DataPackageCreator;
import com.atakmap.android.helloworld.features.datapackage.DataPackageCreatorImpl;
import com.atakmap.android.helloworld.features.elevation.ElevationController;
import com.atakmap.android.helloworld.features.elevation.ElevationCreator;
import com.atakmap.android.helloworld.features.elevation.ElevationCreatorImpl;
import com.atakmap.android.helloworld.features.emergency.EmergencyController;
import com.atakmap.android.helloworld.features.emergency.EmergencyCreator;
import com.atakmap.android.helloworld.features.emergency.EmergencyCreatorImpl;
import com.atakmap.android.helloworld.features.importer.ImportCreator;
import com.atakmap.android.helloworld.features.importer.ImportCreatorImpl;
import com.atakmap.android.helloworld.features.inspect.InspectController;
import com.atakmap.android.helloworld.features.inspect.InspectCreator;
import com.atakmap.android.helloworld.features.inspect.InspectCreatorImpl;
import com.atakmap.android.helloworld.features.iss.IssController;
import com.atakmap.android.helloworld.features.layerdownload.LayerDownloadCreator;
import com.atakmap.android.helloworld.features.layerdownload.LayerDownloadCreatorImpl;
import com.atakmap.android.helloworld.features.layers.LayerCreator;
import com.atakmap.android.helloworld.features.layers.LayerCreatorImpl;
import com.atakmap.android.helloworld.features.layers.LayersController;
import com.atakmap.android.helloworld.features.location.LocationController;
import com.atakmap.android.helloworld.features.location.LocationCreator;
import com.atakmap.android.helloworld.features.location.LocationCreatorImpl;
import com.atakmap.android.helloworld.features.lrf.LrfController;
import com.atakmap.android.helloworld.features.lrf.LrfCreator;
import com.atakmap.android.helloworld.features.lrf.LrfCreatorImpl;
import com.atakmap.android.helloworld.features.mapcapture.MapCaptureController;
import com.atakmap.android.helloworld.features.mapcapture.MapCaptureCreator;
import com.atakmap.android.helloworld.features.mapcapture.MapCaptureCreatorImpl;
import com.atakmap.android.helloworld.features.marker.MarkerController;
import com.atakmap.android.helloworld.features.marker.MarkerCreator;
import com.atakmap.android.helloworld.features.marker.MarkerCreatorImpl;
import com.atakmap.android.helloworld.features.menu.MenuController;
import com.atakmap.android.helloworld.features.menu.MenuCreator;
import com.atakmap.android.helloworld.features.menu.MenuCreatorImpl;
import com.atakmap.android.helloworld.features.notification.NotificationController;
import com.atakmap.android.helloworld.features.notification.NotificationCreator;
import com.atakmap.android.helloworld.features.notification.NotificationCreatorImpl;
import com.atakmap.android.helloworld.features.overlayview.OverlayViewController;
import com.atakmap.android.helloworld.features.pane.PaneController;
import com.atakmap.android.helloworld.features.prompt.PromptCreator;
import com.atakmap.android.helloworld.features.prompt.PromptCreatorImpl;
import com.atakmap.android.helloworld.features.radio.RadioCreator;
import com.atakmap.android.helloworld.features.radio.RadioCreatorImpl;
import com.atakmap.android.helloworld.features.route.RouteController;
import com.atakmap.android.helloworld.features.route.RouteCreator;
import com.atakmap.android.helloworld.features.route.RouteCreatorImpl;
import com.atakmap.android.helloworld.features.screenshot.ScreenshotController;
import com.atakmap.android.helloworld.features.screenshot.ScreenshotCreator;
import com.atakmap.android.helloworld.features.screenshot.ScreenshotCreatorImpl;
import com.atakmap.android.helloworld.features.search.SearchWidgetController;
import com.atakmap.android.helloworld.features.sensor.SensorFovController;
import com.atakmap.android.helloworld.features.sensor.SensorFovCreator;
import com.atakmap.android.helloworld.features.sensor.SensorFovCreatorImpl;
import com.atakmap.android.helloworld.features.shape.ShapeCreator;
import com.atakmap.android.helloworld.features.shape.ShapeCreatorImpl;
import com.atakmap.android.helloworld.features.shape.ShapesController;
import com.atakmap.android.helloworld.features.speech.SpeechController;
import com.atakmap.android.helloworld.features.speech.SpeechCreator;
import com.atakmap.android.helloworld.features.speech.SpeechCreatorImpl;
import com.atakmap.android.helloworld.features.stream.StreamController;
import com.atakmap.android.helloworld.features.stream.StreamCreator;
import com.atakmap.android.helloworld.features.stream.StreamCreatorImpl;
import com.atakmap.android.helloworld.features.toolbar.ToolbarController;
import com.atakmap.android.helloworld.features.toolbar.ToolbarCreator;
import com.atakmap.android.helloworld.features.toolbar.ToolbarCreatorImpl;
import com.atakmap.android.helloworld.features.transfer.TransferController;
import com.atakmap.android.helloworld.features.transfer.TransferCreator;
import com.atakmap.android.helloworld.features.transfer.TransferCreatorImpl;
import com.atakmap.android.helloworld.features.video.VideoController;
import com.atakmap.android.helloworld.features.video.VideoCreator;
import com.atakmap.android.helloworld.features.video.VideoCreatorImpl;
import com.atakmap.android.helloworld.features.webview.WebViewController;
import com.atakmap.android.helloworld.navstack.NavigationStackShellProbe;

import java.util.Collections;
import java.util.Set;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ElementsIntoSet;
import dagger.multibindings.IntoSet;

/**
 * The ONE registration point for Creator implementations (ADR-0003). Each
 * compiled-in Creator is contributed into a {@code Set<Creator>} via
 * {@code @IntoSet} multibindings; the systems check walks that set. Adding a
 * Creator means one typed {@code @Provides} + one {@code @IntoSet} delegation
 * here — nothing else.
 *
 * <p>Typed bindings are {@code @Singleton} so the instance the systems check
 * exercises is the same one handed to consumers (Creators that hold state —
 * a registered importer, a menu factory, a downloader registry — need that).
 *
 * <p>Constructed with the plugin {@link Context} at the composition root, which
 * a few impls need (menu factory inflation, import cache dir). Lives in
 * {@code src/atakShared} because it names the concrete impls (which touch
 * ATAK). {@code src/main} stays ATAK-free. Don't want Dagger in your own
 * plugin? The hand-wired equivalent of this module is a dozen lines — see
 * MIGRATION.md, "Without Dagger".
 */
@Module
public class CreatorModule {

    private final Context pluginContext;

    public CreatorModule(Context pluginContext) {
        this.pluginContext = pluginContext;
    }

    // ---- CotCreator: exercised by the systems check only (no typed consumer yet)

    @Provides
    @IntoSet
    Creator provideCotCreator() {
        return new CotCreatorImpl();
    }

    // ---- Creators src/main consumes directly: typed binding + set delegation

    @Provides
    @Singleton
    RadioCreator provideRadioCreator() {
        return new RadioCreatorImpl();
    }

    @Provides
    @IntoSet
    Creator radioCreatorIntoSet(RadioCreator c) {
        return c;
    }

    @Provides
    @Singleton
    LocationCreator provideLocationCreator() {
        return new LocationCreatorImpl();
    }

    @Provides
    @IntoSet
    Creator locationCreatorIntoSet(LocationCreator c) {
        return c;
    }

    @Provides
    @Singleton
    VideoCreator provideVideoCreator() {
        return new VideoCreatorImpl();
    }

    @Provides
    @IntoSet
    Creator videoCreatorIntoSet(VideoCreator c) {
        return c;
    }

    @Provides
    @Singleton
    ImportCreator provideImportCreator() {
        return new ImportCreatorImpl(pluginContext);
    }

    @Provides
    @IntoSet
    Creator importCreatorIntoSet(ImportCreator c) {
        return c;
    }

    @Provides
    @Singleton
    MenuCreator provideMenuCreator() {
        return new MenuCreatorImpl(pluginContext);
    }

    @Provides
    @IntoSet
    Creator menuCreatorIntoSet(MenuCreator c) {
        return c;
    }

    @Provides
    @Singleton
    LayerDownloadCreator provideLayerDownloadCreator() {
        return new LayerDownloadCreatorImpl();
    }

    @Provides
    @IntoSet
    Creator layerDownloadCreatorIntoSet(LayerDownloadCreator c) {
        return c;
    }

    @Provides
    @Singleton
    RouteCreator provideRouteCreator() {
        return new RouteCreatorImpl();
    }

    @Provides
    @IntoSet
    Creator routeCreatorIntoSet(RouteCreator c) {
        return c;
    }

    @Provides
    @Singleton
    CameraCreator provideCameraCreator() {
        return new CameraCreatorImpl();
    }

    @Provides
    @IntoSet
    Creator cameraCreatorIntoSet(CameraCreator c) {
        return c;
    }

    @Provides
    @Singleton
    BroadcastCreator provideBroadcastCreator() {
        return new BroadcastCreatorImpl();
    }

    @Provides
    @IntoSet
    Creator broadcastCreatorIntoSet(BroadcastCreator c) {
        return c;
    }

    @Provides
    @Singleton
    MarkerCreator provideMarkerCreator() {
        return new MarkerCreatorImpl();
    }

    @Provides
    @IntoSet
    Creator markerCreatorIntoSet(MarkerCreator c) {
        return c;
    }

    @Provides
    @Singleton
    ShapeCreator provideShapeCreator() {
        return new ShapeCreatorImpl();
    }

    @Provides
    @IntoSet
    Creator shapeCreatorIntoSet(ShapeCreator c) {
        return c;
    }

    @Provides
    @Singleton
    ElevationCreator provideElevationCreator() {
        return new ElevationCreatorImpl();
    }

    @Provides
    @IntoSet
    Creator elevationCreatorIntoSet(ElevationCreator c) {
        return c;
    }

    @Provides
    @Singleton
    NotificationCreator provideNotificationCreator() {
        return new NotificationCreatorImpl();
    }

    @Provides
    @IntoSet
    Creator notificationCreatorIntoSet(NotificationCreator c) {
        return c;
    }

    @Provides
    @Singleton
    EmergencyCreator provideEmergencyCreator() {
        return new EmergencyCreatorImpl();
    }

    @Provides
    @IntoSet
    Creator emergencyCreatorIntoSet(EmergencyCreator c) {
        return c;
    }

    @Provides
    @Singleton
    ToolbarCreator provideToolbarCreator() {
        return new ToolbarCreatorImpl();
    }

    @Provides
    @IntoSet
    Creator toolbarCreatorIntoSet(ToolbarCreator c) {
        return c;
    }

    @Provides
    @Singleton
    AttachmentCreator provideAttachmentCreator() {
        return new AttachmentCreatorImpl();
    }

    @Provides
    @IntoSet
    Creator attachmentCreatorIntoSet(AttachmentCreator c) {
        return c;
    }

    @Provides
    @Singleton
    ScreenshotCreator provideScreenshotCreator() {
        return new ScreenshotCreatorImpl(pluginContext);
    }

    @Provides
    @IntoSet
    Creator screenshotCreatorIntoSet(ScreenshotCreator c) {
        return c;
    }

    @Provides
    @Singleton
    LayerCreator provideLayerCreator() {
        return new LayerCreatorImpl(pluginContext);
    }

    @Provides
    @IntoSet
    Creator layerCreatorIntoSet(LayerCreator c) {
        return c;
    }

    @Provides
    @Singleton
    CoordinateEntryCreator provideCoordinateEntryCreator() {
        return new CoordinateEntryCreatorImpl();
    }

    @Provides
    @IntoSet
    Creator coordinateEntryCreatorIntoSet(CoordinateEntryCreator c) {
        return c;
    }

    @Provides
    @Singleton
    TransferCreator provideTransferCreator() {
        return new TransferCreatorImpl();
    }

    @Provides
    @IntoSet
    Creator transferCreatorIntoSet(TransferCreator c) {
        return c;
    }

    @Provides
    @Singleton
    DataPackageCreator provideDataPackageCreator() {
        return new DataPackageCreatorImpl(pluginContext);
    }

    @Provides
    @IntoSet
    Creator dataPackageCreatorIntoSet(DataPackageCreator c) {
        return c;
    }

    @Provides
    @Singleton
    InspectCreator provideInspectCreator() {
        return new InspectCreatorImpl();
    }

    @Provides
    @IntoSet
    Creator inspectCreatorIntoSet(InspectCreator c) {
        return c;
    }

    @Provides
    @Singleton
    PromptCreator providePromptCreator() {
        return new PromptCreatorImpl();
    }

    @Provides
    @IntoSet
    Creator promptCreatorIntoSet(PromptCreator c) {
        return c;
    }

    @Provides
    @Singleton
    SpeechCreator provideSpeechCreator() {
        return new SpeechCreatorImpl(pluginContext);
    }

    @Provides
    @IntoSet
    Creator speechCreatorIntoSet(SpeechCreator c) {
        return c;
    }

    @Provides
    @Singleton
    SensorFovCreator provideSensorFovCreator() {
        return new SensorFovCreatorImpl();
    }

    @Provides
    @IntoSet
    Creator sensorFovCreatorIntoSet(SensorFovCreator c) {
        return c;
    }

    @Provides
    @Singleton
    LrfCreator provideLrfCreator() {
        return new LrfCreatorImpl();
    }

    @Provides
    @IntoSet
    Creator lrfCreatorIntoSet(LrfCreator c) {
        return c;
    }

    @Provides
    @Singleton
    StreamCreator provideStreamCreator() {
        return new StreamCreatorImpl();
    }

    @Provides
    @IntoSet
    Creator streamCreatorIntoSet(StreamCreator c) {
        return c;
    }

    @Provides
    @Singleton
    MapCaptureCreator provideMapCaptureCreator() {
        return new MapCaptureCreatorImpl();
    }

    @Provides
    @IntoSet
    Creator mapCaptureCreatorIntoSet(MapCaptureCreator c) {
        return c;
    }

    // Controllers are wired here too: constructor-injected with their Creators
    // (ADR-0003), they stay ATAK-free and testable through their interface.
    @Provides
    @Singleton
    RouteController provideRouteController(RouteCreator routeCreator) {
        return new RouteController(routeCreator, pluginContext);
    }

    @Provides
    @Singleton
    CameraController provideCameraController(CameraCreator cameraCreator) {
        return new CameraController(cameraCreator);
    }

    @Provides
    @Singleton
    SearchWidgetController provideSearchWidgetController(
            BroadcastCreator broadcastCreator) {
        return new SearchWidgetController(broadcastCreator);
    }

    @Provides
    @Singleton
    OverlayViewController provideOverlayViewController(
            BroadcastCreator broadcastCreator) {
        return new OverlayViewController(broadcastCreator);
    }

    @Provides
    @Singleton
    MarkerController provideMarkerController(MarkerCreator markerCreator,
            MenuCreator menuCreator) {
        return new MarkerController(markerCreator, menuCreator, pluginContext);
    }

    @Provides
    @Singleton
    ShapesController provideShapesController(ShapeCreator shapeCreator,
            MarkerCreator markerCreator) {
        return new ShapesController(shapeCreator, markerCreator,
                pluginContext);
    }

    @Provides
    @Singleton
    EmergencyController provideEmergencyController(
            EmergencyCreator emergencyCreator) {
        return new EmergencyController(emergencyCreator);
    }

    @Provides
    @Singleton
    LocationController provideLocationController(
            LocationCreator locationCreator) {
        return new LocationController(locationCreator);
    }

    @Provides
    @Singleton
    ElevationController provideElevationController(
            ElevationCreator elevationCreator) {
        return new ElevationController(elevationCreator, pluginContext);
    }

    @Provides
    @Singleton
    ContentProviderController provideContentProviderController() {
        return new ContentProviderController(pluginContext);
    }

    @Provides
    @Singleton
    NotificationController provideNotificationController(
            NotificationCreator notificationCreator,
            BroadcastCreator broadcastCreator) {
        return new NotificationController(notificationCreator,
                broadcastCreator, pluginContext);
    }

    @Provides
    @Singleton
    VideoController provideVideoController(VideoCreator videoCreator,
            BroadcastCreator broadcastCreator) {
        return new VideoController(videoCreator, broadcastCreator);
    }

    @Provides
    @Singleton
    ToolbarController provideToolbarController(ToolbarCreator toolbarCreator,
            BroadcastCreator broadcastCreator) {
        return new ToolbarController(toolbarCreator, broadcastCreator,
                pluginContext);
    }

    @Provides
    @Singleton
    AttachmentController provideAttachmentController(
            AttachmentCreator attachmentCreator) {
        return new AttachmentController(attachmentCreator);
    }

    @Provides
    @Singleton
    WebViewController provideWebViewController(
            BroadcastCreator broadcastCreator) {
        return new WebViewController(broadcastCreator);
    }

    @Provides
    @Singleton
    ScreenshotController provideScreenshotController(
            ScreenshotCreator screenshotCreator) {
        return new ScreenshotController(screenshotCreator, pluginContext);
    }

    @Provides
    @Singleton
    LayersController provideLayersController(LayerCreator layerCreator) {
        LayersController controller = new LayersController(layerCreator,
                pluginContext);
        // Inbound: the overlay-manager custom actions (assets/actions/*.xml)
        // re-enter the plugin by broadcast; the Creator adapts them onto the
        // Controller's LayerActionPort. Registered here so the actions work
        // from plugin load, exactly when the legacy MapComponent registration
        // took effect.
        layerCreator.registerLayerActions(controller);
        return controller;
    }

    @Provides
    @Singleton
    CoordinateEntryController provideCoordinateEntryController(
            CoordinateEntryCreator coordinateEntryCreator) {
        return new CoordinateEntryController(coordinateEntryCreator,
                pluginContext);
    }

    @Provides
    @Singleton
    TransferController provideTransferController(
            TransferCreator transferCreator, ImportCreator importCreator) {
        return new TransferController(transferCreator, importCreator);
    }

    @Provides
    @Singleton
    DataPackageController provideDataPackageController(
            DataPackageCreator dataPackageCreator,
            ImportCreator importCreator) {
        return new DataPackageController(dataPackageCreator, importCreator);
    }

    @Provides
    @Singleton
    InspectController provideInspectController(InspectCreator inspectCreator) {
        return new InspectController(inspectCreator);
    }

    @Provides
    @Singleton
    CameraCaptureController provideCameraCaptureController() {
        // No Creator behind this one: CameraActivity, its broadcast and the
        // startActivity launch are pure Android (see the Controller's
        // javadoc). ADR-0005 still gives the feature a Controller.
        return new CameraCaptureController();
    }

    @Provides
    @Singleton
    BumpController provideBumpController(PromptCreator promptCreator) {
        return new BumpController(promptCreator, pluginContext);
    }

    @Provides
    @Singleton
    SpeechController provideSpeechController(SpeechCreator speechCreator,
            MarkerCreator markerCreator, CameraCreator cameraCreator,
            BroadcastCreator broadcastCreator,
            EmergencyCreator emergencyCreator) {
        return new SpeechController(speechCreator, markerCreator,
                cameraCreator, broadcastCreator, emergencyCreator,
                pluginContext);
    }

    @Provides
    @Singleton
    MenuController provideMenuController(MenuCreator menuCreator) {
        return new MenuController(menuCreator);
    }

    @Provides
    @Singleton
    IssController provideIssController(MarkerCreator markerCreator) {
        return new IssController(markerCreator);
    }

    @Provides
    @Singleton
    SensorFovController provideSensorFovController(
            SensorFovCreator sensorFovCreator) {
        return new SensorFovController(sensorFovCreator);
    }

    // Constructing the controller registers the LRF point tool with ATAK's
    // tool manager (legacy parity: the receiver's constructor did); the
    // PaneController dispose cascade tears it down.
    @Provides
    @Singleton
    LrfController provideLrfController(LrfCreator lrfCreator) {
        return new LrfController(lrfCreator, pluginContext);
    }

    @Provides
    @Singleton
    StreamController provideStreamController(StreamCreator streamCreator,
            ImportCreator importCreator) {
        return new StreamController(streamCreator, importCreator);
    }

    @Provides
    @Singleton
    MapCaptureController provideMapCaptureController(
            MapCaptureCreator mapCaptureCreator) {
        return new MapCaptureController(mapCaptureCreator);
    }

    // The Pane controller owns the hello-world pane's view wiring and
    // dispatches taps to the feature Controllers it is constructed with
    // (every feature gets one — ADR-0005).
    @Provides
    @Singleton
    PaneController providePaneController(RouteController routeController,
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
        return new PaneController(pluginContext, routeController,
                cameraController, searchWidgetController,
                overlayViewController, markerController, emergencyController,
                shapesController, locationController, elevationController,
                contentProviderController, notificationController,
                videoController, toolbarController, attachmentController,
                webViewController, screenshotController, layersController,
                coordinateEntryController, transferController,
                dataPackageController, inspectController,
                cameraCaptureController, bumpController, speechController,
                menuController, issController, sensorFovController,
                lrfController, streamController, mapCaptureController);
    }

    // ---- Humble-shell probes + the assembled systems check

    @Provides
    @ElementsIntoSet
    Set<ShellProbe> provideShellProbes() {
        // One probe per LAZILY-created Humble shell (eager shells fail loudly at
        // load on their own): construct + dispose the real shell so a base-type
        // break surfaces now, not at first open.
        return Collections.singleton(
                new NavigationStackShellProbe(pluginContext));
    }

    @Provides
    @Singleton
    SystemsCheck provideSystemsCheck(Set<Creator> creators,
            Set<ShellProbe> shellProbes) {
        // Singleton so the graph retains the check — and, through it, the
        // load-run's CheckReport — rather than assembling a fresh one per call.
        return new SystemsCheck(creators, shellProbes);
    }
}
