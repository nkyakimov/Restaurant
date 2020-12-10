package restaurant.load;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import restaurant.IP_And_Type.IP;
import restaurant.IP_And_Type.Type;
import restaurant.IP_And_Type.newTypeActivity;
import restaurant.app.R;
import restaurant.kitchen.mainActivity.Kitchen;
import restaurant.service.app.Tables;
import restaurant.service.handler.TableHandler;
import restaurant.service.orders.UnsentOrders;
import restaurant.service.user.LoginActivity;
import restaurant.service.user.User;

public class Loader extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loader_activity);
        TableHandler.setup(8190);
        UnsentOrders.setup(getFilesDir().getAbsolutePath() + "/" + getString(R.string.unsentOrders));
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!IP.loadIP(getFilesDir().getAbsolutePath() + "/" + getString(R.string.ipFile))) {
            addNewIP();
        } else {
            if (Type.loadType(getFilesDir().getAbsolutePath() + "/" + getString(R.string.programType))) {
                System.out.println("Type; ----------" + Type.getType());
                if (Type.getType().equals("table")) {
                    if (User.loadUser(getFilesDir().getAbsolutePath() + "/" + getString(R.string.userFile))) {
                        int result;
                        if ((result = TableHandler.connect(User.getUsername(), User.getPassword(), getApplicationContext(), true)) == 0) {
                            addNewUser();
                        } else if (result == 1) {
                            Intent tables = new Intent(this, Tables.class);
                            startActivity(tables);
                            finish();
                        } else {
                            Intent serverDown = new Intent(this, ServerDown.class);
                            startActivity(serverDown);
                            finish();
                        }
                    } else {
                        addNewUser();
                    }
                } else if (Type.getType().equals("kitchen")) {
                    Intent kitchen = new Intent(this, Kitchen.class);
                    startActivity(kitchen);
                    finish();
                } else {
                    chooseNewType();
                }
            } else {
                chooseNewType();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void chooseNewType() {
        Intent newType = new Intent(this, newTypeActivity.class);
        startActivity(newType);
        finish();
    }

    private void addNewUser() {
        Intent newUser = new Intent(this, LoginActivity.class);
        TableHandler.stop();
        startActivity(newUser);
        finish();
    }

    private void addNewIP() {
        Intent newIPActivity = new Intent(this, restaurant.IP_And_Type.newIPActivity.class);
        startActivityForResult(newIPActivity, 0);
        TableHandler.stop();
        finish();
    }

}
