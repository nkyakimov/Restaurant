package restaurant.service.handler;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import restaurant.app.R;
import restaurant.service.user.User;

public class NotificationHandler extends Service {
    private static int id = 0;
    private static Socket socket;
    private static ObjectOutputStream oos;
    private static ObjectInputStream ois;
    private static NotificationManager notificationManager;
    private static boolean retry = true;

    public static void stop(boolean stopForever) {
        new Thread(() -> {
            if (socket == null) {
                return;
            }
            if (stopForever) {
                retry = false;
            }
            try {
                oos.writeUTF("end");
                oos.flush();
            } catch (IOException ignored) {

            }
            try {
                socket.close();
            } catch (Exception ignored) {

            } finally {
                socket = null;
            }
        }).start();
    }

    private void listen() {
        Thread t = new Thread(() -> {
            try {
                while (true) {
                    String response = ois.readUTF();
                    if (response == null) {
                        throw new Exception();
                    }
                    if (response.startsWith("Message ")) {
                        try {
                            response = response.replaceAll(" +", " ");
                            String data = response.substring(8);
                            String number = data.substring(0, data.indexOf(" "));
                            showNotification(TableHandler.context, "Table " + number, data.substring(data.indexOf(" ") + 1), id++);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (Exception e) {
                if (socket != null) {
                    stop(false);
                    stopSelf();
                }
            }
        });
        t.start();
    }

    public void showNotification(Context context, String title, String message, int reqCode) {

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, title)
                .setSmallIcon(R.drawable.food)
                .setContentTitle(title)
                .setChannelId("Table")
                .setAutoCancel(false)
                .setContentText(message)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

        notificationManager.notify(reqCode, notificationBuilder.build());
    }

    public void setup() {
        try {
            notificationManager = (NotificationManager) TableHandler.context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                CharSequence name = "Table";
                int importance = NotificationManager.IMPORTANCE_HIGH;
                NotificationChannel mChannel = new NotificationChannel("Table", name, importance);
                notificationManager.createNotificationChannel(mChannel);
            }
        } catch (Exception ignored) {

        }
        connect();
    }

    protected void connect() {
        new Thread(() -> {
            try {
                socket = new Socket();
                Log.i("Notification Manager", "Trying to Connect");
                try {
                    socket.connect(new InetSocketAddress("192.168.1.245", 8191), 10000);
                } catch (Exception e) {
                    stopSelf();
                    return;
                }
                Log.i("Notification Manager", "Connected");
                oos = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
                oos.flush();
                ois = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
                User.loadUser(getFilesDir().getAbsolutePath() + "/" + getString(R.string.userFile));
                oos.writeUTF(User.getUsername());
                oos.writeUTF(User.getPassword());
                oos.flush();
                if (ois.readInt() < 0) {
                    if (socket != null) {
                        stop(true);
                        stopSelf();
                    }
                    return;
                }
                listen();
            } catch (Exception e) {
                if (socket != null) {
                    stop(true);
                    stopSelf();
                }
            }
        }).start();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate() {
        super.onCreate();
        retry = true;
        startForeground(5, new NotificationCompat.Builder(this, "running")
                .setSmallIcon(R.drawable.food)
                .setContentTitle("Restaurant Notifications Running")
                .setChannelId("Table")
                .setAutoCancel(true)
                .setSound(null)
                .setOnlyAlertOnce(true)
                .setPriority(-2)
                .build());
        setup();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("Notification Manager", "Stopping " + retry);
        if (retry) {
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction("restartNotifications");
            broadcastIntent.setClass(this, RestartNotifications.class);
            sendBroadcast(broadcastIntent);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
