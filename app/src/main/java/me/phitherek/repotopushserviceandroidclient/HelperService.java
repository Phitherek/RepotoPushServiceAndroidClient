package me.phitherek.repotopushserviceandroidclient;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 */
public class HelperService extends IntentService {
    public static final String ACTION_NOTIFICATION_CANCELLED = "me.phitherek.repotopushserviceandroidclient.action.NOTIFICATION_CANCELLED";
    public static final String EXTRA_NOTIFICATION_ID = "me.phitherek.repotopushserviceandroidclient.extra.NOTIFICATION_ID";

    /**
     * Starts this service to perform action NotificationCancelled with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startNotificationCancelled(Context context, Integer notifId) {
        Intent intent = new Intent(context, HelperService.class);
        intent.setAction(ACTION_NOTIFICATION_CANCELLED);
        intent.putExtra(EXTRA_NOTIFICATION_ID, notifId);
        context.startService(intent);
    }

    public HelperService() {
        super("HelperService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_NOTIFICATION_CANCELLED.equals(action)) {
                final Integer notifId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1);
                handleActionNotificationCancelled(notifId);
            }
        }
    }

    /**
     * Handle action NotificationCancelled in the provided background thread with the provided
     * parameters.
     */
    private void handleActionNotificationCancelled(Integer notifId) {
        NotificationIdDispenser.removeActive(notifId);
    }
}
