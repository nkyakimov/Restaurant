package restaurant.server.handlers;

import restaurant.options.Options;
import restaurant.storage.Product;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

public class DeviceHandler implements Runnable {
    private final Intercom intercom;
    private final Socket socket;
    private final List<String> types;
    private final ObjectOutputStream oos;
    private final ObjectInputStream ois;

    public DeviceHandler(Intercom intercom, ObjectOutputStream oos, ObjectInputStream ois, Socket socket,
                         List<String> types) {
        this.intercom = intercom;
        this.socket = socket;
        this.types = types;
        this.oos = oos;
        this.ois = ois;
        intercom.addDeviceHandler(this);
    }

    protected boolean matches(String type) {
        return types.stream().anyMatch(t -> t.toLowerCase().equals(type.toLowerCase()));
    }

    protected synchronized void message(String user, int table, Product product, String comment) {
        try {
            if (comment.isEmpty()) {
                oos.writeUTF("For " + user + " table " + table + " product " + product.getId());
            } else {
                oos.writeUTF(
                        "For "
                                + user
                                + " table "
                                + table
                                + " product "
                                + product.getId()
                                + " comment "
                                + comment);
            }
            oos.flush();
        } catch (IOException ioException) {
            intercom.removeDeviceHandler(this);
            try {
                socket.close();
            } catch (Exception ignored) {

            }
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                String response = ois.readUTF();
                if (Options.getOption(response).equals(Options.Ring)) {
                    intercom.ring(response.substring(5).trim(), true);
                }
            }
        } catch (IOException | NullPointerException e) {
            intercom.removeDeviceHandler(this);
            try {
                socket.close();
            } catch (Exception ignored) {

            }
        }
    }
}
