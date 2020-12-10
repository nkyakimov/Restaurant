package restaurant.IP_And_Type;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class IP {
    private static String ip="";
    private static String ipFilePath="";
    public static void setIP(String ip) {
        IP.ip = ip;
        clear();
        updateIP(ipFilePath);
    }

    public static String getIp() {
        return ip;
    }
    public static boolean loadIP(String ipFilePath) {
        IP.ipFilePath = ipFilePath;
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File(ipFilePath)));
            IP.ip = (String) ois.readObject();
            clear();
            ois.close();
            updateIP(ipFilePath);
            return !ip.isEmpty();
        } catch (ClassNotFoundException | ClassCastException e) {
            System.err.println("Something is wrong with classes");
            return false;
        } catch (IOException e) {
            return false;
        }
    }

    private static void clear() {
        ip.replaceAll("[^0-9.]","");
    }

    public static void updateIP(String ipFilePath) {
        File ipFile = new File(ipFilePath);
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ipFile));
            oos.writeObject(IP.ip);
            oos.close();
        } catch (FileNotFoundException e) {
            try {
                if(ipFile.createNewFile()) {
                    updateIP(ipFilePath);
                }
            } catch (IOException ex) {
                setIP("");
            }
        } catch (IOException e) {
            setIP("");
        }
    }
}
