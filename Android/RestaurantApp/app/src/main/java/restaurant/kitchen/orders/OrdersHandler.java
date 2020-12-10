package restaurant.kitchen.orders;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class OrdersHandler {
    public static List<Order> currentOrders;
    public static List<Order> newOrders;
    public static List<Order> rungOrders;
    private static String filePath;

    public static void setup(String filePath) {
        OrdersHandler.filePath=filePath;
        load();
    }

    public static void update() {
        try{
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath));
            oos.writeObject(currentOrders);
            oos.writeObject(newOrders);
            oos.writeObject(rungOrders);
        }catch (FileNotFoundException fileNotFound) {
            try {
                File file = new File(filePath);
                if (file.createNewFile()) {
                    update();
                }
            }catch (Exception e) {

            }
        } catch (Exception e) {

        }
    }

    public static void load() {
        try{
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath));
            currentOrders = (ArrayList) ois.readObject();
            newOrders = (ArrayList) ois.readObject();
            rungOrders = (ArrayList) ois.readObject();
            ois.close();
        } catch (Exception e) {
            currentOrders = new ArrayList<>();
            newOrders = new ArrayList<>();
            rungOrders = new ArrayList<>();
        }
    }
}
