package restaurant.IP_And_Type;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import restaurant.app.R;
import restaurant.load.Loader;
import restaurant.service.user.User;

public class newTypeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_type);
        try {
            User.logout();
        }catch (NullPointerException e) {

        }
        ImageView table = findViewById(R.id.tableImage);
        table.setOnClickListener(v -> {
            Type.setType("table");
            Intent loader = new Intent(getApplicationContext(), Loader.class);
            startActivity(loader);
            finish();
        });
        ImageView kitchen = findViewById(R.id.kitchenImage);
        kitchen.setOnClickListener(v -> {
            Type.setType("kitchen");
            Intent loader = new Intent(getApplicationContext(), Loader.class);
            startActivity(loader);
            finish();
        });
    }
}
