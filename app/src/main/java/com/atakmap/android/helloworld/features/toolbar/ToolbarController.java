package com.atakmap.android.helloworld.features.toolbar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.atakmap.android.helloworld.abstraction.Disposable;
import com.atakmap.android.helloworld.features.broadcast.BroadcastCreator;

/**
 * The toolbar feature's Controller: the Ford TowTruck toolbar-item demo and
 * the action-bar icon badge-count demo, ATAK-free — the
 * {@code ActionMenuData} family stays behind {@link ToolbarCreator}, the
 * broadcast bus behind {@link BroadcastCreator}. Holds a live receiver
 * registration, so it is {@link Disposable}; {@code PaneController.dispose()}
 * cascades here.
 */
public class ToolbarController implements Disposable {

    /**
     * Broadcast a tap on the TowTruck toolbar item fires — declared by the
     * item's own spec, heard by this Controller's registered receiver.
     */
    public static final String FORD_CLICK_ACTION = "com.ford.tool.showtoast";

    /**
     * Broadcast {@code HelloWorldTool} listens for: each send bumps the badge
     * count shown on the plugin's own action-bar icon.
     */
    public static final String ICON_COUNT_ACTION = "com.atakmap.android.helloworld.plugin.iconcount";

    /** The demo toolbar item, exactly as the legacy receiver described it. */
    private final ToolbarItemSpec fordTowTruck = ToolbarItemSpec.builder()
            .ref("com.ford.tool/TowTruck")
            .title("Ford TowTruck")
            .iconName("ic_menu_drawing")
            .clickBroadcastAction(FORD_CLICK_ACTION)
            .placement(ToolbarPlacement.OVERFLOW)
            .build();

    private final ToolbarCreator toolbarCreator;
    private final BroadcastCreator broadcastCreator;
    private final Context pluginContext;

    private Disposable fordClickRegistration;

    public ToolbarController(ToolbarCreator toolbarCreator,
            BroadcastCreator broadcastCreator, Context pluginContext) {
        this.toolbarCreator = toolbarCreator;
        this.broadcastCreator = broadcastCreator;
        this.pluginContext = pluginContext;
    }

    /**
     * Listen for taps on the TowTruck toolbar item and toast in response.
     * Registered once at pane wiring for the life of the plugin (the legacy
     * receiver registered at construction, the same moment) and torn down by
     * {@link #dispose()}. Idempotent — a second call is a no-op.
     */
    public void listenForFordToolClicks() {
        if (fordClickRegistration != null)
            return;
        BroadcastReceiver fordReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Toast.makeText(pluginContext,
                        "Ford Tow Truck Application", Toast.LENGTH_SHORT)
                        .show();
            }
        };
        fordClickRegistration = broadcastCreator.register(fordReceiver,
                FORD_CLICK_ACTION,
                "toast when the demo TowTruck toolbar item is tapped");
    }

    /** Put the Ford TowTruck item on ATAK's toolbar (overflow menu). */
    public void showFordTowTruckItem() {
        toolbarCreator.addToolbarItem(fordTowTruck);
    }

    /** Take the Ford TowTruck item back off the toolbar. */
    public void hideFordTowTruckItem() {
        toolbarCreator.removeToolbarItem(fordTowTruck.ref());
    }

    /**
     * Bump the badge count on the plugin's action-bar icon.
     * {@code HelloWorldTool} hears the broadcast and increments the count on
     * its {@code NavButtonModel}.
     */
    public void bumpIconCount() {
        broadcastCreator.send(new Intent(ICON_COUNT_ACTION));
    }

    @Override
    public void dispose() {
        if (fordClickRegistration != null) {
            fordClickRegistration.dispose();
            fordClickRegistration = null;
        }
    }
}
