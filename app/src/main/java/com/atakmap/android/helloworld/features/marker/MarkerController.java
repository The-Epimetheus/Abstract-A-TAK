package com.atakmap.android.helloworld.features.marker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.util.Base64;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.atakmap.android.helloworld.features.menu.MenuCreator;
import com.atakmap.android.helloworld.plugin.R;

import java.io.ByteArrayOutputStream;

/**
 * The marker feature's Controller: the marker-placement demos, ATAK-free,
 * depending only on {@link MarkerCreator} and {@link MenuCreator}. The Pane
 * controller forwards the taps here.
 */
public class MarkerController {

    private static final String TAG = "MarkerController";

    private final MarkerCreator markerCreator;
    private final MenuCreator menuCreator;
    private final Context pluginContext;

    public MarkerController(MarkerCreator markerCreator,
            MenuCreator menuCreator, Context pluginContext) {
        this.markerCreator = markerCreator;
        this.menuCreator = menuCreator;
        this.pluginContext = pluginContext;
    }

    /**
     * The special-wheel demo: a friendly unit marker at the map center with
     * the plugin's custom radial menu, persisted and announced. (Overriding
     * the menu of ALL markers of a type takes searching existing items plus an
     * ITEM_ADDED listener — this demo overrides just the one it places.)
     */
    public void placeUnitAtCenter() {
        String uid = markerCreator.placeMarker(MarkerSpec.atMapCenter()
                .type("a-f-G-U-C-I")
                .meta("readiness", true)
                .meta("archive", true)
                .meta("how", "h-g-i-g-o")
                .meta("editable", true)
                .meta("movable", true)
                .meta("removable", true)
                .meta("entry", "user")
                .meta("callsign", "Test Marker")
                .title("Test Marker")
                .menu(menuCreator.loadRadialMenu("menu.xml"))
                .groupPath("Cursor on Target", "Friendly")
                .persistAndAnnounce()
                .build());
        Log.d(TAG, "created a new unit marker: " + uid);
    }

    /**
     * An aircraft dropped the way a user would drop it, with the icon freed
     * for full rotation (heading mask + no-arrow mask) and a track set.
     */
    public void placeAircraftAtCenter() {
        markerCreator.placeMarker(MarkerSpec.atMapCenter()
                .dropAsIfByUser()
                .callsign("SNF")
                .type("a-f-A")
                .showCotDetails(false)
                .neverPersist(true)
                .fullRotation()
                .track(310, 20)
                .colorArgb(Color.YELLOW)
                .iconsetPath(
                        "34ae1613-9645-4222-a9d2-e5f243dea2865/Military/A10.png")
                .build());
    }

    /**
     * An aircraft that stales itself out: CoT is created but not archived, the
     * marker aged out 20s after placement, and the event dispatched externally.
     */
    public void placeStaleoutAircraftAtCenter() {
        markerCreator.placeMarker(MarkerSpec.atMapCenter()
                .dropAsIfByUser()
                .showCotDetails(false)
                .archive(false)
                .type("a-f-A")
                .callsign("WT888")
                .autoStaleAfterMillis(20000)
                .meta("movable", false)
                .track(280, 50)
                .meta("Speed", 50d)
                .rotateWithHeading()
                .dispatchAsCot()
                .build());
    }

    /** A marker whose icon is the plugin's SVG drawable, inlined as base64. */
    public void placeSvgMarkerAtCenter() {
        Bitmap icon = getBitmap(pluginContext, R.drawable.svg_example);
        if (icon == null) {
            Log.w(TAG, "svg example drawable could not be rasterized");
            return;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        icon.compress(Bitmap.CompressFormat.PNG, 100, baos);
        String encoded = "base64://" + Base64.encodeToString(baos.toByteArray(),
                Base64.NO_WRAP | Base64.URL_SAFE);

        markerCreator.placeMarker(MarkerSpec.atMapCenter()
                .title("HelloWorld")
                .type("custom-type")
                .alwaysShowText()
                .clickable()
                .iconUri(encoded)
                .build());
    }

    /**
     * A marker with a fully custom CoT type ("b-g-n-M-O-B"), a plugin
     * drawable as its icon, and one of ATAK's stock radial menus picked by
     * path. Ten seconds after placement its label text turns red — label
     * styling stays live-mutable after the marker is on the map.
     */
    public void placeCustomTypeMarker() {
        final String uid = markerCreator.placeMarker(MarkerSpec.atMapCenter()
                .type("b-g-n-M-O-B")
                .meta("how", "h-g-i-g-o")
                .meta("callsign", "Custom Marker")
                .title("Custom Marker")
                .menu("menus/a-n.xml")
                // prevents the icon from changing automatically
                .meta("adapt_marker_icon", false)
                .iconUri("android.resource://"
                        + pluginContext.getPackageName()
                        + "/" + R.drawable.abc)
                // an icon straight off disk works too:
                // .iconUri("file:///sdcard/custom_marker.png")
                .groupPath("Cursor on Target")
                .build());
        Log.d(TAG, "creating a new marker for: " + uid);

        // looking for the color red for the text
        Thread t = new Thread() {
            public void run() {
                try {
                    Thread.sleep(10000);
                } catch (Exception ignored) {
                }
                markerCreator.setMarkerTextColor(uid, 0xFFFF0000);
                Log.d(TAG, "text color set");
            }
        };
        t.start();
    }

    private static Bitmap getBitmap(Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        if (drawable instanceof BitmapDrawable) {
            return BitmapFactory.decodeResource(context.getResources(),
                    drawableId);
        } else if (drawable instanceof VectorDrawable) {
            VectorDrawable vectorDrawable = (VectorDrawable) drawable;
            Bitmap bitmap = Bitmap.createBitmap(
                    vectorDrawable.getIntrinsicWidth(),
                    vectorDrawable.getIntrinsicHeight(),
                    Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            vectorDrawable.setBounds(0, 0, canvas.getWidth(),
                    canvas.getHeight());
            vectorDrawable.draw(canvas);
            return bitmap;
        } else {
            return null;
        }
    }
}
