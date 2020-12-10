package restaurant.service.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import restaurant.app.R;
import restaurant.table.Table;

public class TableIconAdapter extends BaseAdapter {
    private final List<Table> tables;
    private final Context context;

    public TableIconAdapter(Context context, List<Table> tables) {
        if (tables != null) {
            this.tables = tables;
        }
        else
            this.tables = new ArrayList<>();
        this.context = context;
    }

    @Override
    public int getCount() {
        return tables.size();
    }

    @Override
    public Table getItem(int position) {
        return tables.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflter = (LayoutInflater.from(context));
        view = inflter.inflate(R.layout.table_icon, null); // inflate the layout
        TextView id = (TextView) view.findViewById(R.id.table_id);
        id.setText(String.valueOf(tables.get(position).getId()));
        return view;
    }

}
