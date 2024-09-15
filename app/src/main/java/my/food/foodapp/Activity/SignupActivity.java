package my.food.foodapp.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

import java.io.IOException;

import my.food.foodapp.Domain.Users;
import my.food.foodapp.FeatureFlag;
import my.food.foodapp.databinding.ActivitySignupBinding;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignupActivity extends BaseActivity {

    ActivitySignupBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setVariable();
    }

    private void setVariable() {
        binding.backBtnn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignupActivity.this,IntroActivity.class));
            }
        });

        binding.signupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String fName = binding.fnameEdt.getText().toString().trim();
                final String lName = binding.lnameEdt.getText().toString().trim();
                String email = binding.userEdt.getText().toString();
                String password = binding.passEdt.getText().toString();
                RadioButton radioUser = binding.radioUser;
                RadioButton radioChef = binding.radioChef;
                RadioButton radioDelivery = binding.radioDelivery;

                if (TextUtils.isEmpty(fName) || TextUtils.isEmpty(lName) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                    Toast.makeText(SignupActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(password.length() < 6){
                    Toast.makeText(SignupActivity.this,"Password must have 6 characters",Toast.LENGTH_SHORT).show();
                    return;
                }

                //dohvacanje userType
                String userType;
                if (radioUser.isChecked()) {
                    userType = "user";
                } else if (radioChef.isChecked()) {
                    userType = "chef";
                } else if (radioDelivery.isChecked()) {
                    userType = "delivery";
                } else {
                    // Opcionalno: postavite defaultnu vrijednost ako nijedan nije odabran
                    userType = "unknown";
                }

                String backendService = FeatureFlag.getBackendService(SignupActivity.this);

                if("firebase".equals(backendService)){
                    signupWithFirebase(email, password, fName, lName);
                }else{
                    signupWithSpring(email, password, fName, lName,userType);
                }

            }
        });

        binding.loginBtnn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignupActivity.this,LoginActivity.class));
            }
        });
    }

    private void signupWithFirebase(String email, String password, String fName, String lName) {
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        DatabaseReference userRef = database.getReference("users").child(user.getUid());
                        userRef.child("firstName").setValue(fName);
                        userRef.child("lastName").setValue(lName);
                    }

                    startActivity(new Intent(SignupActivity.this, MainActivity.class));
                    finish();
                } else {
                    Log.e(TAG, "createUserWithEmail:failure", task.getException());
                    Toast.makeText(SignupActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void signupWithSpring(String email, String password, String fName, String lName,String userType) {
        Users user = new Users();
        user.setFirstName(fName);
        user.setLastName(lName);
        user.setEmail(email);
        user.setPassword(password);
        user.setUserType(userType);

        Call<ResponseBody> call = apiService.registerUser(user);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                int statusCode = response.code();
                if(response.isSuccessful()){
                    try {
                        //Pretvaramo tijelo odgovora u string
                        String responseData = response.body().string();
                        Log.d(TAG, "Response data: " + responseData);

                        //provjera jeli odgovor numericki
                        if(isNumeric(responseData)){
                            long userId = Long.parseLong(responseData);
                            saveUserId(userId);
                            if ("user".equals(userType)) {
                                startActivity(new Intent(SignupActivity.this, MainActivity.class));
                            } else if("chef".equals(userType)) {
                                startActivity(new Intent(SignupActivity.this, ChefActivity.class));
                            }else{
                                startActivity(new Intent(SignupActivity.this, DeliveryActivity.class));
                            }
                            finish();
                        }else{
                            Toast.makeText(SignupActivity.this, "Invalid response format", Toast.LENGTH_SHORT).show();
                        }
                    } catch (IOException e){
                        e.printStackTrace();
                        Toast.makeText(SignupActivity.this, "Failed to read response", Toast.LENGTH_SHORT).show();
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        Toast.makeText(SignupActivity.this, "Failed to parse user ID", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Log.d(TAG, "HTTP Status Code: " + statusCode);
                    Toast.makeText(SignupActivity.this, "Registration failed. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "Network error: " + t.getMessage());
                Toast.makeText(SignupActivity.this, "Registration failed. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private boolean isNumeric(String str) {
        return str != null && str.matches("-?\\d+(\\.\\d+)?");
    }
}