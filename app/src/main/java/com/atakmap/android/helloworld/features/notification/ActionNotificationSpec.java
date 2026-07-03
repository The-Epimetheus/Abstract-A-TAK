package com.atakmap.android.helloworld.features.notification;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Plugin-side description of an action notification: a plain Android
 * notification carrying one action button whose tap re-enters the plugin as
 * an internal ATAK broadcast (see
 * {@link NotificationCreator#postActionNotification}).
 */
public final class ActionNotificationSpec {

    private final String title;
    private final String actionLabel;
    private final String actionBroadcast;
    private final Map<String, String> extras;

    private ActionNotificationSpec(Builder b) {
        this.title = b.title;
        this.actionLabel = b.actionLabel;
        this.actionBroadcast = b.actionBroadcast;
        this.extras = Collections.unmodifiableMap(new LinkedHashMap<>(b.extras));
    }

    public String title() {
        return title;
    }

    public String actionLabel() {
        return actionLabel;
    }

    public String actionBroadcast() {
        return actionBroadcast;
    }

    public Map<String, String> extras() {
        return extras;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String title;
        private String actionLabel;
        private String actionBroadcast;
        private final Map<String, String> extras = new LinkedHashMap<>();

        /** The notification's content title. */
        public Builder title(String title) {
            this.title = title;
            return this;
        }

        /** Label shown on the notification's single action button. */
        public Builder actionLabel(String actionLabel) {
            this.actionLabel = actionLabel;
            return this;
        }

        /**
         * Intent action delivered on ATAK's internal broadcast bus when the
         * action button is tapped.
         */
        public Builder actionBroadcast(String action) {
            this.actionBroadcast = action;
            return this;
        }

        /** String extra carried on the delivered broadcast intent. */
        public Builder extra(String key, String value) {
            this.extras.put(key, value);
            return this;
        }

        public ActionNotificationSpec build() {
            if (title == null || actionLabel == null || actionBroadcast == null)
                throw new IllegalStateException(
                        "title, actionLabel and actionBroadcast are required");
            return new ActionNotificationSpec(this);
        }
    }
}
