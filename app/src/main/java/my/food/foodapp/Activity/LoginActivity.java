package my.food.foodapp.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import my.food.foodapp.FeatureFlag;
import my.food.foodapp.databinding.ActivityLoginBinding;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends BaseActivity {

    ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setVariable();
    }

    private void setVariable() {
        binding.backBtnn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this,IntroActivity.class));
            }
        });

        binding.loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = binding.userEdt.getText().toString();
                String password = binding.passEdt.getText().toString();
                if(!email.isEmpty() && !password.isEmpty()){
                    String backendService = FeatureFlag.getBackendService(LoginActivity.this);
                    if ("firebase".equals(backendService)) {
                        loginWithFirebase(email, password);
                    } else {
                        loginWithSpring(email, password);
                    }
                }else{
                    Toast.makeText(LoginActivity.this,"Please fill username and password",Toast.LENGTH_SHORT).show();
                }
            }
        });

        binding.signupBtnn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this,SignupActivity.class));
            }
        });
    }

    private void loginWithSpring(String email, String password) {
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("email", email);
        loginRequest.put("password", password);

        Call<ResponseBody> call = apiService.loginUser(loginRequest);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        String responseData = response.body().string();
                        Log.d(TAG, "Response data: " + responseData);

                        // Parsiranje JSON odgovora
                        JSONObject jsonObject = new JSONObject(responseData);
                        long userId = jsonObject.getLong("id");
                        String userType = jsonObject.getString("userType");

                        saveUserId(userId);

                        if ("user".equals(userType)) {
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        } else if ("chef".equals(userType)) {
                            startActivity(new Intent(LoginActivity.this, ChefActivity.class));
                        } else if ("delivery".equals(userType)) {
                            startActivity(new Intent(LoginActivity.this, DeliveryActivity.class));
                        }else{
                            Toast.makeText(LoginActivity.this, "Unknown user type", Toast.LENGTH_SHORT).show();
                        }
                        finish();
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(LoginActivity.this, "Failed to read response", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "Authentication failed with status code: " + response.code());
                    Toast.makeText(LoginActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("NetworkError", "Network error: " + t.getMessage());
                Toast.makeText(LoginActivity.this, "Network error. Please try again later.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isNumeric(String str) {
        return str != null && str.matches("-?\\d+(\\.\\d+)?");
    }

    private void loginWithFirebase(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                } else {
                    Toast.makeText(LoginActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}