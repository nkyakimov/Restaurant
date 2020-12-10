package restaurant.service.orders;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import restaurant.service.handler.TableHandler;

class UnsentOrder implements Serializable {
    public int tableID;
    public int productID;
    public String comment;

    public UnsentOrder(int tableID, int productID, String comment) {
        this.tableID = tableID;
        this.productID = productID;
        this.comment = comment;
    }
}

public class UnsentOrders {
    private static List<UnsentOrder> unsentOrders = new ArrayList<>();
    private static String filePath;

    public static void setup(String filePath) {
        UnsentOrders.filePath = filePath;
        load();
    }

    private static void save() {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(filePath)));
            oos.writeObject(unsentOrders);
            oos.close();
        } catch (FileNotFoundException fileNotFound) {
            File newFile = new File(filePath);
            try {
                if (newFile.createNewFile()) {
                    save();
                }
            } catch (IOException ignored) {

            }
        } catch (Exception ignored) {

        }
    }

    private static void load() {
        try{
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File(filePath)));
            unsentOrders = (List) ois.readObject();
            ois.close();
        } catch (Exception e) {
            unsentOrders = new ArrayList<>();
        }
    }


    public static void addOrderToSendLater(int tableID, int productID, String comment) {
        unsentOrders.add(new UnsentOrder(tableID, productID, comment));
        save();
    }

    public static void resendOrders() {
        unsentOrders.removeAll(unsentOrders.stream()
                .filter(order -> TableHandler.order(order.tableID, order.productID, order.comment) != -1)
                .collect(Collectors.toList()));
        save();
    }
}
