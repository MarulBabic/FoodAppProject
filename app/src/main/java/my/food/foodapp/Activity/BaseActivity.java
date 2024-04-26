package my.food.foodapp.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import my.food.foodapp.R;

public class BaseActivity extends AppCompatActivity {
    FirebaseAuth mAuth;
    FirebaseDatabase database;
    public String TAG="Marul";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        database = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();

        getWindow().setStatusBarColor(getResources().getColor(R.color.white));
    }

    protected String getCurrentUserId() {                               // dohvaćanje ID trenutno prijavljenog korisnika
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            return user.getUid();
        }
        return null;
    }

    protected DatabaseReference getUserDatabaseReference() {         // dohvaćanje reference na odgovarajući čvor u DB gdje se nalaze podaci korisnika
        String userId = getCurrentUserId();
        if (userId != null) {
            return database.getReference("users").child(userId);     //users predstavlja glavni čvor
        }
        return null;
    }
}