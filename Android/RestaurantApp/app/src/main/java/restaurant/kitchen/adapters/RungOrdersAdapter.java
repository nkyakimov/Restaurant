package restaurant.kitchen.adapters;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import restaurant.app.R;
import restaurant.kitchen.handler.KitchenHandler;
import restaurant.kitchen.mainActivity.Kitchen;
import restaurant.kitchen.orders.Order;

import static restaurant.kitchen.orders.OrdersHandler.currentOrders;
//import static restaurant.kitchen.orders.OrdersHandler.orders;

public class RungOrdersAdapter extends BaseAdapter {
    private static List<Order> orders;
    private static List<Double> times;
    private final List<View> views = new ArrayList<>();
    private Handler handler = new Handler();
    private Runnable updater = new Runnable() {
        @Override
        public void run() {
            try {
                for (int i = 0; i < views.size(); i++) {
                    timeUpdate(i, views.get(i));
                }
            } catch (ArrayIndexOutOfBoundsException ignored) {

            }
        }
    };

    private Context context;
    private Kitchen kitchen;

    public RungOrdersAdapter(Context applicationContext, Kitchen kitchen, List<Order> orders) {
        this.context = applicationContext;
        this.kitchen = kitchen;
        RungOrdersAdapter.orders = orders;
        times = new ArrayList<>();
        times.addAll(Stream.generate(() -> 3.00).limit(orders.size()).collect(Collectors.toList()));
        Timer tmr = new Timer();
        tmr.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(updater);
            }
        }, 1000, 1000);
    }

    public static void addOrder(Order order) {
        synchronized (orders) {
            orders.add(order);
        }
        synchronized (times) {
            times.add(3.00);
        }
    }

    @Override
    public int getCount() {
        synchronized (orders) {
            return orders.size();
        }
    }

    @Override
    public Order getItem(int position) {
        synchronized (orders) {
            return orders.get(position);
        }
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (view == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            view = layoutInflater.inflate(R.layout.rung_order_adapter, null);
            views.add(position, view);
            ((TextView) view.findViewById(R.id.productName)).setText(getItem(position).product.getName());
            ((TextView) view.findViewById(R.id.tableID)).append(getItem(position).tableID);
            ((TextView) view.findViewById(R.id.username)).append(getItem(position).username);
            if (getItem(position).comment.isEmpty()) {
                view.findViewById(R.id.comment).setVisibility(View.GONE);
            } else {
                ((TextView) view.findViewById(R.id.comment)).setText(getItem(position).comment);
            }

            view.findViewById(R.id.reorder).setOnClickListener(v -> {
                try {
                    synchronized (orders) {
                        currentOrders.add(orders.remove(position));
                    }
                    synchronized (times) {
                        times.remove(position);
                        views.remove(position);
                    }
                    kitchen.updateRungView();
                    kitchen.updateCurrentView();
                } catch (Exception ignored) {

                }
            });
            view.findViewById(R.id.ring).setOnClickListener(v -> {
                try {
                    Order order = getItem(position);
                    KitchenHandler.ring(order.username, Integer.parseInt(order.tableID), order.product.getName() + " is ready");
                } catch (Exception ignored) {

                }
            });
            view.findViewById(R.id.delete).setOnClickListener(v -> {
                try {
                    synchronized (times) {
                        times.remove(position);
                        views.remove(position);
                    }
                    synchronized (orders) {
                        orders.remove(position);
                    }
                    kitchen.updateRungView();
                } catch (Exception ignored) {

                }
            });
        }
        timeUpdate(position, view);
        return view;
    }

    private void timeUpdate(int position, View view) {
        TextView time = view.findViewById(R.id.timer);
        synchronized (times) {
            time.setText(String.format("%.2f", times.get(position) - 0.01));
            times.add(position, times.get(position) - 0.01);
        }
    }
}
