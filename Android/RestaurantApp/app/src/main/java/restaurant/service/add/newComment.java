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
import restaurant.storage.Product;

public class newComment extends AppCompatDialogFragment {
    private EditText comment;
    private CommentListener listener;
    private String text;
    private Product product;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (CommentListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement CommentListener");
        }
    }


    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.new_comment_popup, null);
        comment = view.findViewById(R.id.productName);
        comment.setText(text);
        builder.setView(view).setTitle(R.string.new_comment).setMessage(R.string.comment_info).setNegativeButton(R.string.cancel, (dialog, which) -> {})
                .setPositiveButton(R.string.add, (dialog, which) ->
                        listener.applyComment(product, comment.getText().toString())
                );
        return builder.create();
    }

    public void setData(Product product, String text) {
        this.product = product;
        this.text = text;
    }


    public interface CommentListener {
        void applyComment(Product p, String comment);
    }
}
