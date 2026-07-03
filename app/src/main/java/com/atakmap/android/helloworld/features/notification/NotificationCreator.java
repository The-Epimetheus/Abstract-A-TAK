package com.atakmap.android.helloworld.features.notification;

import com.atakmap.android.helloworld.abstraction.Creator;

/**
 * Abstraction over ATAK's notification plumbing. Two distinct paths hide
 * behind it: {@code NotificationUtil} (ATAK's own notifier, stock ATAK icons
 * from host resources) and the ATAKActivity trampoline that lets a
 * notification action re-enter plugin code despite the plugin classloader
 * (a {@code PendingIntent} cannot target plugin classes directly). Both drag
 * host types/resources ({@code com.atakmap.app.R}) that must not leak into
 * {@code src/main}. Interface here; implementation in {@code src/atakShared}.
 */
public interface NotificationCreator extends Creator {

    /**
     * Post through ATAK's {@code NotificationUtil} under the given id with a
     * stock ATAK team icon; the text is used as title, ticker and body, and
     * the post vibrates. Re-posting under the same id replaces.
     */
    void postStatusNotification(int notificationId, String text);

    /**
     * Post a plain Android notification on ATAK's notification channel with
     * one action button. Tapping the action brings ATAK to the foreground and
     * re-delivers the spec's broadcast on ATAK's internal bus, with the spec's
     * extras plus an int extra {@code "notificationId"} naming the posted
     * notification — a listener can cancel it by that id.
     */
    void postActionNotification(ActionNotificationSpec spec);

    /**
     * Cancel a notification previously posted under {@code notificationId} —
     * e.g. the id an action notification's delivered intent carries. Cancel
     * must go through the HOST context's {@code NotificationManager} (the
     * notification was posted there), which is why this crosses the seam
     * instead of living in a Controller. No-op for an unknown or
     * already-cancelled id.
     */
    void cancelNotification(int notificationId);
}
