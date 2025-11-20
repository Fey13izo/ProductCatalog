package mgarzon.createbest.productcatalog;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
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

public class MainActivity extends AppCompatActivity {

    // UI Components
    private EditText editTextName;
    private EditText editTextPrice;
    private Button addButton;
    private ListView listViewProducts;

    // Firebase
    private DatabaseReference databaseReference;

    // Data Structure for ListView
    private ArrayList<String> productList;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI components using the IDs from your XML
        editTextName = findViewById(R.id.editTextName);
        editTextPrice = findViewById(R.id.editTextPrice);
        addButton = findViewById(R.id.addButton);
        listViewProducts = findViewById(R.id.listViewProducts);

        // Initialize Firebase reference
        databaseReference = FirebaseDatabase.getInstance().getReference("products");

        // Initialize list and adapter
        productList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, productList);
        listViewProducts.setAdapter(adapter);

        // Set button click listener
        addButton.setOnClickListener(v -> addProduct());

        // Load products from Firebase
        loadProducts();
    }

    /**
     * Load all products from Firebase and display in the ListView
     */
    private void loadProducts() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                productList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Assumes you have a Product.java class
                    Product product = snapshot.getValue(Product.class);
                    if (product != null) {
                        String productInfo = "Name: " + product.getProductName() + "\n" +
                                "Price: $" + String.format("%.2f", product.getPrice());
                        productList.add(productInfo);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Failed to load products: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Add a new product to Firebase
     */
    private void addProduct() {
        String productName = editTextName.getText().toString().trim();
        String priceStr = editTextPrice.getText().toString().trim();

        if (productName.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(this, "Please enter product name and price", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double price = Double.parseDouble(priceStr);
            String productId = databaseReference.push().getKey();

            // The Product.java class should have a constructor like this
            Product product = new Product(productName, price);

            if (productId != null) {
                databaseReference.child(productId).setValue(product)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Product added successfully", Toast.LENGTH_SHORT).show();
                            // Clear the input fields after successful addition
                            editTextName.setText("");
                            editTextPrice.setText("");
                        })
                        .addOnFailureListener(e -> Toast.makeText(this, "Failed to add product.", Toast.LENGTH_SHORT).show());
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid price format", Toast.LENGTH_SHORT).show();
        }
    }
}

