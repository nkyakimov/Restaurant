package restaurant.service.user;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import restaurant.IP_And_Type.Type;
import restaurant.app.R;
import restaurant.load.Loader;

public class LoginActivity extends AppCompatActivity {
    private EditText username;
    private EditText password;
    private TextView error;
    private Button add;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.change_user_activity);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        username.setText(User.getUsername());
        add = findViewById(R.id.addUserButton);
        error = findViewById(R.id.errorUser);
        add.setOnClickListener(v -> {
            String newUsername = username.getText().toString();
            String newPassword = password.getText().toString();
            if(!newUsername.isEmpty()&&!newPassword.isEmpty()) {
                User.saveUser(newUsername,newPassword);
                startActivity(new Intent(this, Loader.class));
                finish();
            } else {
                error.setText(R.string.user_error);
            }
        });
        Button changeType = findViewById(R.id.changeTypeButton);
        changeType.setOnClickListener(v -> {
            Type.resetType();
            Intent reload = new Intent(getApplicationContext(),Loader.class);
            startActivity(reload);
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
