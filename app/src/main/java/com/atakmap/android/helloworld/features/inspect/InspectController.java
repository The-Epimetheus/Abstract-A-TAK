package com.atakmap.android.helloworld.features.inspect;

import android.app.AlertDialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.widget.TextView;
import android.widget.Toast;

import com.atakmap.android.helloworld.abstraction.Disposable;

/**
 * The map-item inspect feature's Controller: run the host's item-selection
 * tool and show the picked item's CoT representation in a copyable dialog.
 * ATAK-free — the selection tool, its start/finish broadcast round trip, and
 * the item-to-CoT conversion live behind {@link InspectCreator}; the dialog
 * and clipboard here are plain Android (boundary-legal).
 *
 * <p>{@link Disposable} because the feature holds live host registrations
 * (the selection tool itself, plus a one-shot finish listener while a pick is
 * running); PaneController's dispose cascade tears them down on unload.
 */
public class InspectController implements Disposable {

    private final InspectCreator inspectCreator;

    public InspectController(InspectCreator inspectCreator) {
        this.inspectCreator = inspectCreator;
    }

    /**
     * Start the map-item inspector. {@code onToolFinished} runs when the tool
     * ends for ANY reason (selection made, invalid selection, or
     * {@link #stopInspection() cancelled}) so the caller can clear its
     * toggled button state; a resolvable selection then also gets the
     * "Resulting CoT" dialog. {@code hostContext} must be the host (map)
     * context — a dialog cannot be built on the plugin context.
     */
    public void startInspection(final Context hostContext,
            final Runnable onToolFinished) {
        inspectCreator.startItemInspection(result -> {
            onToolFinished.run();
            if (result == null)
                return;
            showResultDialog(hostContext, result);
        });
    }

    /** Cancel a running inspection; the start callback still fires. */
    public void stopInspection() {
        inspectCreator.stopItemInspection();
    }

    /**
     * Show the inspected item's CoT in a dialog. The text is selectable, and
     * long-pressing it copies the whole payload to the clipboard — CoT XML is
     * exactly the kind of thing you want to paste somewhere else.
     */
    private void showResultDialog(Context hostContext,
            InspectionResult result) {
        String val;
        if (result.cotXml() != null)
            val = result.cotXml();
        else if (result.neverCot())
            val = "map item set to never persist (nevercot)";
        else
            val = "error turning a map item into CoT";

        AlertDialog.Builder builderSingle = new AlertDialog.Builder(
                hostContext);
        TextView showText = new TextView(hostContext);
        showText.setText(val);
        showText.setTextIsSelectable(true);
        showText.setOnLongClickListener(v -> {
            // Copy the Text to the clipboard
            ClipboardManager manager = (ClipboardManager) hostContext
                    .getSystemService(Context.CLIPBOARD_SERVICE);
            TextView showTextParam = (TextView) v;
            manager.setText(showTextParam.getText());
            Toast.makeText(v.getContext(),
                    "copied the data", Toast.LENGTH_SHORT).show();
            return true;
        });

        builderSingle.setTitle("Resulting CoT");
        builderSingle.setView(showText);
        builderSingle.show();
    }

    /** Tear down the selection tool + any armed finish listener. Idempotent. */
    @Override
    public void dispose() {
        inspectCreator.disposeTool();
    }
}
