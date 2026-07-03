package com.atakmap.android.helloworld.features.search;

import android.content.Intent;

import com.atakmap.android.helloworld.features.broadcast.BroadcastCreator;

/**
 * The search-widget feature's Controller. A bare pass-through to
 * {@link BroadcastCreator} — kept anyway so every feature shows the same
 * tap → Controller → Creator shape (ADR-0005); the widget itself listens for
 * this action on the map component side.
 */
public class SearchWidgetController {

    /** The action the custom map widget demo listens for. */
    public static final String SHOW_SEARCH_WIDGET = "SHOW_MY_WACKY_SEARCH";

    private final BroadcastCreator broadcastCreator;

    public SearchWidgetController(BroadcastCreator broadcastCreator) {
        this.broadcastCreator = broadcastCreator;
    }

    /** Ask the custom search map widget to show itself. */
    public void showSearchWidget() {
        broadcastCreator.send(new Intent(SHOW_SEARCH_WIDGET));
    }
}
