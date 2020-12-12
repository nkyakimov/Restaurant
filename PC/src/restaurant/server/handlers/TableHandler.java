package restaurant.server.handlers;

import restaurant.accounts.AccountDataBase;
import restaurant.options.Options;
import restaurant.table.Table;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Map;

public class TableHandler implements Runnable {
    private final Intercom intercom;
    private final String username;
    private final Socket socket;
    private final boolean admin;
    private final ObjectOutputStream oos;
    private final ObjectInputStream ois;
    private Map<Integer, Table> tables;
    private boolean running = true;

    public TableHandler(
            Intercom intercom,
            Socket socket,
            ObjectOutputStream oos,
            ObjectInputStream ois,
            String username,
            boolean admin) {
        this.intercom = intercom;
        this.username = username;
        this.socket = socket;
        this.oos = oos;
        this.ois = ois;
        this.admin = admin;
        intercom.addTableHandler(this);
        tables = intercom.getTables(username, admin);
    }

    protected String getUsername() {
        return username;
    }

    @Override
    public void run() {
        try {
            while (running) {
                String response = ois.readUTF();
                try {
                    switch (Options.getOption(response)) {
                        case CreateTable -> createTable(response.substring(7));
                        case RemoveFromTable -> removeFromTable(response.substring(7));
                        case Bill -> bill(response.substring(5));
                        case Order -> order(response.substring(6));
                        case DeleteFromTable -> deleteTable(response.substring(7));
                        case RequestSomething -> executeRequest(response.substring(8).trim());
                        case ChangePassword -> changePassword(response.substring(7));
                        case Test -> test();
                        case Quit -> throw new IOException("Ending");
                    }
                } catch (ArrayIndexOutOfBoundsException ignored) {
                    //oos.writeUTF(FALSE);
                }
            }
        } catch (Exception e) {
            stop();
        }
    }

    private void test() throws IOException {
        oos.writeUTF("TEST");
        oos.flush();
    }

    protected synchronized void stop() {
        running = false;
        intercom.removeTableHandler(this);
        try {
            socket.close();
        } catch (IOException ioException) {
            throw new RuntimeException("Cannot close TableHandler Socket");
        }
    }

    private void changePassword(String substring) throws IOException {
        try {
            String[] data = substring.split(" +", 3);
            if (AccountDataBase.changePassword(data[0], data[1], data[2])) {
                oos.writeBoolean(true);
                oos.flush();
                intercom.throwOut(data[0]);
            } else {
                oos.writeBoolean(false);
            }
        } catch (Exception e) {
            oos.writeBoolean(false);
        } finally {
            oos.flush();
        }
    }

    private void executeRequest(String request) throws IOException {
        try {
            if (request.equals("MyTables")) {
                oos.writeObject(tables);
            } else if (request.startsWith("Table")) {
                try {
                    oos.writeObject(
                            intercom.getTable(username, Integer.parseInt(request.substring(6)), admin));
                } catch (Exception e) {
                    oos.writeObject(null);
                }
            } else {
                oos.writeObject(intercom.getProducts(request));
            }
        } finally {
            oos.flush();
            oos.reset();
        }
    }

    private void deleteTable(String substring) throws IOException {
        try {
            if (!admin) {
                oos.writeBoolean(false);
            } else {
                try {
                    intercom.removeTable(Integer.parseInt(substring.trim()));
                    oos.writeBoolean(true);
                } catch (NumberFormatException ignored) {
                    oos.writeBoolean(false);
                }
            }
        } finally {
            oos.flush();
        }
    }

    private void order(String info) throws IOException {
        try {
            String[] data = info.split(" ", 3);
            if (tables.get(Integer.parseInt(data[0])) != null) {
                try {
                    oos.writeBoolean(
                            intercom.order(username, Integer.parseInt(data[0]), Integer.parseInt(data[1]), data[2]));
                } catch (ArrayIndexOutOfBoundsException e) {
                    oos.writeBoolean(
                            intercom.order(username, Integer.parseInt(data[0]), Integer.parseInt(data[1]), ""));
                }
            } else {
                oos.writeBoolean(false);
            }
        } finally {
            oos.flush();
        }

    }

    private void createTable(String info) throws IOException {
        int id = Integer.parseInt(info);
        Table temp = new Table(id);
        oos.writeBoolean(intercom.addTable(temp, getUsername()));
        oos.flush();
    }

    private void bill(String tableId) throws IOException {
        try {
            int id = Integer.parseInt(tableId);
            tables.get(id).bill(intercom.getRestName(), intercom.getBillPath());
            tables.remove(id);
            intercom.removeTable(id);
            oos.writeBoolean(true);
        } catch (NumberFormatException e) {
            oos.writeBoolean(false);
            throw new RuntimeException(tableId + " is not a valid table number");
        } finally {
            oos.flush();
        }
    }

    private void removeFromTable(String info) throws IOException {
        try {
            if (!admin) {
                oos.writeBoolean(false);
            } else {
                String[] ids = info.split(" ");
                if (tables.get(Integer.parseInt(ids[0])) != null) {
                    oos.writeBoolean(intercom.removeFromTable(Integer.parseInt(ids[0]), Integer.parseInt(ids[1])));
                } else {
                    oos.writeBoolean(false);
                }
            }
        } finally {
            oos.flush();
        }
    }
}
