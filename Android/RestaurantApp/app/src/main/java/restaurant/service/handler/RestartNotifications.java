package restaurant.service.handler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class RestartNotifications extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("Notification Manager","Restarting Services");
        try {
            Thread.sleep(6000);
        } catch (InterruptedException e) {
            Log.i("Notification Manager","Failed to Wait");
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(new Intent(context, NotificationHandler.class));
        } else {
            context.startService(new Intent(context, NotificationHandler.class));
        }
    }
}
