package restaurant.load;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import restaurant.app.R;
import restaurant.IP_And_Type.newIPActivity;
import restaurant.service.handler.NotificationHandler;
import restaurant.service.user.LoginActivity;
import restaurant.service.handler.TableHandler;
import restaurant.service.user.User;

public class ServerDown extends AppCompatActivity {
    ProgressBar pb;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.server_down_activity);
        NotificationHandler.stop(true);
        stopService(new Intent(this, NotificationHandler.class));
        Button retry = findViewById(R.id.retryButton);
        Button change = findViewById(R.id.changeIP);
        pb = findViewById(R.id.progressBar3);
        pb.setVisibility(View.INVISIBLE);
        retry.setOnClickListener(v -> {
            reconnect();
        });
        change.setOnClickListener(v -> {
            Intent changeIP = new Intent(this, newIPActivity.class);
            startActivity(changeIP);
            finish();
        });
    }

    private void reconnect() {
        Intent loader = new Intent(getApplicationContext(),Loader.class);
        startActivity(loader);
        finish();
    }


    public void onBackPressed() {
        finish();
    }
}
