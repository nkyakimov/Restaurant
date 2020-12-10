package restaurant.storage;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import restaurant.exceptions.ProductAlreadyThere;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProductDataBase {
    private static final String SUCCESS = "ProductDB successfully loaded";
    private static final String ERROR_UPDATE = "Error updating productDB";
    private final String productDBLocation;
    private final List<String> allTypes;
    private Map<Integer, Product> products;

    public ProductDataBase(String productDBLocation) {
        allTypes = new ArrayList<>();
        this.productDBLocation = productDBLocation;
        if ((products = fromJSON()) == null) {
            throw new RuntimeException("Cannot load PDB");
        }
        calculateAllTypes();
        System.out.println(SUCCESS);
    }

    private void calculateAllTypes() {
        allTypes.addAll(products.values().stream().map(Product::getType).distinct().collect(Collectors.toList()));
    }

    public List<String> getAllTypes() {
        return allTypes;
    }

    public void sendToDevice(ObjectOutputStream oos, List<String> types) throws IOException {
        //oos.writeObject(products);
        oos.writeObject(products.values().stream().filter(product -> types.contains(product.getType())).collect(Collectors.toConcurrentMap(Product::getId,product->product)));
        oos.flush();
    }

    /**
     * @param data Expects Product as String like: "id,name,price,type"
     * @throws ProductAlreadyThere
     */
    public void addProduct(String data) throws ProductAlreadyThere {// id,name,price,type
        try {
            String[] productInformation = data.split(",");
            if (products.get(Integer.parseInt(productInformation[0])) != null) {
                throw new ProductAlreadyThere("Product with id " + Integer.parseInt(productInformation[0]) + " " +
                        "already exists");
            }
            products.put(
                    Integer.parseInt(productInformation[0]),
                    new Product(
                            Integer.parseInt(productInformation[0]),
                            productInformation[1].trim(),
                            Double.parseDouble(productInformation[2]),
                            productInformation[3].trim()));
            toJSON();
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException ignored) {
        }
    }

    public boolean removeProduct(int id) {
        Product toRemove = products.remove(id);
        if (toRemove == null) {
            return false;
        } else {
            toJSON();
            return true;
        }
    }

    private void toJSON() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            mapper.writeValue(new File(productDBLocation),
                    products.values());
        } catch (IOException e) {
            System.err.println(ERROR_UPDATE);
            System.err.println(e.getMessage());
        }
    }

    private Map fromJSON() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(new File(productDBLocation), new TypeReference<List<Product>>() {
            })
                    .stream().collect(Collectors.toConcurrentMap(Product::getId, product -> product));
        } catch (IOException e) {
            System.err.println(e.getMessage());
            return null;
        }
    }

    private boolean createDemoProductDB() {
        File file = new File(productDBLocation);
        try {
            if (file.createNewFile()) {
                products.clear();
                var oos = new ObjectOutputStream(new FileOutputStream(productDBLocation));
                oos.writeObject(products);
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            file.deleteOnExit();
            return false;
        }
    }

    public Product getProduct(Integer i) {
        return products.get(i);
    }

    public List<Product> allMatch(String data) {
        return products.values().stream()
                .filter(product -> product.match(data))
                .collect(Collectors.toList());
    }

    public void print() {
        products.forEach((k, v) -> System.out.println(k + " " + v.getFoodName()));
    }
}
