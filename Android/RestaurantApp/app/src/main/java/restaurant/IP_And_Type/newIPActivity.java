package restaurant.IP_And_Type;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import restaurant.app.R;
import restaurant.load.Loader;
import restaurant.service.user.User;

public class newIPActivity extends AppCompatActivity {
    private EditText ip;
    private Button addIp;
    private  TextView error;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.change_ip_activity);
        User.logout();
        Type.resetType();
        ip = findViewById(R.id.ipEditText);
        error = findViewById(R.id.errorIP);
        addIp = findViewById(R.id.ipConfButton);
            ip.setText(IP.getIp());

        addIp.setOnClickListener(v -> {
            String newIP = ip.getText().toString();
            int count = 0;
            for(char i : newIP.toCharArray()) {
                if(i == '.')
                    count++;
            }
            if(count!=3) {
               error.setText(R.string.wrongIPFormat);
            } else {
                IP.setIP(newIP);
                startActivity(new Intent(this, Loader.class));
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, Loader.class));
        finish();
    }
}
