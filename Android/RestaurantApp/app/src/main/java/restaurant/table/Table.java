package restaurant.table;

import restaurant.storage.Product;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Table implements Serializable {
    private final int id;
    private static final long serialVersionUID = 2506;
    private final Map<Product, Integer> tableProducts;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public Table(int id) {
        this.id = id;
        tableProducts = new HashMap<>();
    }

    public int getId() {
        return id;
    }

    public Map<Product,Integer> getProducts() {
        return tableProducts;
    }
}
