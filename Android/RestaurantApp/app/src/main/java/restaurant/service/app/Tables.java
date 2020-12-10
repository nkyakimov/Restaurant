package restaurant.service.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.GridView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import restaurant.IP_And_Type.Type;
import restaurant.app.R;
import restaurant.load.Loader;
import restaurant.load.ServerDown;
import restaurant.service.adapters.TableIconAdapter;
import restaurant.service.add.newTable;
import restaurant.service.change.ChangePasswordActivity;
import restaurant.service.handler.NotificationHandler;
import restaurant.service.handler.TableHandler;
import restaurant.service.orders.UnsentOrders;
import restaurant.service.user.User;
import restaurant.table.Table;

import static restaurant.service.handler.TableHandler.connect;
import static restaurant.service.handler.TableHandler.createTable;
import static restaurant.service.handler.TableHandler.deleteTable;
import static restaurant.service.handler.TableHandler.getTables;
import static restaurant.service.handler.TableHandler.requestTables;

public class Tables extends AppCompatActivity implements newTable.TableListener, NavigationView.OnNavigationItemSelectedListener {
    private static volatile boolean doNotStop = false;
    private GridView tablesGrid;
    private DrawerLayout drawerLayout;
    private List<Table> tables;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        doNotStop = false;
        setContentView(R.layout.service_main_activity);
        tablesGrid = findViewById(R.id.tables_grid);
        drawerLayout = findViewById(R.id.drawer);
        setUpTables(getTables());
        UnsentOrders.resendOrders();
        final SwipeRefreshLayout pullToRefresh = findViewById(R.id.refresh);
        pullToRefresh.setOnRefreshListener(() -> {
            reload();
            pullToRefresh.setRefreshing(false);
        });

        NavigationView navigationView = findViewById(R.id.navigation);
        navigationView.setNavigationItemSelectedListener(this);
        View userField = navigationView.getHeaderView(0);
        TextView user = userField.findViewById(R.id.userField);
        user.setText(User.getUsername().toUpperCase());
        user.setTextSize(25);


        FloatingActionButton plus = findViewById(R.id.sendOrderButton);
        plus.setOnClickListener(v -> {
            newTable newTable = new newTable();
            newTable.show(getSupportFragmentManager(), "new Table dialog");
        });
        tablesGrid.setOnItemClickListener((parent, view, position, id) -> {
            reload();
            Intent table = new Intent(getApplicationContext(), TableView.class);
            table.putExtra("Table", TableHandler.requestTable(tables.get(position).getId()));
            startActivity(table);
        });
        tablesGrid.setOnItemLongClickListener((parent, view, position, id) -> {
            int code;
            if ((code = deleteTable(tables.get(position).getId())) == 1) {
                reload();
            } else if (code == 0) {
                Snackbar snackbar = Snackbar
                        .make(view, "You are not admin", Snackbar.LENGTH_LONG);
                snackbar.show();
            } else {
                serverDown();
            }
            return true;
        });
        tablesGrid.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                FloatingActionButton fab = findViewById(R.id.sendOrderButton);
                if (firstVisibleItem < 3) {
                    fab.show();
                } else {
                    fab.hide();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        reload();
    }

    private void reload() {
        if (!requestTables()) {
            if (connect(User.getUsername(), User.getPassword(), getApplicationContext(), true) == -1) {
                serverDown();
            } else {
                setUpTables(getTables());
            }
        } else {
            setUpTables(getTables());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!doNotStop) {
            TableHandler.stop();
        }
    }

    private void setUpTables(List<Table> tables) {
        runOnUiThread(() -> {
            this.tables = tables;
            TableIconAdapter tableIconAdapter = new TableIconAdapter(getApplicationContext(), tables);
            tablesGrid.setAdapter(tableIconAdapter);
        });
    }


    private void serverDown() {
        Intent serverDown = new Intent(this, ServerDown.class);
        TableHandler.stop();
        NotificationHandler.stop(true);
        stopService(new Intent(this, NotificationHandler.class));
        startActivity(serverDown);
        finish();
    }

    @Override
    public void applyNumber(String number) {
        int value;
        int tableID;
        try {
            tableID = Integer.parseInt(number);
        } catch (Exception e) {
            return;
        }
        value = createTable(tableID);
        if (value == -1) {
            serverDown();
        } else if (value == 1) {
            reload();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.error_creating_table)
                    .setTitle(R.string.error).setPositiveButton("Leave", (dialog, which) -> finish()).show();
        }
    }

    public void restartApp() {
        TableHandler.stop();
        NotificationHandler.stop(true);
        stopService(new Intent(this, NotificationHandler.class));
        Intent begin = new Intent(getApplicationContext(), Loader.class);
        doNotStop=false;
        startActivity(begin);
        finish();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        switch (menuItem.getItemId()) {
            case R.id.change_type: {
                Type.resetType();
                User.logout();
                restartApp();
                break;
            }
            case R.id.logout: {
                User.logout();
                restartApp();
                break;
            }
            case R.id.change_password: {
                NotificationHandler.stop(true);
                stopService(new Intent(this, NotificationHandler.class));
                doNotStop = true;
                Intent changePassword = new Intent(this, ChangePasswordActivity.class);
                startActivity(changePassword);
                finish();
                break;
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START);

        return true;
    }
}