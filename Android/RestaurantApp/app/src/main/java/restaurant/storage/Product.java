package restaurant.storage;

import java.io.Serializable;
import java.util.Objects;

public class Product implements Serializable {
    private final Integer id;
    private final String foodName;
    private final Double price;
    private final String type;
    public static final long serialVersionUID = -2226761754150408384L;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return id.equals(product.id) &&
                foodName.equals(product.foodName) &&
                price.equals(product.price) &&
                type.equals(product.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public Product(int id, String foodName, double price, String type) {
        this.id = id;
        this.foodName = foodName;
        this.price = price;
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String toString() {
        return id + "   " + foodName + " - " + type;
    }

    public String getName() {
        return foodName;
    }
}
