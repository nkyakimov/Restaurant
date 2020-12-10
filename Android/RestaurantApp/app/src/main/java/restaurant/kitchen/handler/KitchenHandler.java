package restaurant.kitchen.handler;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import restaurant.IP_And_Type.IP;
import restaurant.kitchen.ProductDataBase.SimplePDB;
import restaurant.kitchen.mainActivity.Kitchen;
import restaurant.kitchen.orders.Order;
import restaurant.storage.Product;

public class KitchenHandler {
    private static Socket socket;
    private static Kitchen kitchen;
    private static ObjectOutputStream oos;
    private static ObjectInputStream ois;
    private static int port;

    public static void setup(int port, Kitchen kitchen) {
        KitchenHandler.port = port;
        KitchenHandler.kitchen = kitchen;
    }

    public static List<String> connect() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Callable<List<String>> callable = () -> {
            try {
                try {
                    socket.close();
                } catch (Exception ignored) {
                }
                socket = new Socket();
                socket.connect(new InetSocketAddress(IP.getIp(), port), 2000);
                oos = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
                oos.flush();
                ois = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
                return (List<String>) (ArrayList) ois.readObject();
            } catch (Exception e) {
                e.printStackTrace();
                socket.close();
                return null;
            }
        };
        Future<List<String>> result = executor.submit(callable);
        try {
            return result.get();
        } catch (ExecutionException | InterruptedException e) {
            return null;
        }
    }


    public static boolean submitAndContinueSetup(ArrayList<String> types) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Callable<Boolean> callable = () -> {
            oos.writeObject(types);
            oos.flush();
            try {
                try {
                    Map<Integer, Product> productMap = ((ConcurrentHashMap) ois.readObject());
                    SimplePDB.setup(productMap);
                    startListener();
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
        };
        Future<Boolean> result = executorService.submit(callable);
        try {
            return result.get();
        } catch (ExecutionException | InterruptedException e) {
            return false;
        }
    }

    public synchronized static boolean ring(String username, int table, String message) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Callable<Boolean> callable = () -> {
            try {
                oos.writeUTF("ring " + username + " " + table + " " + message);
                return true;
            } catch (Exception e) {
                try {
                    socket.close();
                } catch (Exception ignored) {
                }
                return false;
            } finally {
                oos.flush();
            }
        };
        Future<Boolean> result = executorService.submit(callable);
        try {
            return result.get();
        } catch (ExecutionException | InterruptedException e) {
            return false;
        }
    }

    private static void startListener() {
        Thread t = new Thread(() -> {
            try {
                while (true) {
                    String response = ois.readUTF();
                    if (response.startsWith("For ")) {
                        String[] data = new String[0];
                        try {
                            data = response.split(" +", 8);
                            kitchen.newOrder(new Order(data[1], data[3], SimplePDB.getProduct(Integer.parseInt(data[5])), data[7]));
                        } catch (Exception e) {
                            try {
                                kitchen.newOrder(new Order(data[1], data[3], SimplePDB.getProduct(Integer.parseInt(data[5])), ""));
                            } catch (Exception ignored) {
                            }
                        }
                    }
                }
            } catch (Exception e) {
                try {
                    socket.close();
                } catch (Exception ignored) {

                }
            }
        });
        t.start();
    }


    public static void close() {
        Runnable stop = () -> {
            try {
                socket.close();
                socket = null;
            } catch (Exception ignored) {

            }
        };
        stop.run();
    }
}
