package restaurant.table;

import restaurant.storage.Product;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Table implements Serializable {
    private static final long serialVersionUID = 2506;
    private final int id;
    private final Map<Product, Integer> tableProducts;


    public Table(int id) {
        this.id = id;
        tableProducts = new ConcurrentHashMap<>();
    }

    public int getId() {
        return id;
    }

    public void addProduct(Product i) {

        try {
            tableProducts.replace(i, tableProducts.get(i) + 1);
        } catch (Exception e) {
            tableProducts.put(i, 1);
        }
    }

    public void removeProduct(Product i) {
        int count;
        try {
            if ((count = tableProducts.get(i)) > 1) {
                tableProducts.replace(i, count - 1);
            } else {
                tableProducts.remove(i);
            }
        } catch (NullPointerException e) {
            System.err.println("Cannot remove this product");
        }
    }

    public double getTotal() {
        return tableProducts.keySet().stream()
                .mapToDouble(i -> i.getPrice() * tableProducts.get(i))
                .sum();
    }

    public void bill(String restaurantName, String filepath) {
        DecimalFormat df = new DecimalFormat("#.##");
        String billPath = filepath + File.separator
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy  HH_mm"));
        int i = 1;
        while (true) {
            String newPath = billPath + "_(" + i + ").txt";
            File bill = new File(newPath);
            try {
                if (bill.createNewFile()) {
                    FileWriter fw = new FileWriter(newPath, false);
                    fw.append("-- ").append(restaurantName).append(" --").append(System.lineSeparator());
                    tableProducts.keySet().forEach(product -> printProduct(product, fw));
                    fw.append("\t\t Total: ").append(df.format(getTotal())).append(System.lineSeparator());
                    fw.append("---------------------");
                    fw.close();
                    break;
                }
            } catch (IOException e) {
                System.err.println("Cannot create bill");
                return;
            }
            i++;
        }
    }

    private void printProduct(Product product, FileWriter fw) {
        try {
            fw.append(product.toString())
                    .append(" x")
                    .append(String.valueOf((int) tableProducts.get(product)))
                    .append(System.lineSeparator())
                    .append("\t\t\t")
                    .append(String.valueOf(product.getPrice() * tableProducts.get(product)))
                    .append(System.lineSeparator());
        } catch (IOException ignored) {
        }
    }
}
