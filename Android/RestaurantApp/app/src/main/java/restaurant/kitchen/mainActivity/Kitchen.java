package restaurant.kitchen.mainActivity;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import java.util.ArrayList;
import java.util.List;

import restaurant.IP_And_Type.Type;
import restaurant.app.R;
import restaurant.kitchen.adapters.CurrentOrdersAdapter;
import restaurant.kitchen.adapters.NewOrdersAdapter;
import restaurant.kitchen.adapters.RungOrdersAdapter;
import restaurant.kitchen.handler.KitchenHandler;
import restaurant.kitchen.orders.Order;
import restaurant.kitchen.orders.OrdersHandler;
import restaurant.load.Loader;
import restaurant.load.ServerDown;

import static restaurant.kitchen.orders.OrdersHandler.currentOrders;
import static restaurant.kitchen.orders.OrdersHandler.newOrders;
import static restaurant.kitchen.orders.OrdersHandler.rungOrders;

public class Kitchen extends AppCompatActivity {
    private ListView currentOrdersView;
    private ListView rungOrdersView;
    private GridView newOrdersView;
    private CurrentOrdersAdapter currentOrdersAdapter;
    private NewOrdersAdapter newOrdersAdapter;
    private RungOrdersAdapter rungOrdersAdapter;
    private NotificationCompat.Builder notificationCompat;
    private NotificationManager notificationManager;

    public synchronized void newOrder(Order order) {
        newOrders.add(order);
        OrdersHandler.update();
        runOnUiThread(this::updateNewView);
        playSound();
    }

    public synchronized void updateCurrentView() {
        runOnUiThread(() -> currentOrdersAdapter.notifyDataSetChanged());
        OrdersHandler.update();
    }

    public synchronized void updateNewView() {
        runOnUiThread(() -> newOrdersAdapter.notifyDataSetChanged());
        OrdersHandler.update();
    }

    public synchronized void updateRungView() {
        runOnUiThread(() -> rungOrdersAdapter.notifyDataSetChanged());
        OrdersHandler.update();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.kitchen_main_activity);
        setupNotification();
        KitchenHandler.setup(8189, this);
        OrdersHandler.setup(getFilesDir().getAbsolutePath() + "/" + getString(R.string.ordersFile));
        setupViews();
        surveyTypes();
    }

    private void setupViews() {
        rungOrdersView = findViewById(R.id.rungOrdersView);
        currentOrdersView = findViewById(R.id.currentOrdersList);
        newOrdersView = findViewById(R.id.waitingOrdersGrid);
        currentOrdersAdapter = new CurrentOrdersAdapter(getApplicationContext(), this);
        currentOrdersView.setAdapter(currentOrdersAdapter);
        newOrdersAdapter = new NewOrdersAdapter(getApplicationContext());
        newOrdersView.setAdapter(newOrdersAdapter);
        newOrdersView.setOnItemClickListener((parent, view, position, id) -> {
            currentOrders.add(newOrders.remove(position));
            updateNewView();
            updateCurrentView();
        });
        rungOrdersAdapter = new RungOrdersAdapter(getApplicationContext(), this,rungOrders);
        rungOrdersView.setAdapter(rungOrdersAdapter);
    }

    private void setupNotification() {
        notificationManager = ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(new NotificationChannel("kitchen", "kitchen", NotificationManager.IMPORTANCE_HIGH));
        }
        notificationCompat = new NotificationCompat.Builder(this, "kitchen")
                .setSmallIcon(R.drawable.food)
                .setContentTitle("New Order")
                .setChannelId("kitchen")
                .setAutoCancel(true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
    }

    private void playSound() {
        notificationManager.notify(5,
                notificationCompat.build());
    }

    private void surveyTypes() {
        List<String> types = KitchenHandler.connect();
        if (types == null) {
            KitchenHandler.close();
            serverDown();
            return;
        }
        List<Integer> selected_indexes = new ArrayList<>();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.kitchen_type_choose_popup, null);
        ListView listView = view.findViewById(R.id.listTypes);
        ArrayAdapter<String> typesAdapter = new ArrayAdapter<>(this, R.layout.kitchen_type_choose_adapter, types);
        listView.setAdapter(typesAdapter);

        listView.setOnItemClickListener((parent, view1, position, id) -> {
            if (selected_indexes.contains(position)) {
                selected_indexes.remove(Integer.valueOf(position));
                view1.setBackgroundColor(getColor(R.color.white));
            } else {
                selected_indexes.add(position);
                view1.setBackgroundColor(getColor(R.color.colorAccent));
            }
        });

        builder.setView(view).setTitle("Type Choose").setNegativeButton("Cancel", (dialog, which) -> {
            Type.resetType();
            KitchenHandler.close();
            Intent loader = new Intent(this, Loader.class);
            startActivity(loader);
        }).setPositiveButton("OK", (dialog, which) -> {
                    ArrayList<String> selectedTypes = new ArrayList<>();
                    for (int index : selected_indexes) {
                        selectedTypes.add(types.get(index));
                    }
                    if(!KitchenHandler.submitAndContinueSetup(selectedTypes)){
                        KitchenHandler.close();
                        serverDown();
                    }
                }
        ).show();
    }

    private void serverDown() {
        Intent serverDown = new Intent(this, ServerDown.class);
        startActivity(serverDown);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        KitchenHandler.close();
    }
}
