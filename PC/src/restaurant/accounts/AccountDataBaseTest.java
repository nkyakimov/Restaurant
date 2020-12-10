package restaurant.accounts;

import org.junit.jupiter.api.BeforeAll;
import restaurant.exceptions.UserAlreadyThereException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccountDataBaseTest {
    private static final String FILE = "demo.txt";

    @BeforeAll
    static void setup() throws IOException {
        File file = new File(FILE);
        if (file.exists() || file.createNewFile()) {
            new FileOutputStream(file).close();
            AccountDataBase.setup(FILE);
            assertDoesNotThrow(() -> AccountDataBase.addUser("nick", "2506", false));
            assertDoesNotThrow(() -> AccountDataBase.addUser("admin", "admin", true));
        }
    }

    @org.junit.jupiter.api.Test
    void changePassword() {
        assertTrue(AccountDataBase.changePassword("nick", "2506", "5566"));
        assertFalse(AccountDataBase.changePassword("admin", "2506", "5566"));
        assertEquals(AccountDataBase.verify("nick", "5566"), 0);
        assertEquals(-1, AccountDataBase.verify("nick", "2506"));
        assertEquals(AccountDataBase.verify("admin", "admin"), 1);
        assertDoesNotThrow(() -> AccountDataBase.changePassword(null, "2222", ""));
        assertDoesNotThrow(() -> AccountDataBase.changePassword("nic", "2222", ""));
    }

    @org.junit.jupiter.api.Test
    void addUser() {
        assertThrows(UserAlreadyThereException.class, () -> AccountDataBase.addUser("nick", "5566", false));
    }

    @org.junit.jupiter.api.Test
    void verify() {
        assertEquals(AccountDataBase.verify("nick", "2506"), 0);
        assertEquals(AccountDataBase.verify("admin", "admin"), 1);
        assertEquals(-1, AccountDataBase.verify("admin", "ceco"));
    }
}