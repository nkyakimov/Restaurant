package restaurant.server.handlers.orders;

import java.io.Serializable;

public record UnsentOrder(String username, Integer tableID, Integer productID, String comment) implements Serializable {

}
