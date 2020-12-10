package restaurant.storage;

import java.io.Serializable;
import java.util.Objects;

public class Product implements Serializable {
    public static final long serialVersionUID = -2226761754150408384L;
    private final Integer id;
    private final String foodName;
    private final Double price;
    private final String type;

    public Product(Integer id, String foodName, Double price, String type) {
        this.id = id;
        this.foodName = foodName;
        this.price = price;
        this.type = type;
    }

    public Product() {
        this(null,null,null,null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Product product = (Product) o;
        return Objects.equals(id, product.id) &&
                Objects.equals(foodName, product.foodName) &&
                Objects.equals(price, product.price) &&
                Objects.equals(type, product.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public String getFoodName() {
        return foodName;
    }

    public int getId() {
        return id;
    }

    public double getPrice() {
        return price;
    }

    public String getType() {
        return type;
    }

    public String toString() {
        return foodName + " " + price;
    }

    public boolean match(String data) {
        final String nameAndID = id + " " + foodName.toLowerCase() + " " + type.toLowerCase();
        for (String word : data.split(" +")) {
            if (!nameAndID.contains(word.toLowerCase())) {
                return false;
            }
        }
        return true;
    }
}
