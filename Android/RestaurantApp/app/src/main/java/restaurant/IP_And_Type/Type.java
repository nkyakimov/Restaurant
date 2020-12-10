package restaurant.IP_And_Type;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Type {
    private static String type;
    private static String typeFilePath;

    public static String getType() {
        return type;
    }

    public static void setType(String type) {
        Type.type = type;
        updateType(typeFilePath);
    }

    public static boolean loadType(String typeFilePath) {
        Type.typeFilePath = typeFilePath;
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File(typeFilePath)));
            Type.type = (String) ois.readObject();
            ois.close();
            updateType(typeFilePath);
            return !type.isEmpty();
        } catch (ClassNotFoundException | ClassCastException e) {
            System.err.println("Something is wrong with classes");
            return false;
        } catch (IOException e) {
            return false;
        }
    }

    public static void updateType(String typeFilePath) {
        if (typeFilePath != null) {
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(typeFilePath))) {
                oos.writeObject(type);
            } catch (Exception e) {
                setType("");
            }
        }
    }

    public static void resetType() {
        type = "";
        updateType(typeFilePath);
    }
}
