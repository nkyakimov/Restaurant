package restaurant.accounts.pair;

import java.io.Serializable;
import java.util.Arrays;

public class Pair implements Serializable {
    public char[] password;
    public Boolean admin;

    public Pair(String password, Boolean admin) {
        this.password = password.toCharArray();
        this.admin = admin;
    }


    @Override
    public String toString() {
        return "password = " + new String(password) + " admin = " + admin;
    }

    public boolean verify(String password) {
        return Arrays.equals(this.password, password.toCharArray());
    }
}
