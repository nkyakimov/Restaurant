package restaurant.service.change;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import restaurant.app.R;
import restaurant.service.app.Tables;
import restaurant.service.handler.TableHandler;
import restaurant.service.user.User;
import restaurant.load.Loader;
import restaurant.load.ServerDown;

public class ChangePasswordActivity extends AppCompatActivity {
    TextView error;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.change_password_activity);
        TextView user = findViewById(R.id.user);
        user.append(User.getUsername());
        EditText oldPass = findViewById(R.id.oldPass);
        EditText newPass = findViewById(R.id.newPass);
        error = findViewById(R.id.errorChangePass);
        Button cancel = findViewById(R.id.cancelChangePass);
        Button ok = findViewById(R.id.okChangePass);
        cancel.setOnClickListener(v -> {
            Intent loader = new Intent(this, Loader.class);
            startActivity(loader);
            finish();
        });

        ok.setOnClickListener(v -> {
            String oldPassString = oldPass.getText().toString();
            String newPassString = newPass.getText().toString();
            if (oldPassString.contains(" ") || newPassString.contains(" ")) {
                showError();
            } else {
                int res;
                if((res=TableHandler.changePassword(User.getUsername(),oldPassString,newPassString))==1) {
                    User.saveUser(User.getUsername(),newPassString);
                    TableHandler.stop();
                    Intent loader = new Intent(this, Loader.class);
                    startActivity(loader);
                    finish();
                } else if(res==0){
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(R.string.password_not_changed)
                            .setTitle(R.string.error)
                            .show();
                } else {
                    Intent serverDown = new Intent(this, ServerDown.class);
                    startActivity(serverDown);
                    finish();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent loader = new Intent(this, Loader.class);
        startActivity(loader);
        finish();
    }

    private void showError() {
        error.setText(getResources().getText(R.string.user_error));
    }

}
