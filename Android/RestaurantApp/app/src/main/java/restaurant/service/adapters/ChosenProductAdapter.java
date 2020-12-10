package restaurant.service.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Map;

import restaurant.app.R;
import restaurant.storage.Product;

public class ChosenProductAdapter extends BaseAdapter {
    Map<Product, Integer> products;
    Context context;
    public ChosenProductAdapter(Context applicationContext, Map<Product, Integer> products) {
        this.context=applicationContext;
        this.products=products;
    }

    @Override
    public int getCount() {
        return products.size();
    }

    @Override
    public Product getItem(int position) {
        return new ArrayList<>(products.keySet()).get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater.from(context));
        view = inflater.inflate(R.layout.product_choosen_to_order_adapter,null);
        TextView product = (TextView) view.findViewById(R.id.product_info);
        TextView count = (TextView) view.findViewById(R.id.product_count);
        product.setText(getItem(position).toString());
        count.setText("x"+products.get(getItem(position)));
        return view;
    }
}
