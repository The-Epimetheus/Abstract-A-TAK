package com.atakmap.android.helloworld.features.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.atakmap.android.helloworld.abstraction.SelfCheckResult;
import com.atakmap.android.helloworld.abstraction.SelfChecks;
import com.atakmap.android.maps.MapView;
import com.atakmap.android.util.NotificationUtil;
import com.atakmap.android.util.PendingIntentHelper;

import java.util.Map;
import java.util.UUID;

/**
 * The only place ATAK's notification plumbing is touched: NotificationUtil
 * for stock-icon posts, and the ATAKActivity trampoline + ATAK notification
 * channel for action notifications. The icons come from ATAK core resources
 * ({@code com.atakmap.app.R}) — exactly the kind of host reference that must
 * not leak into {@code src/main}.
 */
public final class NotificationCreatorImpl implements NotificationCreator {

    @Override
    public String id() {
        return "NotificationCreator";
    }

    @Override
    public void postStatusNotification(int notificationId, String text) {
        // Stock team icon; title, ticker and body all carry the same text —
        // matches the legacy demo. Vibrates, no tap intent.
        NotificationUtil.getInstance().postNotification(notificationId,
                com.atakmap.app.R.drawable.team_human, text, text, text, null,
                true);
    }

    @Override
    public void postActionNotification(ActionNotificationSpec spec) {
        MapView mv = MapView.getMapView();
        Context hostContext = mv.getContext();

        // A PendingIntent cannot target plugin classes (they live behind
        // ATAK's plugin classloader). The action instead brings ATAKActivity
        // to the front carrying the plugin's broadcast as "internalIntent";
        // ATAK re-dispatches that on its internal bus, where the plugin's
        // registered receiver can hear it.
        Intent atakFrontIntent = new Intent();
        atakFrontIntent.setComponent(new ComponentName("com.atakmap.app.civ",
                "com.atakmap.app.ATAKActivity"));
        atakFrontIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        Intent internal = new Intent(spec.actionBroadcast());
        for (Map.Entry<String, String> e : spec.extras().entrySet())
            internal.putExtra(e.getKey(), e.getValue());
        int notificationId = internal.hashCode();
        // Contract: the delivered intent names the posted notification so the
        // listener can cancel it. (The legacy demo posted under a different
        // hash, so its cancel silently missed — the seam posts under the id
        // it advertises.)
        internal.putExtra("notificationId", notificationId);
        atakFrontIntent.putExtra("internalIntent", internal);

        PendingIntent appIntent = PendingIntent.getActivity(hostContext,
                notificationId, atakFrontIntent,
                PendingIntentHelper.adaptFlags(0));

        Notification.Builder notificationBuilder;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            notificationBuilder = new Notification.Builder(hostContext);
        } else {
            // ATAK's default notification channel; plugins may post on it.
            notificationBuilder = new Notification.Builder(hostContext,
                    "com.atakmap.app.def");
        }

        notificationBuilder.setContentTitle(spec.title())
                .setSmallIcon(com.atakmap.app.R.drawable.ic_atak_launcher)
                .setOngoing(false)
                .setGroup(UUID.randomUUID().toString())
                .addAction(com.atakmap.app.R.drawable.phone_icon,
                        spec.actionLabel(), appIntent);

        NotificationManager nm = (NotificationManager) hostContext
                .getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(notificationId, notificationBuilder.build());
    }

    @Override
    public void cancelNotification(int notificationId) {
        // Host context via MapView — the same context notify() posted under;
        // the Controller stays ATAK-free and never holds host context itself.
        MapView mv = MapView.getMapView();
        NotificationManager nm = (NotificationManager) mv.getContext()
                .getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(notificationId);
    }

    /**
     * PARTIAL by design: posting a real notification would be user-visible,
     * so the probe only resolves NotificationUtil and exercises the
     * PendingIntentHelper flag adapter — the two ATAK types this impl links.
     */
    @Override
    public SelfCheckResult selfCheck() {
        return SelfChecks.attempt(id(), "notification plumbing threw", () -> {
            NotificationUtil util = NotificationUtil.getInstance();
            if (util == null)
                return SelfCheckResult.skipped(id(),
                        "NotificationUtil not ready");
            int flags = PendingIntentHelper.adaptFlags(0);
            return SelfCheckResult.partial(id(),
                    "NotificationUtil resolved; PendingIntentHelper.adaptFlags(0)="
                            + flags + "; nothing posted (would be user-visible)");
        });
    }
}
