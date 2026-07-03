package com.atakmap.android.helloworld.features.shape;

import android.content.Context;
import android.graphics.Color;

import com.atakmap.android.helloworld.features.marker.MarkerCreator;
import com.atakmap.android.helloworld.features.marker.MarkerSpec;
import com.atakmap.android.helloworld.plugin.R;

/**
 * The drawing-shapes feature's Controller: the five shape demos, ATAK-free,
 * depending only on {@link ShapeCreator} and {@link MarkerCreator}. The Pane
 * controller forwards the taps here.
 */
public class ShapesController {

    private static final String DETECT_UID = "detect-ae:3e:ee";

    /** A 32x32 detection glyph, inlined so the demo has no asset dependency. */
    private static final String DETECT_ICON_BASE64 = "base64:\\iVBORw0KGgoAAAANSUhEUgAAACAAAAAgCAYAAABzenr0AAAABHNCSVQICAgIfAhkiAAAAmJJREFUWIXNl79LG1EcwD93zSWN2oJ1ECQEDoyg2Ey1UHAJFUIFhyq0S0PAsX9Cl0KnQql_QDOeFETo0pLWQTMUDR10UIpVA-mQRRKjQ7locrnr0CTkmmt-3J3aL3yH-_Le-3zeu8e7d-AsQrW8lggBR7W8cokQcCSJYkkSxdJVSzTgG_F4dSMerzqREGzAk5IoBtZiMW9ElkWAVDarRxWlXNH1HDBbk3FdwBJeD7sS3Qq0hTuR6EagK7hdiU4CPcHtSLQTsAXvVeJfAo7gvUhYCbgC71bib4EW-IWm8WJ9nYWJCS40jU-Hh6YOwwMDPJ-aYimdbgx4y-fjoSxzd3i4o0Tz7CxnXtF1ltJpfp6dkSkWSezs0O_1NrJPkjjXNF6mUvwoFDjXND4eHHA_kWD3-BiAiCyLa7GYVxLFAJCk6cT0tINbxW2fj1eRiKlWUFUAYuEwj0IhDMPg8coKbzY3UebnTRJRRQlUdD1ZXwmxFzjAiaryZHW1kcu7uy1tBEFgbmyMz5mMqW61Ep6W3h3CL0ksjI83nseGhizbCYLATU_n4T382RCzFV1PRhWl4yr0SRJPJydNtforaI5vuRz3RkZMNavNWFfsWuJXuczbrS1T7Vk4DMCH_X2-5_NkT095t73N18XFtnCAG03jFIEvumHMvd_b658OBkV5cFAwDINytcqDQACfx4NhGORV1ZTR0VEKqooBnJRK3PH7eT0zw3Qw2BYO_-FB5KqE3aPYFQmnHyNHEm59jm1JuH0h6Unisq5kXUlc9qW0rYSTa7mdcPXHxLHEdcBNEtcFb5ZwBP8NALbcQk1BI7gAAAAASUVORK5CYII=";

    private final ShapeCreator shapeCreator;
    private final MarkerCreator markerCreator;
    private final Context pluginContext;

    public ShapesController(ShapeCreator shapeCreator,
            MarkerCreator markerCreator, Context pluginContext) {
        this.shapeCreator = shapeCreator;
        this.markerCreator = markerCreator;
        this.pluginContext = pluginContext;
    }

    /**
     * The range/bearing-circle demo: a hostile "detect" marker with a base64
     * icon and an accuracy ellipse that follows it for its whole life. To
     * persist a circle, persist its center marker — not the circle itself,
     * which is rebuilt from the marker.
     *
     * @return false if the marker was already placed (nothing re-placed).
     */
    public boolean placeDetectMarkerWithAccuracyEllipse() {
        if (markerCreator.markerExists(DETECT_UID))
            return false;

        markerCreator.placeMarker(MarkerSpec.at(32, -172)
                .dropAsIfByUser()
                .uid(DETECT_UID)
                .callsign("detect 1")
                .type("a-h-G")
                .showCotDetails(false)
                .neverPersist(true)
                .iconUri(DETECT_ICON_BASE64)
                .persist()
                .build());

        shapeCreator.attachAccuracyEllipse(AccuracyEllipseSpec
                .followingMarker(DETECT_UID)
                .name("Error Ellipse")
                .dimensionsMeters(20, 20)
                .fillColorArgb(Color.argb(50, 238, 187, 255))
                .strokeColorArgb(Color.GREEN)
                .strokeWeight(4)
                .build());
        return true;
    }

    /** A plain white rectangle, dispatched externally as a CoT event. */
    public void addRectangleDemo() {
        shapeCreator.drawRectangle(RectangleSpec.builder()
                .corner(10, 10)
                .corner(10, 5)
                .corner(5, 5)
                .corner(5, 10)
                .basicStyle()
                .fillColorArgb(0x00000000)
                .strokeColorArgb(Color.WHITE)
                .title("Test Rectangle")
                .dispatchAsCot()
                .build());
    }

    /**
     * The shape showcase: one of everything — a multi-polyline of two lines,
     * two standalone polygons, a rectangle, a circle, an ellipse, and two
     * altitude-mode lines (absolute vs clamp-to-ground). Redraw is idempotent:
     * every spec carries a fixed uid, and drawing replaces by uid.
     */
    public void drawShapesShowcase() {
        shapeCreator.drawMultiPolyline(MultiPolylineSpec.builder()
                .uid("list-1")
                .archive(false)
                .editable(false)
                .movable(false)
                .child(ShapeSpec.builder()
                        .uid("ds-1")
                        .title("DrawingShape-1")
                        .archive(false)
                        .strokeColorArgb(Color.RED)
                        .point(0, 0).point(1, 1).point(2, 1)
                        .heightMeters(100)
                        .closed()
                        .editable(false)
                        .build())
                .child(ShapeSpec.builder()
                        .uid("ds-2")
                        .title("DrawingShape-2")
                        .archive(false)
                        .strokeColorArgb(Color.BLUE)
                        .point(0, 0).point(-1, -1).point(-2, -1)
                        .heightMeters(200)
                        .closed()
                        .editable(false)
                        .build())
                .build());

        shapeCreator.drawShape(ShapeSpec.builder()
                .uid("ds-3")
                .title("DrawingShape-3")
                .archive(false)
                .point(0, 0).point(2, 0).point(2, -1)
                .closed()
                .strokeColorArgb(Color.YELLOW)
                .heightMeters(300)
                .movable(false)
                .editable(false)
                .build());

        shapeCreator.drawShape(ShapeSpec.builder()
                .uid("ds-4")
                .title("DrawingShape-4")
                .archive(false)
                .point(0, 0).point(-2, 0).point(-2, 1)
                .strokeColorArgb(Color.GREEN)
                .meta("gotcha", false)
                .heightMeters(400)
                .closed()
                .movable(false)
                .editable(false)
                .build());

        shapeCreator.drawRectangle(RectangleSpec.builder()
                .uid("rect-1")
                .title("rectangle")
                .archive(false)
                .editable(false)
                .corner(3, 3)
                .corner(3, 2.5)
                .corner(2.5, 2.5)
                .corner(2.5, 3)
                .build());

        shapeCreator.drawCircle(CircleSpec.centeredAt(2d, 2d, 2000)
                .uid("circle-1")
                .title("circle")
                .archive(false)
                .editable(false)
                .strokeColorArgb(Color.RED)
                .fillColorArgb(Color.BLACK)
                .build());

        shapeCreator.drawEllipse(EllipseSpec.centeredAt(1d, 2d)
                .uid("ellipse-1")
                .title("ellipse")
                .archive(false)
                .editable(false)
                .strokeColorArgb(Color.GREEN)
                .fillColorArgb(Color.YELLOW)
                .dimensionsMeters(2000, 500)
                .angleDegrees(0)
                .build());

        shapeCreator.drawShape(ShapeSpec.builder()
                .uid("line1")
                .archive(false)
                .point(7, 3, -100)
                .point(7, 4, -100)
                .altitudeMode(ShapeSpec.AltitudeMode.ABSOLUTE)
                .strokeColorArgb(Color.RED)
                .build());

        shapeCreator.drawShape(ShapeSpec.builder()
                .uid("line2")
                .archive(false)
                .point(7.2, 3.2, -100)
                .point(7.2, 4.2, -100)
                .altitudeMode(ShapeSpec.AltitudeMode.CLAMP_TO_GROUND)
                .strokeColorArgb(Color.MAGENTA)
                .build());
    }

    /**
     * A custom map group with its own overlay-manager entry, holding a green
     * polygon and a marker — toggleable as one layer.
     */
    public void addCustomGroupWithShapeAndMarker() {
        String iconUri = "android.resource://"
                + pluginContext.getPackageName()
                + "/" + R.drawable.ic_launcher_badge;
        shapeCreator.createGroupWithOverlay("MyCustomGroup", iconUri);

        shapeCreator.drawShape(ShapeSpec.builder()
                .groupPath("MyCustomGroup")
                .point(43.08321804, -77.67835268)
                .point(43.09321804, -77.67835268)
                .point(43.09321804, -77.67935268)
                .point(43.08821804, -77.67895268)
                .point(43.08321804, -77.67935268)
                .point(43.08321804, -77.67835268)
                .clickable()
                .editable(true)
                .closed()
                .strokeColorArgb(Color.DKGRAY)
                .fillColorArgb(Color.GREEN)
                .title("test polygon")
                .archive(false)
                .build());

        markerCreator.placeMarker(MarkerSpec
                .at(43.10321804, -77.67835268)
                .type("a-u-g")
                .title("ovTest")
                .meta("callsign", "ovTest")
                .meta("title", "ovTest")
                .meta("readiness", true)
                .meta("archive", false)
                .meta("editable", true)
                .meta("movable", true)
                .meta("removable", true)
                .meta("entry", "user")
                .showLabel()
                .groupPath("MyCustomGroup")
                .build());
    }

    /** Two friendly markers joined by a red association line. */
    public void addAssociationDemo() {
        String first = markerCreator.placeMarker(MarkerSpec.at(43, -72)
                .dropAsIfByUser()
                .type("a-f")
                .neverPersist(true)
                .build());
        String second = markerCreator.placeMarker(MarkerSpec.at(43.1, -72.2)
                .dropAsIfByUser()
                .type("a-f")
                .neverPersist(true)
                .build());
        if (first != null && second != null)
            shapeCreator.associate(first, second, Color.RED);
    }
}
