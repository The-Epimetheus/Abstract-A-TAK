package com.atakmap.android.helloworld.features.pane;

import android.content.Context;

/**
 * The Humble shell as the {@link PaneController} sees it: the few operations
 * that belong to the live DropDown (and its sibling panes) plus the map
 * primitives the shell extracts at tap time. Plugin-owned and ATAK-free —
 * the shell implements it with the ATAK calls ({@code resize},
 * {@code DropDownManager}, {@code MapView}) on its side of the boundary.
 */
public interface PaneShell {

    /** Resize the hello-world pane; fractions of the screen (1.0 = full). */
    void resizePane(double widthFraction, double heightFraction);

    /** Hide the hello-world pane. */
    void hidePane();

    /**
     * Retain the hello-world pane: keep it on the drop-down back stack when
     * another pane opens on top, so it is still there when that pane closes
     * (ATAK {@code DropDownReceiver.setRetain(true)} semantics).
     */
    void retainPane();

    /** The map center at call time, as {@code {latitude, longitude}}. */
    double[] mapCenterLatLon();

    /** The host activity context (dialogs must use it, not the plugin context). */
    Context hostContext();

    /** Show the recycler-view demo pane (retaining this one underneath). */
    void showRecyclerPane();

    /** Show the tab-view demo pane (retaining this one underneath). */
    void showTabPane();

    /** Build the navigation-stack demo pane and push it. */
    void pushNavigationStackPane();
}
