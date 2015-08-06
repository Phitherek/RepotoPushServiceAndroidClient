package me.phitherek.repotopushserviceandroidclient;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.pusher.client.Pusher;
import com.pusher.client.PusherOptions;
import com.pusher.client.channel.Channel;
import com.pusher.client.channel.PrivateChannelEventListener;
import com.pusher.client.connection.ConnectionEventListener;
import com.pusher.client.connection.ConnectionState;
import com.pusher.client.connection.ConnectionStateChange;

import java.util.HashMap;
import java.util.Hashtable;

public class RepotoPushServiceClientService extends Service {

    private final String pusherKey = "102bf6729c96532e9daa";
    private static Gson gson;
    private static Pusher pusher;
    private static Boolean running = false;
    private static Context appContext = null;

    public RepotoPushServiceClientService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        gson = new Gson();
        appContext = getApplicationContext();
        Context ctx = getApplicationContext();
        SharedPreferences sharedPrefs = ctx.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        String defaultValue = "";
        String currentToken = sharedPrefs.getString(getString(R.string.shared_preferences_current_token_key), defaultValue);
        final String currentUsername = sharedPrefs.getString(getString(R.string.shared_preferences_current_username_key), defaultValue);
        if (!currentToken.equals("")) {
            CustomHttpAuthorizer authorizer = new CustomHttpAuthorizer("https://repotopushauth.deira.phitherek.me/endpoint", ctx);
            HashMap<String, String> additionalParams = new HashMap<String, String>();
            additionalParams.put("token", currentToken);
            authorizer.setQueryStringParameters(additionalParams);
            PusherOptions options = new PusherOptions();
            options.setAuthorizer(authorizer);
            options.setEncrypted(true);
            pusher = new Pusher(pusherKey, options);
            pusher.connect(new ConnectionEventListener() {
                @Override
                public void onConnectionStateChange(ConnectionStateChange change) {
                    Log.wtf("connection_state", "Repoto Push Service Client Service connection state: " + change.getPreviousState() + " -> " + change.getCurrentState());
                }

                @Override
                public void onError(String message, String code, Exception e) {
                    Toast toast = Toast.makeText(getApplicationContext(), "Repoto Push Service Client Service connection problem!", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }, ConnectionState.ALL);
            Channel channel = pusher.subscribePrivate("private-" + currentUsername);
            channel.bind("my_event", new PrivateChannelEventListener() {
                @Override
                public void onAuthenticationFailure(String message, Exception e) {
                    Log.wtf("auth_failure", String.format("Repoto Push Service Client Service authentication failure due to [%s], exception was [%s]",
                            message, e));
                }

                @Override
                public void onSubscriptionSucceeded(String channelName) {
                    Log.wtf("subscription_success", "Repoto Push Service Client Service subscription succeeded");
                }

                @Override
                public void onEvent(String channelName, String eventName, String message) {
                    try {
                        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
                        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "RepotoPushServiceClientServiceWakelock");
                        wakeLock.acquire();
                        Hashtable<String, String> memodata = gson.fromJson(message, (new Hashtable<String, String>()).getClass());
                        String memomsg = memodata.get("message");
                        Integer notifId = NotificationIdDispenser.nextId();
                        Intent cancelIntent = new Intent();
                        cancelIntent.setAction(HelperService.ACTION_NOTIFICATION_CANCELLED);
                        cancelIntent.putExtra(HelperService.EXTRA_NOTIFICATION_ID, notifId);
                        cancelIntent.setClass(getApplicationContext(), HelperService.class);
                        PendingIntent cancelPI = PendingIntent.getService(getApplicationContext(), notifId, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                        Intent showIntent = new Intent(getApplicationContext(), ShowActivity.class);
                        showIntent.setAction(ShowActivity.ACTION_SHOW_CONTENT);
                        showIntent.putExtra(ShowActivity.EXTRA_PUSHMEMO_CONTENT, memomsg);
                        PendingIntent showPI = PendingIntent.getActivity(getApplicationContext(), notifId, showIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                        NotificationManager NM;
                        NM = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        NotificationCompat.Builder notifyBuilder = new NotificationCompat.Builder(getApplicationContext());
                        notifyBuilder.setSmallIcon(R.drawable.notification_icon);
                        notifyBuilder.setContentTitle(getString(R.string.received_new_pushmemo_for) + " " + currentUsername);
                        notifyBuilder.setContentText(memomsg);
                        notifyBuilder.setTicker(getString(R.string.received_new_pushmemo_for) + " " + currentUsername + ": " + memomsg);
                        notifyBuilder.setLights(0xffffff00, 1000, 4000);
                        notifyBuilder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
                        notifyBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(memomsg));
                        notifyBuilder.setContentIntent(showPI);
                        notifyBuilder.setDeleteIntent(cancelPI);
                        notifyBuilder.setWhen(System.currentTimeMillis());
                        Notification notify = notifyBuilder.build();
                        NM.notify(notifId, notify);
                        wakeLock.release();
                    } catch (Exception e) {
                        running = false;
                        e.printStackTrace();
                    }
                }
            });
            running = true;

            return START_STICKY;
        } else {
            Toast toast = Toast.makeText(ctx, "Repoto Push Service Client Service failed to start!", Toast.LENGTH_SHORT);
            toast.show();

            return START_FLAG_RETRY;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        pusher.disconnect();
        running = false;
    }

    public static Boolean getRunning() {
        running = false;
        if(appContext != null) {
            ActivityManager manager = (ActivityManager) appContext.getSystemService(Context.ACTIVITY_SERVICE);
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (RepotoPushServiceClientService.class.getName().equals(service.service.getClassName())) {
                    running = true;
                }
            }
        }
        return running;
    }
}
