// In app/src/main/java/mgarzon/createbest/productcatalog/MainActivity.java

package mgarzon.createbest.productcatalog;

// IMPORT STATEMENTS HAVE BEEN CORRECTED TO USE 'androidx'
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

// This class now correctly extends the androidx version of AppCompatActivity
public class MainActivity extends AppCompatActivity {

    EditText editTextName;
    EditText editTextPrice;
    Button buttonAddProduct;
    ListView listViewProducts;

    List<Product> products;

    DatabaseReference databaseProducts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        databaseProducts = FirebaseDatabase.getInstance().getReference("products");
        editTextName = findViewById(R.id.editTextName);
        editTextPrice = findViewById(R.id.editTextPrice);
        listViewProducts = findViewById(R.id.listViewProducts);
        buttonAddProduct = findViewById(R.id.addButton);

        products = new ArrayList<>();

        buttonAddProduct.setOnClickListener(view -> addProduct());

        listViewProducts.setOnItemLongClickListener((adapterView, view, i, l) -> {
            Product product = products.get(i);
            showUpdateDeleteDialog(product.getId(), product.getProductName());
            return true;
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        databaseProducts.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                products.clear();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Product product = postSnapshot.getValue(Product.class);
                    products.add(product);
                }
                ProductList productsAdapter = new ProductList(MainActivity.this, products);
                listViewProducts.setAdapter(productsAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // It's good practice to log errors
                Toast.makeText(MainActivity.this, "Failed to load products: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showUpdateDeleteDialog(final String productId, String productName) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.update_dialog, null);
        dialogBuilder.setView(dialogView);

        final EditText editTextName = dialogView.findViewById(R.id.editTextName);
        final EditText editTextPrice  = dialogView.findViewById(R.id.editTextPrice);
        final Button buttonUpdate = dialogView.findViewById(R.id.buttonUpdateProduct);
        final Button buttonDelete = dialogView.findViewById(R.id.buttonDeleteProduct);

        dialogBuilder.setTitle(productName);
        final AlertDialog b = dialogBuilder.create();
        b.show();

        buttonUpdate.setOnClickListener(view -> {
            String name = editTextName.getText().toString().trim();
            String priceStr = editTextPrice.getText().toString();
            if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(priceStr)) {
                try {
                    double price = Double.parseDouble(priceStr);
                    updateProduct(productId, name, price);
                    b.dismiss();
                } catch (NumberFormatException e) {
                    Toast.makeText(MainActivity.this, "Please enter a valid price", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(MainActivity.this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
            }
        });

        buttonDelete.setOnClickListener(view -> {
            deleteProduct(productId);
            b.dismiss();
        });
    }

    private void updateProduct(String id, String name, double price) {
        DatabaseReference dR = FirebaseDatabase.getInstance().getReference("products").child(id);
        Product product = new Product(id, name, price);
        dR.setValue(product);
        Toast.makeText(getApplicationContext(), "Product Updated", Toast.LENGTH_LONG).show();
    }

    private boolean deleteProduct(String id) {
        DatabaseReference dR = FirebaseDatabase.getInstance().getReference("products").child(id);
        dR.removeValue();
        Toast.makeText(getApplicationContext(), "Product Deleted", Toast.LENGTH_LONG).show();
        return true;
    }

    private void addProduct() {
        String name = editTextName.getText().toString().trim();
        String priceStr = editTextPrice.getText().toString().trim();

        if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(priceStr)) {
            try {
                double price = Double.parseDouble(priceStr);
                String id = databaseProducts.push().getKey();
                Product product = new Product(id, name, price);
                if (id != null) {
                    databaseProducts.child(id).setValue(product);
                }
                editTextName.setText("");
                editTextPrice.setText("");
                Toast.makeText(this, "Product added", Toast.LENGTH_LONG).show();
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Please enter a valid price", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Please enter a name and price", Toast.LENGTH_LONG).show();
        }
    }
}
