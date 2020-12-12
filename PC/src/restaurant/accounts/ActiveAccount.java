package restaurant.accounts;

import restaurant.table.Table;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class ActiveAccount implements Serializable {
    private final String username;
    private final Map<Integer, Table> tables;

    public ActiveAccount(String username) {
        this.username = username;
        this.tables = new ConcurrentHashMap<>();
    }

    public String getUsername() {
        return username;
    }

    public Table getTable(int id) {
        return tables.get(id);
    }

    public void removeTable(int id) {
        tables.remove(id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return username.equals(((ActiveAccount) o).username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }

    public Map<Integer, Table> getTables() {
        return tables;
    }

    public void addTable(Table table) {
        tables.put(table.getId(), table);
    }
}
