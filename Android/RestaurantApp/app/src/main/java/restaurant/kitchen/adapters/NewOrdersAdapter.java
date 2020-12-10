package restaurant.kitchen.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import restaurant.app.R;
import restaurant.kitchen.orders.Order;

import static restaurant.kitchen.orders.OrdersHandler.newOrders;

public class NewOrdersAdapter extends BaseAdapter {

    private Context context;

    public NewOrdersAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return newOrders.size();
    }

    @Override
    public Order getItem(int position) {
        return newOrders.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        view = layoutInflater.inflate(R.layout.new_order_adapter, null);
        ((TextView) view.findViewById(R.id.productName)).setText(getItem(position).product.getName());
        ((TextView) view.findViewById(R.id.username)).setText(getItem(position).username);
        return view;
    }
}
