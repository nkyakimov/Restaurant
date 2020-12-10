package restaurant.kitchen.ProductDataBase;

import java.util.HashMap;
import java.util.Map;

import restaurant.storage.Product;

public class SimplePDB {

    private static Map<Integer, Product> pdb;

    public static void setup(Map<Integer, Product> pdb) {
        SimplePDB.pdb = pdb;
    }

    public static Product getProduct(Integer i) {
        return pdb.get(i);
    }
}
