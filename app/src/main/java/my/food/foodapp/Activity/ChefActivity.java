package my.food.foodapp.Activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import my.food.foodapp.Adapter.OrderRequestAdapter;
import my.food.foodapp.Domain.OrderRequest;
import my.food.foodapp.R;
import my.food.foodapp.databinding.ActivityChefBinding;
import my.food.foodapp.databinding.ActivityMainBinding;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// ChefActivity.java
public class ChefActivity extends BaseActivity {

    private ActivityChefBinding binding;
    private RecyclerView ordersRecyclerView;
    private OrderRequestAdapter orderRequestAdapter;
    private List<OrderRequest> orderRequestList = new ArrayList<>();
    private long selectedOrderId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChefBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
       // setContentView(R.layout.activity_chef);


        ordersRecyclerView = findViewById(R.id.ordersRecyclerView);

        orderRequestAdapter = new OrderRequestAdapter(orderRequestList, new OrderRequestAdapter.OnOrderClickListener() {
            @Override
            public void onOrderClick(Long orderId) { // Prihvaća orderId kao Long
                selectedOrderId = orderId; // Postavlja selectedOrderId
                Log.d("CHEF_ACTIVITY", "Selected Order ID: " + selectedOrderId);

                if (selectedOrderId != 0) {
                    // Poziv na backend API da ažurira status narudžbe
                    Call<Void> call = apiService.acceptOrder(selectedOrderId);
                    call.enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(ChefActivity.this, "Order accepted", Toast.LENGTH_SHORT).show();
                                fetchOrders(); // Osvježite listu nakon ažuriranja
                            } else {
                                Toast.makeText(ChefActivity.this, "Failed to accept order", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Toast.makeText(ChefActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(ChefActivity.this, "No order selected", Toast.LENGTH_SHORT).show();
                }
            }
        });

        ordersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        ordersRecyclerView.setAdapter(orderRequestAdapter);

        // Initial fetch of orders
        fetchOrders();
        setVariable();
    }



    private void fetchOrders() {

        Call<List<OrderRequest>> call = apiService.getRecentOrders();
        call.enqueue(new Callback<List<OrderRequest>>() {
            @Override
            public void onResponse(Call<List<OrderRequest>> call, Response<List<OrderRequest>> response) {

                if (response.isSuccessful() && response.body() != null) {
                    orderRequestList.clear();
                    orderRequestList.addAll(response.body());
                    orderRequestAdapter.notifyDataSetChanged();
                    
                } else {
                    Log.e("API_ERROR", "Response code: " + response.code() + " Message: " + response.message());
                    Toast.makeText(ChefActivity.this, "Failed to fetch orders", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<OrderRequest>> call, Throwable t) {
                Toast.makeText(ChefActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setVariable() {
        binding.logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutUser();
            }
        });
    }

    private void updateOrder(OrderRequest orderRequest) {

        Call<Void> call = apiService.updateOrder(orderRequest.getUserId(), orderRequest);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ChefActivity.this, "Order updated", Toast.LENGTH_SHORT).show();
                    fetchOrders(); // Refresh list after update
                } else {
                    Toast.makeText(ChefActivity.this, "Failed to update order", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(ChefActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
