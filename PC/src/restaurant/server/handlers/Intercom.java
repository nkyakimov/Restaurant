package restaurant.server.handlers;

import restaurant.accounts.ActiveAccount;
import restaurant.server.handlers.orders.UnsentOrder;
import restaurant.storage.Product;
import restaurant.storage.ProductDataBase;
import restaurant.table.Table;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public class Intercom {
    private final Map<ActiveAccount, List<TableHandler>> tableHandlers;
    private final Map<String, List<NotificationHandler>> notificationHandlers;
    private final List<DeviceHandler> deviceHandlers;
    private final ProductDataBase pdb;
    private final String restaurantName;
    private final String billPath;
    private final String backup;
    private final ReadWriteLock deviceHandlerLock = new ReentrantReadWriteLock();
    private final ReadWriteLock unsentMessagesLock = new ReentrantReadWriteLock();
    private final ReadWriteLock unsentOrdersLock = new ReentrantReadWriteLock();
    private Map<Integer, Table> allTables;
    private List<String> unsentMessages;
    private List<UnsentOrder> unsentOrders;

    public Intercom(ProductDataBase pdb, String restaurantName, String billPath, String backup) {
        tableHandlers = new ConcurrentHashMap<>();
        deviceHandlers = new ArrayList<>();
        unsentMessages = new ArrayList<>();
        unsentOrders = new ArrayList<>();
        allTables = new ConcurrentHashMap<>();
        this.pdb = pdb;
        this.restaurantName = restaurantName;
        this.billPath = billPath;
        this.backup = backup;
        notificationHandlers = new ConcurrentHashMap<>();
        restoreBackup();
    }


    private void backup() {
        try (var oos = new ObjectOutputStream(new FileOutputStream(new File(backup)))) {
            oos.writeObject(
                    new ArrayList(
                            tableHandlers.keySet().stream()
                                    .filter(activeAccount -> activeAccount.getTables().size() > 0)
                                    .collect(Collectors.toList())));
            try {
                unsentMessagesLock.readLock().lock();
                oos.writeObject(unsentMessages);
            } finally {
                unsentMessagesLock.readLock().unlock();
            }
            try {
                unsentOrdersLock.readLock().lock();
                oos.writeObject(unsentOrders);
            } finally {
                unsentOrdersLock.readLock().unlock();
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Cannot perform backup of Server");
        }
    }


    private void restoreBackup() {
        try (var ois = new ObjectInputStream(new FileInputStream(new File(backup)))) {
            List<ActiveAccount> accounts = (ArrayList) ois.readObject();
            if (accounts.size() > 0) {
                restoreAccounts(accounts);
            }
            allTables = new ConcurrentHashMap<>(20);
            tableHandlers.keySet().forEach(account -> allTables.putAll(account.getTables()));
            unsentMessages = (ArrayList) ois.readObject();
            unsentOrders = (ArrayList) ois.readObject();
        } catch (Exception e) {
            unsentOrders = new ArrayList<>();
            unsentMessages = new ArrayList<>();
        }
    }


    private void restoreAccounts(List<ActiveAccount> accounts) {
        for (ActiveAccount i : accounts) {
            tableHandlers.put(i, new ArrayList<>());
        }
    }


    protected boolean order(String username, int table, int productId, String comment) {
        if (addToTable(table, productId)) {
            boolean found = tryToOrder(new UnsentOrder(username, table, productId, comment));
            if (!found) {
                try {
                    unsentOrdersLock.writeLock().lock();
                    unsentOrders.add(new UnsentOrder(username, table, productId, comment));
                } finally {
                    unsentOrdersLock.writeLock().unlock();
                }
                backup();
            }
            return found;
        } else {
            return false;
        }
    }


    private void addToQue(String message) {
        try {
            unsentMessagesLock.writeLock().lock();
            unsentMessages.add(message);
        } finally {
            unsentMessagesLock.writeLock().unlock();
        }
        backup();
    }

    private void tryToResendMess() {
        try {
            unsentMessagesLock.writeLock().lock();
            unsentMessages.removeAll(unsentMessages.stream()
                    .filter(message -> ring(message, false)).collect(Collectors.toList()));
        } finally {
            unsentMessagesLock.writeLock().unlock();
        }
        backup();
    }


    protected Map<Integer, Table> getTables(String username, boolean admin) {
        if (admin) {
            return allTables;
        }
        for (ActiveAccount i : tableHandlers.keySet()) {
            if (i.getUsername().equals(username)) {
                return i.getTables();
            }
        }
        return null;
    }


    protected void addTableHandler(TableHandler tableHandler) {
        List<TableHandler> temp;
        if ((temp = tableHandlers.get(new ActiveAccount(tableHandler.getUsername()))) == null) {
            temp = new ArrayList<>();
            temp.add(tableHandler);
            tableHandlers.put(new ActiveAccount(tableHandler.getUsername()), temp);
        } else {
            synchronized (temp) {
                temp.add(tableHandler);
            }
        }
        update();
    }


    protected boolean addTable(Table table, String username) {
        if (allTables.get(table.getId()) != null) {
            return false;
        }
        allTables.put(table.getId(), table);
        for (ActiveAccount account : tableHandlers.keySet()) {
            if (account.getUsername().equals(username)) {
                account.addTable(table);
            }
        }
        backup();
        return true;
    }


    protected boolean removeFromTable(int tableId, int productId) {
        Table table;
        Product product;
        if ((table = allTables.get(tableId)) != null && (product = pdb.getProduct(productId)) != null) {
            table.removeProduct(product);
            backup();
            return true;
        }
        return false;
    }


    protected boolean ring(String message, boolean saveIfFail) { // nick(user) 5(table id) order is ready(text)
        try {
            List<NotificationHandler> notificationHandlerList;
            if ((notificationHandlerList =
                    notificationHandlers.get(
                            message.substring(0, message.indexOf(" ")).trim()))
                    != null) {
                notificationHandlerList.forEach(
                        temp -> temp.message("Message " + message.substring(message.indexOf(" ")).trim()));
                return true;
            } else {
                if (saveIfFail) {
                    addToQue(message);
                }
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }


    protected Table getTable(String username, int table, boolean admin) {
        try {
            if (admin) {
                return allTables.get(table);
            }
            return tableHandlers.keySet().stream()
                    .filter(i -> i.getUsername().equals(username))
                    .findFirst()
                    .orElseThrow()
                    .getTable(table);
        } catch (Exception e) {
            return null;
        }
    }


    private boolean addToTable(int tableId, int productId) {
        Table table;
        Product product;
        if ((table = allTables.get(tableId)) != null
                && (product = pdb.getProduct(productId)) != null) {
            table.addProduct(product);
            backup();
            return true;
        }
        return false;
    }


    protected void removeTableHandler(TableHandler tableHandler) {
        if (tableHandler == null) {
            return;
        }
        List<TableHandler> temp;
        if ((temp = tableHandlers.get(new ActiveAccount(tableHandler.getUsername().trim()))) != null) {
            synchronized (temp) {
                temp.remove(tableHandler);
            }
        }
        update();
    }


    protected void removeDeviceHandler(DeviceHandler deviceHandler) {
        try {
            deviceHandlerLock.writeLock().lock();
            deviceHandlers.remove(deviceHandler);
            update();
        } catch (Exception ignored) {

        } finally {
            deviceHandlerLock.writeLock().unlock();
        }
    }


    private int activeTH() {
        return tableHandlers.keySet().stream().mapToInt(i -> tableHandlers.get(i).size()).sum();
    }

    private int activeNH() {
        return notificationHandlers.keySet().stream().mapToInt(i -> notificationHandlers.get(i).size()).sum();
    }


    private int activeDH() {
        try {
            deviceHandlerLock.readLock().lock();
            return deviceHandlers.size();
        } finally {
            deviceHandlerLock.readLock().unlock();
        }
    }


    private void update() {
        System.out.println("Active TH: " + activeTH() + " Active NH: " + activeNH() + " Active DH: " + activeDH());
    }


    protected String getRestName() {
        return restaurantName;
    }


    protected String getBillPath() {
        return billPath;
    }


    protected void removeTable(int id) {
        allTables.remove(id);
        for (ActiveAccount account : tableHandlers.keySet()) {
            account.removeTable(id);
        }
        backup();
    }


    protected List<Product> getProducts(String substring) {
        return pdb.allMatch(substring);
    }


    protected void addDeviceHandler(DeviceHandler deviceHandler) {
        try {
            deviceHandlerLock.writeLock().lock();
            deviceHandlers.add(deviceHandler);
        } finally {
            deviceHandlerLock.writeLock().unlock();
        }
        update();
        tryToResendOrder();
    }


    private void tryToResendOrder() {
        try {
            unsentOrdersLock.writeLock().lock();
            unsentOrders.removeIf(this::tryToOrder);
        } finally {
            unsentOrdersLock.writeLock().unlock();
        }
        backup();
    }


    private boolean tryToOrder(UnsentOrder unsentOrder) {
        try {
            deviceHandlerLock.readLock().lock();
            boolean found = false;
            Product product;
            String type = (product = pdb.getProduct(unsentOrder.productID())).getType();
            for (DeviceHandler deviceHandler : deviceHandlers) {
                if (deviceHandler.matches(type)) {
                    deviceHandler.message(unsentOrder.username(), unsentOrder.tableID(), product, unsentOrder.comment());
                    found = true;
                }
            }
            return found;
        } finally {
            deviceHandlerLock.readLock().unlock();
        }
    }

    protected void addNotificationHandler(NotificationHandler temp) {
        List<NotificationHandler> list;
        if ((list = notificationHandlers.get(temp.getUsername())) == null) {
            notificationHandlers.put(temp.getUsername(), new ArrayList<>(java.util.Collections.singleton(temp)));
        } else {
            synchronized (list) {
                list.add(temp);
            }
        }
        update();
        tryToResendMess();
    }

    protected void removeNotificationHandler(NotificationHandler notificationHandler) {
        try {
            List<NotificationHandler> list;
            if ((list = notificationHandlers.get(notificationHandler.getUsername())) != null) {
                synchronized (list) {
                    list.remove(notificationHandler);
                }
            } else {
                return;
            }
            synchronized (list) {
                if (list.size() == 0) {
                    notificationHandlers.remove(notificationHandler.getUsername());
                }
            }
        } finally {
            update();
        }
    }

    protected void throwOut(String username) {
        try {
            tableHandlers.get(new ActiveAccount(username)).forEach(TableHandler::stop);
        } catch (NullPointerException ignored) {

        }
        try {
            notificationHandlers.get(username).forEach(NotificationHandler::stop);
        } catch (NullPointerException ignored) {

        }
    }
}