package restaurant.kitchen.orders;

import java.io.Serializable;

import restaurant.storage.Product;

public class Order implements Serializable {
    public final String username;
    public final String tableID;
    public final String comment;
    public final Product product;

    public Order(String username, String tableID, Product product,String comment) {
        this.username = username;
        this.tableID = tableID;
        this.product = product;
        this.comment=comment;
    }
}
