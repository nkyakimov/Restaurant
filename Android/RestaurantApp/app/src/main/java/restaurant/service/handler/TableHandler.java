package restaurant.service.handler;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import restaurant.IP_And_Type.IP;
import restaurant.storage.Product;
import restaurant.table.Table;

public class TableHandler {
    private static final String TEST = "TEST";
    public static int servicePort;
    public static Context context;
    private static Map<Integer, Table> tables;
    private static ObjectOutputStream oos;
    private static ObjectInputStream ois;
    private static Socket socket = null;

    public static List<Table> getTables() {
        List<Table> sortedTables = new ArrayList<>(tables.values());
        sortedTables.sort(Comparator.comparingInt(Table::getId));
        return sortedTables;
    }

    public static Table getTable(int id) {
        return tables.get(id);
    }

    public synchronized static boolean requestTables() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Callable<Boolean> callable = () -> {
            Map<Integer, Table> backup = tables;
            oos.writeUTF("request MyTables");
            oos.flush();
            try {
                tables = (Map) ois.readObject();
                System.out.println(tables.size() + "-----------");
                return true;
            } catch (Exception e) {
                //e.printStackTrace();
                tables = backup;
                return false;
            }
        };
        Future<Boolean> result = executor.submit(callable);
        try {
            return result.get();
        } catch (ExecutionException | InterruptedException e) {
            return false;
        }
    }

    public static List<Product> requestProducts(String info) {
        if (info.contains("MyTables")) {
            return null;
        }
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Callable<List<Product>> callable = () -> {
            try {
                oos.writeUTF("request " + info);
                oos.flush();
                return (List<Product>) (ArrayList) ois.readObject();
            } catch (Exception e) {
                return null;
            }
        };
        Future<List<Product>> result = executor.submit(callable);
        try {
            return result.get();
        } catch (ExecutionException | InterruptedException e) {
            return null;
        }
    }

    public synchronized static Table requestTable(int id) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Callable<Table> callable = () -> {
            try {
                oos.writeUTF("request Table " + id);
                oos.flush();
                return (Table) ois.readObject();
            } catch (Exception e) {
                return null;
            }
        };
        Future<Table> result = executor.submit(callable);
        try {
            return result.get();
        } catch (ExecutionException | InterruptedException e) {
            return null;
        }
    }

    private synchronized static int execute(String command) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Callable<Integer> callable = () -> {
            try {
                oos.writeUTF(command);
                oos.flush();
                oos.flush();
                return (ois.readBoolean()) ? 1 : 0;
            } catch (Exception e) {
                e.printStackTrace();
                return -1;
            }
        };
        Future<Integer> result = executor.submit(callable);
        try {
            return result.get();
        } catch (ExecutionException | InterruptedException e) {
            return -1;
        }

    }

    public synchronized static void stop() {
        new Thread(() -> {
            try {
                oos.writeUTF("end");
                oos.flush();
                oos.flush();
                socket.close();
            } catch (Exception ignored) {

            }
        }).start();
    }

    public static void setup(int servicePort) {
        TableHandler.servicePort = servicePort;
        TableHandler.tables = new HashMap<>();
    }

    private static synchronized void listen() {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            System.out.println(service.service.getClassName());
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(new Intent(context, NotificationHandler.class));
        } else {
            context.startService(new Intent(context, NotificationHandler.class));
        }
    }

    public static int connect(String username, String password, Context applicationContext, boolean startNotifications) {
        context = applicationContext;
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Callable<Integer> callable = () -> {
            try {
                if (test()) {
                    return requestTables() ? 1 : 0;
                }
                try {
                    socket.close();
                } catch (Exception e) {
                    //e.printStackTrace();
                }
                socket = new Socket();
                socket.connect(new InetSocketAddress(IP.getIp(), servicePort), 2000);
                oos = new ObjectOutputStream(socket.getOutputStream());
                oos.flush();
                ois = new ObjectInputStream(socket.getInputStream());
                Log.i("Socket", "Object Streams are opened");
                oos.writeUTF(username);
                oos.writeUTF(password);
                oos.flush();
                if(ois.readInt()<0){
                    stop();
                    return 0;
                }
                if (startNotifications) {
                    listen();
                }
                if (requestTables()) {
                    return 1;
                } else {
                    NotificationHandler.stop(true);

                    return 0;
                }
            } catch (Exception e) {
                stop();
                return -1;
            }
        };
        Future<Integer> result = executor.submit(callable);
        try {
            return result.get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return -1;
        }

    }

    private static boolean test() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Callable<Boolean> callable = () -> {
            try {
                oos.writeUTF(TEST);
                oos.flush();
                return ois.readUTF() != null;
            } catch (IOException e) {
                return false;
            }
        };
        Future<Boolean> result = executor.submit(callable);
        try {
            return result.get();
        } catch (ExecutionException | InterruptedException e) {
            return false;
        }
    }

    public static synchronized int order(int tableID, int productID, String comment) {
        return execute("order " + tableID + " " + productID + " " + comment);
    }

    public static synchronized int deleteTable(int id) {
        return execute("delete " + id);
    }

    public static synchronized int changePassword(String username, String oldPassword, String newPassword) {
        return execute("change " + username + " " + oldPassword + " " + newPassword);
    }

    public static synchronized int createTable(int id) {
        return execute("create " + id);
    }

    public static synchronized int bill(int id) {
        return execute("bill " + id);
    }
}
