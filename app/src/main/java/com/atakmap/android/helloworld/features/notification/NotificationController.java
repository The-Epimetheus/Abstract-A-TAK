package com.atakmap.android.helloworld.features.notification;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.atakmap.android.helloworld.abstraction.Disposable;
import com.atakmap.android.helloworld.features.broadcast.BroadcastCreator;

/**
 * The notification feature's Controller: the three notification demos plus the
 * fake-phone-call listen-back, all ATAK-free — the host-flavored plumbing
 * (stock icons, ATAK channel, the ATAKActivity trampoline, the broadcast bus)
 * lives behind {@link NotificationCreator} and {@link BroadcastCreator}. Holds
 * a live broadcast registration, so it is {@link Disposable}; the Pane
 * controller's dispose cascade tears it down.
 */
public class NotificationController implements Disposable {

    private static final String TAG = "NotificationController";

    /**
     * Broadcast the fake-phone-call action re-enters the plugin on; this
     * Controller listens for it, toasts the carried extras and cancels the
     * notification named by the intent's {@code "notificationId"} int extra.
     */
    public static final String FAKE_PHONE_CALL_ACTION = "com.atakmap.android.helloworld.FAKE_PHONE_CALL";

    private final NotificationCreator notificationCreator;
    private final Context pluginContext;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    /** Live fake-phone-call registration; closed by {@link #dispose()}. */
    private final BroadcastCreator.Registration fakePhoneCallRegistration;

    public NotificationController(NotificationCreator notificationCreator,
            BroadcastCreator broadcastCreator, Context pluginContext) {
        this.notificationCreator = notificationCreator;
        this.pluginContext = pluginContext;
        this.fakePhoneCallRegistration = broadcastCreator.listen(
                FAKE_PHONE_CALL_ACTION,
                "fake-phone-call demo: toasts the delivered extras and cancels"
                        + " the posted notification by its \"notificationId\""
                        + " int extra",
                this::onFakePhoneCall);
    }

    /**
     * Start the plugin's own foreground {@code NotificationService} — THE
     * pattern for plugin-specific notifications. The service runs outside
     * ATAK's classloader paradigm (like the camera/speech activities), so it
     * is addressed by explicit action + package and must be started with the
     * host context.
     */
    public void startPluginNotificationService(Context hostContext) {
        Intent startServiceIntent = new Intent(
                "com.atakmap.android.helloworld.notification.NotificationService");
        startServiceIntent.setPackage("com.atakmap.android.helloworld.plugin");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            hostContext.startForegroundService(startServiceIntent);
        else
            hostContext.startService(startServiceIntent);
    }

    /**
     * Post 30 notifications back-to-back through ATAK's NotificationUtil.
     * This exists to reproduce a NW ROM bug — it WILL crash a NW device — and
     * is NOT the pattern for plugin notifications (see
     * {@link #startPluginNotificationService}).
     */
    public void spamStatusNotifications() {
        int indx = 10000;
        Log.d(TAG, "spam using the NotificationUtil");
        for (int i = 0; i < 30; ++i) {
            String contentTitle = "Test Spammer: " + i;
            notificationCreator.postStatusNotification(indx + i, contentTitle);
        }
    }

    /**
     * Post the fake-phone-call notification: its one action button bounces
     * through ATAKActivity back into the plugin as a
     * {@link #FAKE_PHONE_CALL_ACTION} broadcast; the listener toasts the
     * carried extras and cancels the notification by the id on the intent.
     */
    public void postFakePhoneCallNotification() {
        notificationCreator.postActionNotification(ActionNotificationSpec
                .builder()
                .title("Test Notification")
                .actionLabel("Phone Call")
                .actionBroadcast(FAKE_PHONE_CALL_ACTION)
                .extra("mytime", "my time: " + System.currentTimeMillis())
                .build());
    }

    /**
     * The fake-phone-call action arrived (the user tapped the notification's
     * action button; ATAKActivity re-dispatched the internal intent onto the
     * bus): toast the action + "mytime" extra, then cancel the posted
     * notification by the "notificationId" int extra the seam stamped on the
     * intent. The bus may deliver off the main thread, so the toast hops —
     * the same reason the legacy receiver posted it.
     */
    private void onFakePhoneCall(final Intent intent) {
        Log.d(TAG, "intent: " + intent.getAction() + " "
                + intent.getStringExtra("mytime"));
        mainHandler.post(() -> Toast.makeText(pluginContext,
                "intent: " + intent.getAction() + " "
                        + intent.getStringExtra("mytime"),
                Toast.LENGTH_LONG).show());
        int id = intent.getIntExtra("notificationId", 0);
        Log.d(TAG, "cancelling id: " + id);
        if (id > 0) {
            notificationCreator.cancelNotification(id);
        }
    }

    /** Close the fake-phone-call registration ({@code close()} is idempotent). */
    @Override
    public void dispose() {
        fakePhoneCallRegistration.close();
    }
}
