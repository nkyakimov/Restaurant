package restaurant.kitchen.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import restaurant.app.R;
import restaurant.kitchen.handler.KitchenHandler;
import restaurant.kitchen.mainActivity.Kitchen;
import restaurant.kitchen.orders.Order;
import static restaurant.kitchen.orders.OrdersHandler.currentOrders;
import static restaurant.kitchen.orders.OrdersHandler.rungOrders;

public class CurrentOrdersAdapter extends BaseAdapter {
    private Context context;
    private Kitchen kitchen;

    public CurrentOrdersAdapter(Context applicationContext, Kitchen kitchen) {
        this.context = applicationContext;
        this.kitchen = kitchen;
    }

    @Override
    public int getCount() {
        return currentOrders.size();
    }

    @Override
    public Order getItem(int position) {
        return currentOrders.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        view = layoutInflater.inflate(R.layout.current_order_layout, null);
        ((TextView) view.findViewById(R.id.productName)).setText(getItem(position).product.getName());
        ((TextView) view.findViewById(R.id.tableID)).append(getItem(position).tableID);
        ((TextView) view.findViewById(R.id.username)).append(getItem(position).username);
        if (getItem(position).comment.isEmpty()) {
            view.findViewById(R.id.comment).setVisibility(View.GONE);
        } else {
            ((TextView) view.findViewById(R.id.comment)).setText(getItem(position).comment);
        }
        view.findViewById(R.id.ring).setOnClickListener(v -> {
            try {
                Order order;
                synchronized (rungOrders) {
                    RungOrdersAdapter.addOrder((order = currentOrders.remove(position)));
                }
                KitchenHandler.ring(order.username, Integer.parseInt(order.tableID), order.product.getName() + " is ready");
                kitchen.updateCurrentView();
                kitchen.updateRungView();
            } catch (Exception ignored) {
                ignored.printStackTrace();
            }
        });
        view.findViewById(R.id.delete).setOnClickListener(v -> {
            try {
                currentOrders.remove(position);
                kitchen.updateCurrentView();
            } catch (Exception ignored) {

            }
        });
        return view;
    }
}
