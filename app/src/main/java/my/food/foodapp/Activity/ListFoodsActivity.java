package my.food.foodapp.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import my.food.foodapp.Adapter.FoodListAdapter;
import my.food.foodapp.Api.ApiClient;
import my.food.foodapp.Api.ApiService;
import my.food.foodapp.Domain.Foods;
import my.food.foodapp.FeatureFlag;
import my.food.foodapp.R;
import my.food.foodapp.databinding.ActivityListFoodsBinding;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ListFoodsActivity extends BaseActivity {

    ActivityListFoodsBinding binding;
    private RecyclerView.Adapter adapterListFood;
    private long categoryId;
    private String categoryName;
    private String searchText;
    private boolean isSearch;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityListFoodsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        apiService= ApiClient.getRetrofitInstance().create(ApiService.class);

        getIntentExtra();
        initList();
        setVariable();
    }

    private void setVariable() {
        binding.backBtnn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initList() {
        String backendService = FeatureFlag.getBackendService(this);

        if("firebase".equals(backendService)){
            DatabaseReference myRef=database.getReference("Foods");
            binding.progressBar.setVisibility(View.VISIBLE);
            ArrayList<Foods> list =new ArrayList<>();

            Query query;
            if(isSearch){
                query=myRef.orderByChild("Title").startAt(searchText).endAt(searchText+'\uf8ff');
            }else{
                query=myRef.orderByChild("CategoryId").equalTo(categoryId);
            }
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        for(DataSnapshot issue : snapshot.getChildren()){
                            list.add(issue.getValue(Foods.class));
                        }
                        if(list.size()>0){
                            binding.foodListView.setLayoutManager(new GridLayoutManager(ListFoodsActivity.this,2));
                            adapterListFood=new FoodListAdapter(list);
                            binding.foodListView.setAdapter(adapterListFood);
                        }
                        binding.progressBar.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }else {
            binding.progressBar.setVisibility(View.VISIBLE);
            Call<List<Foods>> call;
            if (isSearch) {
                call = apiService.searchFoods(searchText);
            } else {
                Log.d("ListFoodsActivity", "Fetching foods for category ID: " + categoryId);
                call = apiService.getFoodsByCategory(categoryId);
            }

            call.enqueue(new Callback<List<Foods>>() {
                @Override
                public void onResponse(Call<List<Foods>> call, Response<List<Foods>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<Foods> list = response.body();
                        ArrayList<Foods> arrayList = new ArrayList<>(list);
                        if (!list.isEmpty()) {
                            binding.foodListView.setLayoutManager(new GridLayoutManager(ListFoodsActivity.this, 2));
                            adapterListFood = new FoodListAdapter(arrayList);
                            binding.foodListView.setAdapter(adapterListFood);
                        }
                    }
                    binding.progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onFailure(Call<List<Foods>> call, Throwable t) {
                    // Handle failure
                    binding.progressBar.setVisibility(View.GONE);
                }
            });
        }
    }

    private void getIntentExtra() {
        categoryId = getIntent().getLongExtra("CategoryId",0);
        categoryName = getIntent().getStringExtra("CategoryName");
        searchText = getIntent().getStringExtra("text");
        isSearch = getIntent().getBooleanExtra("isSearch",false);

        binding.titleTxt.setText(categoryName);
        binding.backBtnn.setOnClickListener(v -> finish());
    }
}