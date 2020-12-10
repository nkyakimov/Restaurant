package restaurant.service.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import restaurant.app.R;
import restaurant.service.adapters.ChosenProductAdapter;
import restaurant.service.handler.TableHandler;
import restaurant.load.ServerDown;
import restaurant.table.Table;

public class TableView extends AppCompatActivity {
    ListView productsView;
    Table table;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.table_view_activity);

        try {
            table = (Table) getIntent().getExtras().get("Table");
        } catch (Exception e) {
            return;
        }
        try {
            ((TextView) findViewById(R.id.tableNumberView)).setText("Table " + table.getId());
        } catch (Exception e) {

        }
        productsView = findViewById(R.id.productsView);
        ChosenProductAdapter chosenProductAdapter = new ChosenProductAdapter(getApplicationContext(), table.getProducts());
        productsView.setAdapter(chosenProductAdapter);
        /*
        productsView.setOnItemLongClickListener((parent, view, position, id) -> {
            ConstraintLayout c = view.findViewById(R.id.product_view);
            if (c.getSolidColor().equals(getResources().getColor(R.color.checked, getTheme()))) {
                c.setBackgroundResource(R.color.white);
            } else {
                c.setBackgroundResource(R.color.checked);
            }
            return true;
        });

         */
        FloatingActionButton addProduct = findViewById(R.id.sendOrderButton);
        addProduct.setOnClickListener(v -> {
            Intent newProduct = new Intent(getApplicationContext(), restaurant.service.add.newProduct.class);
            newProduct.putExtra("Id", table.getId());
            startActivityForResult(newProduct, 1);
        });
        FloatingActionButton bill = findViewById(R.id.bill);
        bill.setOnClickListener(v -> {
            TableHandler.bill(table.getId());
            finish();
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (!TableHandler.requestTables()) {
            Intent serverDown = new Intent(this, ServerDown.class);
            startActivity(serverDown);
            finish();
        } else {
            try {
                ChosenProductAdapter chosenProductAdapter = new ChosenProductAdapter(getApplicationContext(), TableHandler.getTable(table.getId()).getProducts());
                productsView.setAdapter(chosenProductAdapter);
            } catch (Exception e) {
                Intent serverDown = new Intent(this, ServerDown.class);
                startActivity(serverDown);
                finish();
            }
        }
    }

}
