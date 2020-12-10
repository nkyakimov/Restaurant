package restaurant.service.add;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import restaurant.app.R;

public class newTable extends AppCompatDialogFragment {
    private EditText number;
    private  TableListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (TableListener) context;
        }catch (ClassCastException e) {
            throw new ClassCastException(context.toString()+" must implement ...");
        }
    }

    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.new_table_popup,null);

        builder.setView(view).setTitle(R.string.new_table).setNegativeButton(R.string.cancel, (dialog, which) -> {

        }).setPositiveButton(R.string.add, (dialog, which) -> {
            String table = number.getText().toString();
            listener.applyNumber(table);
        });
        number = view.findViewById(R.id.tableNumberView);
        return builder.create();
    }


    public interface TableListener{
        void applyNumber(String number);
    }
}
