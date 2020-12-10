package restaurant.service.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import restaurant.app.R;
import restaurant.storage.Product;

public class ProductAddAdapter extends BaseAdapter {
    private Context context;
    private List<Product> products;

    public ProductAddAdapter(Context context, List<Product> products) {
        this.context = context;
        if(products!=null)
            this.products = products;
        else
            this.products = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return products.size();
    }

    @Override
    public Product getItem(int position) {
        return products.get(position);
    }

    @Override
    public long getItemId(int position) {
        return products.get(position).getId();
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater.from(context));
        view = inflater.inflate(R.layout.product_option_to_add_adapter,null);
        TextView product = (TextView) view.findViewById(R.id.product_add_info);
        product.setText(getItem(position).toString());
        return view;
    }
}
