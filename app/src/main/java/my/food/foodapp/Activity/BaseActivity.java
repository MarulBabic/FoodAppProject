package my.food.foodapp.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import my.food.foodapp.Api.ApiClient;
import my.food.foodapp.Api.ApiService;
import my.food.foodapp.FeatureFlag;
import my.food.foodapp.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class BaseActivity extends AppCompatActivity {

    //Firebase specific variables
    FirebaseAuth mAuth;
    FirebaseDatabase database;

    //Spring specific variables
    protected ApiService apiService;
    private SharedPreferences sharedPreferences;

    public String TAG = "Marul";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String backendService = FeatureFlag.getBackendService(this);

        if ("firebase".equals(backendService)) {
            initFirebase();
        } else {
            initSpring();
        }

        getWindow().setStatusBarColor(getResources().getColor(R.color.white));
    }

    private void initFirebase() {
        database = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    private void initSpring() {
        Retrofit retrofit = ApiClient.getRetrofitInstance();
        apiService = retrofit.create(ApiService.class);

        sharedPreferences = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
    }


    //Firebase methods
    protected String getCurrentUserIdFirebase() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            return user.getUid();
        }
        return null;
    }

    protected DatabaseReference getUserDatabaseReference() {
        String userId = getCurrentUserIdFirebase();
        if (userId != null) {
            return database.getReference("users").child(userId);
        }
        return null;
    }

    //Spring methods
    protected long getCurrentUserIdSpring() {
        long userId = sharedPreferences.getLong("id", -1);
        return userId;
    }

    protected void saveUserId(long id) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong("id", id);
        editor.apply();
    }

    protected void logoutUser() {
        Call<Void> call = apiService.logout();
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    saveUserId(-1);
                    startActivity(new Intent(BaseActivity.this, LoginActivity.class));
                    finish();
                } else {
                    int statusCode = response.code();
                    String errorBody = response.errorBody() != null ? response.errorBody().toString() : "null";
                    Log.e("Logout Error", "Failed to logout. Status code: " + statusCode + ", Error body: " + errorBody);
                    Toast.makeText(BaseActivity.this, "Failed to logout", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(BaseActivity.this, "Failed to logout", Toast.LENGTH_SHORT).show();
            }
        });
    }

    protected boolean isUserLoggedIn() {
        String backendService = FeatureFlag.getBackendService(this);
        if ("firebase".equals(backendService)) {
            return getCurrentUserIdFirebase() != null;
        } else {
            return getCurrentUserIdSpring() != -1;
        }
    }

    protected String getCurrentUserId() {
        String backendService = FeatureFlag.getBackendService(this);
        if ("firebase".equals(backendService)) {
            return getCurrentUserIdFirebase();
        } else {
            return String.valueOf(getCurrentUserIdSpring());
        }
    }
}

