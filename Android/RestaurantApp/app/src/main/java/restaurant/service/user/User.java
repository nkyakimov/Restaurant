package restaurant.service.user;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class User {
    private static String username = "";
    private static String password = "";
    private static String userFilePath;

    public static String getUsername() {
        return username;
    }

    public static String getPassword() {
        return password;
    }

    public static void saveUser(String username, String password) {
        System.out.println(username + " " + password);
        User.username = username;
        User.password = password;
        updateUser(userFilePath);
    }

    public static void logout() {
        User.password = "";
        try {
            updateUser(userFilePath);
        }catch (Exception e) {
            
        }
    }
    public static boolean loadUser(String userFilePath) {
        User.userFilePath = userFilePath;
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File(userFilePath)));
            User.username = (String) ois.readObject();
            User.password = (String) ois.readObject();
            ois.close();
            return !password.isEmpty();
        } catch (ClassNotFoundException | ClassCastException e) {
            System.err.println("Something is wrong with classes");
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void updateUser(String userFilePath) {
        File userFile = new File(userFilePath);
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(userFile));
            oos.writeObject(username);
            oos.writeObject(password);
            oos.close();
        } catch (FileNotFoundException e) {
            try {
                if (userFile.createNewFile()) {
                    updateUser(userFilePath);
                }
            } catch (IOException ex) {
                saveUser("", "");
            }
        } catch (IOException e) {
            saveUser("", "");
        }
    }

}
