package com.atakmap.android.helloworld.features.webview;

import android.content.Intent;

import com.atakmap.android.helloworld.WebViewDropDownReceiver;
import com.atakmap.android.helloworld.features.broadcast.BroadcastCreator;

/**
 * The webview feature's Controller. A bare pass-through to
 * {@link BroadcastCreator} — kept anyway so every feature shows the same
 * tap → Controller → Creator shape (ADR-0005). The legacy
 * {@link WebViewDropDownReceiver} owns the receiving side (and the webview
 * pane itself); it is separate debt awaiting its own migration round, so this
 * Controller signals it by broadcast exactly as the legacy button did.
 */
public class WebViewController {

    private final BroadcastCreator broadcastCreator;

    public WebViewController(BroadcastCreator broadcastCreator) {
        this.broadcastCreator = broadcastCreator;
    }

    /** Ask the webview drop-down to show itself (opens on top of this pane). */
    public void showWebView() {
        Intent webViewIntent = new Intent();
        webViewIntent.setAction(WebViewDropDownReceiver.SHOW_WEBVIEW);
        broadcastCreator.send(webViewIntent);
    }
}
