package restaurant.accounts;

import restaurant.exceptions.CantCreateFile;
import restaurant.exceptions.DataBaseCreationException;
import restaurant.exceptions.UserAlreadyThereException;
import restaurant.accounts.pair.Pair;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AccountDataBase {
    private static String location;
    private static Map<String, Pair> usernameAndPasswords;

    public static void setup(String accountDataBaseLocation) throws DataBaseCreationException {
        location = accountDataBaseLocation;
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File(location)));
            usernameAndPasswords = (ConcurrentHashMap<String, Pair>) ois.readObject();
        } catch (FileNotFoundException e) {
            System.out.println("AccountDB loaded with new file");
            usernameAndPasswords = new ConcurrentHashMap<>();
        } catch (InvalidClassException invalidClassException) {
            usernameAndPasswords = new ConcurrentHashMap<>();
            throw new DataBaseCreationException("Invalid Class Cast");
        } catch (ClassNotFoundException e) {
            usernameAndPasswords = new ConcurrentHashMap<>();
            throw new DataBaseCreationException("Class Cast not found");
        } catch (IOException e) {
            usernameAndPasswords = new ConcurrentHashMap<>();
        }
    }

    public static void print() {
        for (Map.Entry<String, Pair> i : usernameAndPasswords.entrySet()) {
            System.out.println(i.getKey() + " " + i.getValue());
        }
    }

    public static boolean changePassword(String username, String oldPassword, String newPassword) {
        try {
            if (!usernameAndPasswords.get(username).verify(oldPassword)) {
                return false;
            }
            usernameAndPasswords.replace(username, new Pair(newPassword, usernameAndPasswords.get(username).admin));
            update();
            return true;
        } catch (NullPointerException e) {
            return false;
        }
    }

    public static void addUser(String username, String password, boolean admin) throws UserAlreadyThereException {
        if (usernameAndPasswords.get(username) != null) {
            throw new UserAlreadyThereException("User with name " + username + " already registered");
        } else {
            usernameAndPasswords.put(username, new Pair(password, admin));
            update();
        }
    }

    public static int verify(String username, String password) {
        try {
            Pair pair;
            if ((pair = usernameAndPasswords.get(username)) != null && pair.verify(password)) {
                return pair.admin ? 1 : 0;
            }
            return -1;
        } catch (Exception e) {
            return -1;
        }
    }

    public synchronized static void update() {
        try {
            var oos = new ObjectOutputStream(new FileOutputStream(new File(location)));
            oos.writeObject(usernameAndPasswords);
            oos.close();
        } catch (FileNotFoundException e) {
            try {
                if (createNewADB(location)) {
                    update();
                }
            } catch (CantCreateFile c) {
                throw new RuntimeException("Error when creating accountDb");
            }
        } catch (IOException e) {
            throw new RuntimeException("Error when updating accountDb");
        }
    }

    private static boolean createNewADB(String location) throws CantCreateFile {
        File newABD = new File(location);
        try {
            if (!newABD.createNewFile()) {
                throw new CantCreateFile("Cannot create file for accountDB");
            } else {
                return true;
            }
        } catch (IOException e) {
            return false;
        } finally {
            usernameAndPasswords = new ConcurrentHashMap<>();
        }
    }

    public static void clear() {
        usernameAndPasswords.clear();
    }
}
