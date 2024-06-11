package my.food.foodapp.Activity;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.    RecyclerView;

import android.content.Intent;
import android.os.Bundle;

import my.food.foodapp.FeatureFlag;
import okhttp3.ResponseBody;
import retrofit2.Call;

import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import my.food.foodapp.Adapter.BestFoodsAdapter;
import my.food.foodapp.Adapter.CategoryAdapter;
import my.food.foodapp.Api.ApiClient;
import my.food.foodapp.Api.ApiService;
import my.food.foodapp.Domain.Category;
import my.food.foodapp.Domain.Foods;
import my.food.foodapp.Domain.Location;
import my.food.foodapp.Domain.Price;
import my.food.foodapp.Domain.Time;
import my.food.foodapp.Domain.Users;
import my.food.foodapp.R;
import my.food.foodapp.databinding.ActivityMainBinding;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends BaseActivity {
    private ActivityMainBinding binding;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        apiService = ApiClient.getRetrofitInstance().create(ApiService.class);
        
        initData();
        initLocation();
        initTime();
        initPrice();
        initBestFood();
        initCategory();
        setVariable();
    }

    private void initData() {
        String backendService = FeatureFlag.getBackendService(MainActivity.this);
        if("firebase".equals(backendService)){
            initFirebaseData();
        }else{
            initSpringData();
        }
    }

    private void initSpringData() {
        long userId = getCurrentUserIdSpring();
        Call<ResponseBody> call = apiService.getUser(userId);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    ResponseBody responseBody = response.body();
                    if (responseBody != null) {
                        try {
                            String responseData = responseBody.string();
                            if (!responseData.isEmpty()) {
                                JSONObject jsonObject = new JSONObject(responseData);

                                String firstName = jsonObject.getString("firstName");
                                String lastName = jsonObject.getString("lastName");

                                binding.fName.setText(firstName);
                                binding.lName.setText(lastName);
                            } else {
                                Log.e("Spring Data", "Response body is empty");
                            }
                        } catch (IOException | JSONException e) {
                            e.printStackTrace();
                            Log.e("Spring Data", "Failed to read or parse response body", e);
                        }
                    } else {
                        Log.e("Spring Data", "Response body is null");
                    }
                } else {
                    Log.e("Spring Data", "Request failed with code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("Spring Data", "Failed to get user data", t);
            }
        });
    }

    private void initFirebaseData() {
        DatabaseReference userRef = getUserDatabaseReference();
        if(userRef!=null){
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String firstName = snapshot.child("firstName").getValue(String.class);
                        String lastName = snapshot.child("lastName").getValue(String.class);

                        binding.fName.setText(firstName);
                        binding.lName.setText(lastName);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    private void setVariable() {
        binding.logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            logoutUser();
            }
        });

        binding.searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text=binding.searchEdit.getText().toString();
                if(!text.isEmpty()){
                    Intent intent = new Intent(MainActivity.this,ListFoodsActivity.class);
                    intent.putExtra("text",text);
                    intent.putExtra("isSearch",true);
                    startActivity(intent);
                }
            }
        });

        binding.cartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,CartActivity.class));
            }
        });

    }


    private void initBestFood(){
        String backendService = FeatureFlag.getBackendService(this);

        if("firebase".equals(backendService)){
            DatabaseReference myRef = database.getReference("Foods");
            binding.progressBarBestFood.setVisibility(View.VISIBLE);
            ArrayList<Foods> list = new ArrayList<>();
            Query query = myRef.orderByChild("BestFood").equalTo(true);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        for(DataSnapshot issue: snapshot.getChildren()){
                            list.add(issue.getValue(Foods.class));
                        }
                        if(list.size() > 0){
                            binding.bestFoodView.setLayoutManager(new LinearLayoutManager(MainActivity.this,LinearLayoutManager.HORIZONTAL,false));
                            RecyclerView.Adapter adapter = new BestFoodsAdapter(list);
                            binding.bestFoodView.setAdapter(adapter);
                        }
                        binding.progressBarBestFood.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }else {
            binding.progressBarBestFood.setVisibility(View.VISIBLE);
            Call<List<Foods>> call = apiService.getBestFoods();
            call.enqueue(new Callback<List<Foods>>() {
                @Override
                public void onResponse(Call<List<Foods>> call, Response<List<Foods>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<Foods> list = response.body();
                        ArrayList<Foods> arrayList = new ArrayList<>(list);
                        binding.bestFoodView.setLayoutManager(new LinearLayoutManager(MainActivity.this, LinearLayoutManager.HORIZONTAL, false));
                        RecyclerView.Adapter adapter = new BestFoodsAdapter(arrayList);
                        binding.bestFoodView.setAdapter(adapter);
                    }
                    binding.progressBarBestFood.setVisibility(View.GONE);
                }

                @Override
                public void onFailure(Call<List<Foods>> call, Throwable t) {
                    binding.progressBarBestFood.setVisibility(View.GONE);
                }
            });
        }
    }

    private void initCategory(){
        String backendService = FeatureFlag.getBackendService(this);

        if("firebase".equals(backendService)){
            DatabaseReference myRef = database.getReference("Category");
            binding.progressBarCategory.setVisibility(View.VISIBLE);
            ArrayList<Category> list = new ArrayList<>();
            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        for(DataSnapshot categorySnapshot: snapshot.getChildren()){
                            list.add(categorySnapshot.getValue(Category.class));
                        }
                        if(list.size() > 0){
                            binding.categoryView.setLayoutManager(new GridLayoutManager(MainActivity.this,4));
                            RecyclerView.Adapter adapter = new CategoryAdapter(list);
                            binding.categoryView.setAdapter(adapter);
                        }
                        binding.progressBarCategory.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }else {
            binding.progressBarCategory.setVisibility(View.VISIBLE);
            Call<List<Category>> call = apiService.getCategories();
            call.enqueue(new Callback<List<Category>>() {
                @Override
                public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<Category> list = response.body();
                        ArrayList<Category> arrayList = new ArrayList<>(list);
                        binding.categoryView.setLayoutManager(new GridLayoutManager(MainActivity.this, 4));
                        RecyclerView.Adapter adapter = new CategoryAdapter(arrayList);
                        binding.categoryView.setAdapter(adapter);
                    }
                    binding.progressBarCategory.setVisibility(View.GONE);
                }

                @Override
                public void onFailure(Call<List<Category>> call, Throwable t) {
                    binding.progressBarCategory.setVisibility(View.GONE);
                }
            });
        }
    }


    private void initLocation() {
        String backendService = FeatureFlag.getBackendService(this);

        if("firebase".equals(backendService)){
            DatabaseReference myRef = database.getReference("Location");
            ArrayList<Location> list=new ArrayList<>();
            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        for(DataSnapshot locationSnapshot: snapshot.getChildren()){
                            list.add(locationSnapshot.getValue(Location.class));
                        }
                        ArrayAdapter<Location> adapter = new ArrayAdapter<>(MainActivity.this,R.layout.sp_item,list);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        binding.locationSp.setAdapter(adapter);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }else {
            Call<List<Location>> call = apiService.getLocations();
            call.enqueue(new Callback<List<Location>>() {
                @Override
                public void onResponse(Call<List<Location>> call, Response<List<Location>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<Location> list = response.body();
                        ArrayAdapter<Location> adapter = new ArrayAdapter<>(MainActivity.this, R.layout.sp_item, list);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        binding.locationSp.setAdapter(adapter);
                    }
                }

                @Override
                public void onFailure(Call<List<Location>> call, Throwable t) {

                }
            });
        }
    }

    private void initTime() {
        String backendService = FeatureFlag.getBackendService(this);

        if("firebase".equals(backendService)){
            DatabaseReference myRef = database.getReference("Time");
            ArrayList<Time> list=new ArrayList<>();
            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        for(DataSnapshot timeSnapshot: snapshot.getChildren()){
                            list.add(timeSnapshot.getValue(Time.class));
                        }
                        ArrayAdapter<Time> adapter = new ArrayAdapter<>(MainActivity.this,R.layout.sp_item,list);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        binding.timeSp.setAdapter(adapter);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }else {
            Call<List<Time>> call = apiService.getTimes();
            call.enqueue(new Callback<List<Time>>() {
                @Override
                public void onResponse(Call<List<Time>> call, Response<List<Time>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<Time> list = response.body();
                        ArrayAdapter<Time> adapter = new ArrayAdapter<>(MainActivity.this, R.layout.sp_item, list);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        binding.timeSp.setAdapter(adapter);
                    }
                }

                @Override
                public void onFailure(Call<List<Time>> call, Throwable t) {
                    // Handle failure
                }
            });
        }
    }

    private void initPrice() {
        String backendService = FeatureFlag.getBackendService(this);

        if("firebase".equals(backendService)){
            DatabaseReference myRef = database.getReference("Price");
            ArrayList<Price> list=new ArrayList<>();
            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        for(DataSnapshot priceSnapshot: snapshot.getChildren()){
                            list.add(priceSnapshot.getValue(Price.class));
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }else {
            Call<List<Price>> call = apiService.getPrices();
            call.enqueue(new Callback<List<Price>>() {
                @Override
                public void onResponse(Call<List<Price>> call, Response<List<Price>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<Price> list = response.body();
                    }
                }

                @Override
                public void onFailure(Call<List<Price>> call, Throwable t) {
                    // Handle failure
                }
            });
        }
    }
}