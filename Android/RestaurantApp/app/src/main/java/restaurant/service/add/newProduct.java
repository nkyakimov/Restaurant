package restaurant.service.add;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import restaurant.app.R;
import restaurant.service.adapters.ChosenProductAdapter;
import restaurant.service.adapters.ProductAddAdapter;
import restaurant.service.orders.UnsentOrders;
import restaurant.storage.Product;

import static restaurant.service.handler.TableHandler.order;
import static restaurant.service.handler.TableHandler.requestProducts;

public class newProduct extends AppCompatActivity implements newComment.CommentListener {
    private ListView toOrderList;
    private List<Product> lastRequested;
    private ListView toChooseList;
    private Map<Product, Integer> toOrderProducts;
    private Map<Product, String> comments;
    private int id;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        comments = new HashMap<>();
        id = (int) getIntent().getExtras().get("Id");
        toOrderProducts = new HashMap<>();
        setContentView(R.layout.add_product_activity);
        toOrderList = findViewById(R.id.toOrder);
        toChooseList = findViewById(R.id.toChoose);
        EditText productToAdd = findViewById(R.id.addProductEditText);
        ChosenProductAdapter chosenProductAdapter = new ChosenProductAdapter(getApplicationContext(), toOrderProducts);
        FloatingActionButton showHide = findViewById(R.id.show_hide);
        showHide.setOnClickListener(v -> {
            ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) productToAdd.getLayoutParams();
            if (layoutParams.verticalBias < 0.5) {
                layoutParams.verticalBias = 0.8f;
            } else {
                layoutParams.verticalBias = 0.4f;
            }
            productToAdd.setLayoutParams(layoutParams);
        });
        FloatingActionButton orderButton = findViewById(R.id.sendOrderButton);
        orderButton.setOnClickListener(v -> {
            Boolean error = false;
            for (Map.Entry<Product, Integer> productAndCount : toOrderProducts.entrySet()) {
                int i = 0;
                int count = productAndCount.getValue();
                Product product = productAndCount.getKey();
                while (i < count) {
                    String comment;
                    if ((comment = comments.get(product)) == null) {
                        comment = "";
                    } else {
                        if (comment.contains(System.getProperty("line.separator")) && i != count - 1) {
                            try {
                                comments.replace(product, comments.get(product).substring(comment.indexOf(System.getProperty("line.separator")) + 1));
                            } catch (Exception e) {
                                comments.remove(product);
                            }
                            comment = comment.substring(0, comment.indexOf(System.getProperty("line.separator")));
                        } else {
                            comments.remove(product);
                        }
                    }
                    if (error) {
                        UnsentOrders.addOrderToSendLater(id, product.getId(), comment);
                    } else if (order(id, product.getId(), comment) == -1) {
                        error = true;
                        UnsentOrders.addOrderToSendLater(id, product.getId(), comment);
                    }
                    i++;
                }
            }
            toOrderProducts.clear();
            finish();
        });
        toOrderList.setOnItemClickListener((parent, view, position, id) -> {
            newComment comment = new newComment();
            Product temp = new ArrayList<>(toOrderProducts.keySet()).get(position);
            comment.setData(temp, comments.get(temp));
            comment.show(getSupportFragmentManager(), "new Comment dialog");
        });
        toOrderList.setOnItemLongClickListener((parent, view, position, id) -> {
            Product temp = new ArrayList<>(toOrderProducts.keySet()).get(position);
            try {
                int i;
                if ((i = toOrderProducts.get(temp)) == 1) {
                    toOrderProducts.remove(temp);
                } else {
                    toOrderProducts.replace(temp, i - 1);
                }
                chosenProductAdapter.notifyDataSetChanged();
            } catch (NullPointerException e) {

            }
            return true;
        });
        toChooseList.setOnItemClickListener((parent, view, position, id) -> {
            Product temp;
            try {
                temp = lastRequested.get(position);
            } catch (NullPointerException e) {
                return;
            }
            if (temp == null) {
                return;
            }
            try {
                toOrderProducts.replace(temp, toOrderProducts.get(temp) + 1);
            } catch (NullPointerException e) {
                toOrderProducts.put(temp, 1);
            } finally {
                toOrderList.setAdapter(chosenProductAdapter);
            }
        });
        productToAdd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().isEmpty()) {
                    toChooseList.setAdapter(new ProductAddAdapter(getApplicationContext(), (lastRequested = requestProducts(s.toString()))));
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (toOrderProducts.size() > 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.leave_without_order).setPositiveButton("Leave", (dialog, which) -> finish())
                    .setTitle(R.string.error)
                    .show();
        } else {
            finish();
        }
    }

    @Override
    public void applyComment(Product product, String comment) {
        if (comments.get(product) != null) {
            comments.replace(product, comment);
        } else {
            comments.put(product, comment);
        }
    }
}
