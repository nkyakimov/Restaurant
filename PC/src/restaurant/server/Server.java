package restaurant.server;

import restaurant.accounts.AccountDataBase;
import restaurant.exceptions.UserNotRegistered;
import restaurant.server.handlers.DeviceHandler;
import restaurant.server.handlers.Intercom;
import restaurant.server.handlers.NotificationHandler;
import restaurant.server.handlers.TableHandler;
import restaurant.storage.ProductDataBase;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private static final String RESTAURANT_NAME = "NM";
    private static final String PRODUCTS_DB = System.getProperty("user.dir") + File.separator + "products.json";
    private static final String ACCOUNTS_DB = System.getProperty("user.dir") + File.separator + "accounts.sg";
    private static final String BACKUP = System.getProperty("user.dir") + File.separator + "backup.sg";
    private static final String BILL_PATH = System.getProperty("user.dir") + File.separator + "bills";
    private static final int TABLE_HANDLER_PORT = 8190;
    private static final int DEVICE_HANDLER_PORT = 8189;
    private static final int NOTIFICATION_HANDLER_PORT = 8191;
    private final ProductDataBase pdb;
    private final Intercom intercom;

    public Server() {
        pdb = new ProductDataBase(PRODUCTS_DB);
        intercom = new Intercom(pdb, RESTAURANT_NAME, BILL_PATH, BACKUP);
        AccountDataBase.setup(ACCOUNTS_DB);
        //pdb.print();
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.start();
    }

    private void start() {
        startDeviceHandler();
        startTableHandler();
        startNotificationHandler();
    }

    private void startNotificationHandler() {
        new Thread(
                () -> {
                    ServerSocket serverSocket;
                    try {
                        serverSocket = new ServerSocket(Server.NOTIFICATION_HANDLER_PORT);
                    } catch (IOException e) {
                        System.err.println("Can't start Notification Handler");
                        return;
                    }
                    System.out.println("Started Notification Handler");
                    while (true) {
                        Socket socket = null;
                        try {
                            socket = serverSocket.accept();
                            listenForNotification(socket);
                        } catch (IOException e) {
                            try {
                                socket.close();
                            } catch (IOException | NullPointerException ioException) {
                                System.err.println("Error connecting to TH");
                            }
                        }
                    }
                }).start();
    }

    private void startTableHandler() {
        new Thread(
                () -> {
                    ServerSocket serverSocket;
                    try {
                        serverSocket = new ServerSocket(Server.TABLE_HANDLER_PORT);
                    } catch (IOException e) {
                        System.err.println("Can't start Table Handler");
                        return;
                    }
                    System.out.println("Started Table Handler");
                    while (true) {
                        Socket socket = null;
                        try {
                            socket = serverSocket.accept();
                            listenForTable(socket);
                        } catch (IOException e) {
                            try {
                                socket.close();
                            } catch (IOException | NullPointerException ioException) {
                                System.err.println("Error connecting to TH");
                            }
                        }
                    }
                }).start();
    }

    private void startDeviceHandler() {
        new Thread(
                () -> {
                    ServerSocket serverSocket;
                    try {
                        serverSocket = new ServerSocket(Server.DEVICE_HANDLER_PORT);
                    } catch (IOException e) {
                        System.err.println("Device Handler can't start");
                        return;
                    }
                    System.out.println("Started Device Handler");
                    while (true) {
                        Socket socket = null;
                        try {
                            socket = serverSocket.accept();
                            listenForDevice(socket);
                        } catch (IOException e) {
                            try {
                                socket.close();
                            } catch (IOException | NullPointerException ioException) {
                                System.err.println("Error connecting to DH");
                            }
                        }

                    }
                }).start();
    }

    private void listenForNotification(Socket socket) {
        new Thread(
                () -> {
                    try {
                        var oos = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
                        oos.flush();
                        var ois = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
                        String username = ois.readUTF();
                        String password = ois.readUTF();
                        if (AccountDataBase.verify(username, password) >= 0) {
                            oos.writeInt(1);
                            oos.flush();
                            new Thread(new NotificationHandler(intercom, oos, ois, socket, username)).start();
                        } else {
                            oos.writeInt(-1);
                            oos.flush();
                            throw new UserNotRegistered(
                                    "No such account:\nusername: " + username + " password: " + password);
                        }
                    } catch (UserNotRegistered | IOException e) {
                        System.err.println(e.getMessage());
                        try {
                            socket.close();
                        } catch (IOException ioException) {
                            System.err.println("Error closing socket of NH");
                        }
                    }
                }).start();
    }

    private void listenForTable(Socket socket) {
        new Thread(
                () -> {
                    try {
                        var oos = new ObjectOutputStream(socket.getOutputStream());
                        oos.flush();
                        var ois = new ObjectInputStream(socket.getInputStream());
                        String username = ois.readUTF();
                        String password = ois.readUTF();
                        int res;
                        if ((res = AccountDataBase.verify(username, password)) >= 0) {
                            oos.writeInt(res);
                            oos.flush();
                            new Thread(new TableHandler(intercom, socket, oos, ois, username, res == 1)).start();
                        } else {
                            oos.writeInt(-1);
                            oos.flush();
                            throw new UserNotRegistered(
                                    "No such account:\nusername: " + username + " password: " + password);
                        }
                    } catch (UserNotRegistered | IOException e) {
                        System.err.println(e.getMessage());
                        try {
                            socket.close();
                        } catch (IOException ioException) {
                            System.err.println("Error closing socket of TH");
                        }
                    }
                }).start();
    }

    private void listenForDevice(Socket socket) {
        new Thread(
                () -> {
                    try {
                        var oos = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
                        oos.flush();
                        var ois = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
                        oos.writeObject(pdb.getAllTypes());
                        oos.flush();
                        List<String> types;
                        try {
                            types = (ArrayList) ois.readObject();
                        } catch (Exception e) {
                            throw new IOException("Error receiving types");
                        }
                        pdb.sendToDevice(oos, types);
                        new Thread(new DeviceHandler(intercom, oos, ois, socket, types)).start();
                    } catch (IOException e) {
                        System.err.println("Error adding device DH");
                        try {
                            socket.close();
                        } catch (IOException ioException) {
                            System.err.println("Error closing socket ot DH");
                        }
                    }
                }).start();
    }
}
