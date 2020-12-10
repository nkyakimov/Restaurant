package restaurant.server.handlers;

import restaurant.options.Options;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class NotificationHandler implements Runnable {
    private final Intercom intercom;
    private final Socket socket;
    private final String username;
    private final ObjectOutputStream oos;
    private final ObjectInputStream ois;

    public NotificationHandler(Intercom intercom, ObjectOutputStream oos, ObjectInputStream ois, Socket socket,
                               String username) {
        this.intercom = intercom;
        this.socket = socket;
        this.username = username;
        this.oos = oos;
        this.ois = ois;
        intercom.addNotificationHandler(this);
    }

    protected String getUsername() {
        return username;
    }

    protected void message(String message){
        try {
            oos.writeUTF(message);
            oos.flush();
        } catch (Exception e) {
            intercom.removeNotificationHandler(this);
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
                if (Options.getOption(response).equals(Options.Quit)) {
                    return;
                }
            }
        } catch (Exception e) {
            stop();
        } finally {
            intercom.removeNotificationHandler(this);
        }
    }

    protected void stop() {
        try {
            socket.close();
        } catch (IOException ignored) {

        }
    }
}
