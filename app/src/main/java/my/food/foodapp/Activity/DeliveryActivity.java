package my.food.foodapp.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import my.food.foodapp.Adapter.OrderRequestAdapter;
import my.food.foodapp.Domain.OrderRequest;
import my.food.foodapp.R;
import my.food.foodapp.databinding.ActivityChefBinding;
import my.food.foodapp.databinding.ActivityDeliveryBinding;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DeliveryActivity extends BaseActivity {

    ActivityDeliveryBinding binding;
    private RecyclerView recyclerView;
    private OrderRequestAdapter orderRequestAdapter;
    private List<OrderRequest> acceptedOrders = new ArrayList<>();
    private long selectedOrderId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDeliveryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Inicijaliziraj adapter s praznom listom
        orderRequestAdapter = new OrderRequestAdapter(new ArrayList<>(), new OrderRequestAdapter.OnOrderClickListener() {
            @Override
            public void onOrderClick(Long orderId) {
                selectedOrderId = orderId;
                Log.d("DELIVERY_ACTIVITY", "Selected Order ID: " + selectedOrderId);

                if(selectedOrderId != 0){
                    Call<Void> call = apiService.markOrderAsOnTheWay(selectedOrderId);
                    call.enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(DeliveryActivity.this, "Order accepted for delivery", Toast.LENGTH_SHORT).show();

                                SharedPreferences sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putLong("orderId", selectedOrderId);
                                editor.apply();

                                //finish();
                            } else {
                                Toast.makeText(DeliveryActivity.this, "Failed to update order status", Toast.LENGTH_SHORT).show();
                            }
                        }
                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Toast.makeText(DeliveryActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                        }
                    });
                }else{
                    Toast.makeText(DeliveryActivity.this, "No order selected", Toast.LENGTH_SHORT).show();
                }
            }
        });
        recyclerView.setAdapter(orderRequestAdapter);

        // Učitaj prihvaćene narudžbe
        fetchAcceptedOrders();
        setVariable();
    }

    private void fetchAcceptedOrders() {
        Call<List<OrderRequest>> call = apiService.getAcceptedOrdersFromLast24Hours();
        call.enqueue(new Callback<List<OrderRequest>>() {
            @Override
            public void onResponse(Call<List<OrderRequest>> call, Response<List<OrderRequest>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    acceptedOrders.clear();
                    acceptedOrders.addAll(response.body());
                    orderRequestAdapter.updateOrders(acceptedOrders);
                } else {
                    Log.d("DeliveryActivity", "Response failed. Status code: " + response.code());
                    Toast.makeText(DeliveryActivity.this, "Failed to load orders", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<OrderRequest>> call, Throwable t) {
                Log.d("DeliveryActivity", "Fetch orders failed: " + t.getMessage());
                Toast.makeText(DeliveryActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setVariable() {
        binding.logoutBtn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutUser();
            }
        });
    }
}

