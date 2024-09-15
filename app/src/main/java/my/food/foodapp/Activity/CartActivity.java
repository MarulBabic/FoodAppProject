package my.food.foodapp.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import my.food.foodapp.Adapter.CartAdapter;
import my.food.foodapp.Api.ApiClient;
import my.food.foodapp.Api.ApiService;
import my.food.foodapp.Domain.Foods;
import my.food.foodapp.Domain.OrderItem;
import my.food.foodapp.Domain.OrderRequest;
import my.food.foodapp.Helper.ChangeNumberItemsListener;
import my.food.foodapp.Helper.ManagmentCart;
import my.food.foodapp.R;
import my.food.foodapp.databinding.ActivityCartBinding;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;

public class CartActivity extends BaseActivity {

    private ActivityCartBinding binding;
    private RecyclerView.Adapter adapter;
    private ManagmentCart managmentCart;
    private double tax;
    String SECRET_KEY="sk_test_51OtvnvFGFuK3sA9AFckcmM9VK6xJp6nkoFwWenkSnzkUtM7WMbdGJTFTo9ZZTCPTBcbIIPgiCwVMVvOSYLp8Lft700YlTntXBH";
    String PUBLISH_KEY="pk_test_51OtvnvFGFuK3sA9A3oILA9zlq7RyizJZGhhHbekxr7bQRopDIZje7YS7hzsfMRfml90yYJGoZLwej4mWuATwpvrc00SEKe57nJ";
    PaymentSheet paymentSheet;
    String customerID;
    String EphericalKey;
    String ClientSecret;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCartBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        managmentCart=new ManagmentCart(this);

        PaymentConfiguration.init(this,PUBLISH_KEY);

        paymentSheet = new PaymentSheet(this,paymentSheetResult -> {

            onPaymentResult(paymentSheetResult);
        });

        binding.orderbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculateCart();
                PaymentFlow();
            }
        });


        StringRequest stringRequest= new StringRequest(Request.Method.POST,
                "https://api.stripe.com/v1/customers",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject object = new JSONObject(response);
                            customerID = object.getString("id");

                            getEphericalKey(customerID);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> header = new HashMap<>();
                header.put("Authorization","Bearer "+SECRET_KEY);
                return header;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(CartActivity.this);
        requestQueue.add(stringRequest);

        setVariable();
        calculateCart();
        initList();
    }

    private OrderRequest createOrderRequest() {
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setUserId(getCurrentUserIdSpring()); // Postavite ID korisnika
        orderRequest.setTotalAmount(readTotalFromFile()); // Ukupni iznos
        orderRequest.setStatus("Pending"); // Postavite početni status narudžbe

        //dodavanje adrese za dostavu
        String address = binding.addressEditText.getText().toString().trim();

        orderRequest.setAddress(address);

        List<OrderItem> orderItems = new ArrayList<>();
        for (Foods foodItem : managmentCart.getListCart()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(foodItem.getId()); // ID proizvoda iz Foods instance
            orderItem.setQuantity(foodItem.getNumberInCart()); // Količina iz košarice
            orderItem.setPrice(foodItem.getPrice()); // Cijena iz Foods instance

            orderItems.add(orderItem);
        }
        orderRequest.setItems(orderItems);

        return orderRequest;
    }

    private void onPaymentResult(PaymentSheetResult paymentSheetResult) {
        if(paymentSheetResult instanceof PaymentSheetResult.Completed){
            Toast.makeText(this,"payment success",Toast.LENGTH_SHORT).show();

            OrderRequest orderRequest = createOrderRequest();
            sendOrderRequestToServer(orderRequest); // Metoda za slanje narudžbe na server
        }

    }

    private void sendOrderRequestToServer(OrderRequest orderRequest) {
       Call<Void> call= apiService.placeOrder(orderRequest);
       call.enqueue(new Callback<Void>() {
           @Override
           public void onResponse(Call<Void> call, retrofit2.Response<Void> response) {
               if(response.isSuccessful()){
                   Toast.makeText(CartActivity.this,"Order placed successfully",Toast.LENGTH_SHORT).show();
               }else{
                   Toast.makeText(CartActivity.this,"Failed to place order",Toast.LENGTH_SHORT).show();
               }
           }

           @Override
           public void onFailure(Call<Void> call, Throwable t) {
               Toast.makeText(CartActivity.this, "Failed to place order, network error", Toast.LENGTH_SHORT).show();
           }
       });

    }

    private void getEphericalKey(String customerID) {

        StringRequest stringRequest= new StringRequest(Request.Method.POST,
                "https://api.stripe.com/v1/ephemeral_keys",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject object = new JSONObject(response);
                            EphericalKey = object.getString("id");

                            getClientSecret(customerID,EphericalKey);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> header = new HashMap<>();
                header.put("Authorization","Bearer "+SECRET_KEY);
                header.put("Stripe-Version","2023-10-16");
                return header;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("customer",customerID);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(CartActivity.this);
        requestQueue.add(stringRequest);
    }

    private void getClientSecret(String customerID, String ephericalKey) {

        StringRequest stringRequest= new StringRequest(Request.Method.POST,
                "https://api.stripe.com/v1/payment_intents",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject object = new JSONObject(response);
                            ClientSecret = object.getString("client_secret");


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> header = new HashMap<>();
                header.put("Authorization","Bearer "+SECRET_KEY);
                return header;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("customer",customerID);
                double totalAmount = readTotalFromFile();
                params.put("amount", String.valueOf((int) (totalAmount * 100)));
                params.put("currency","eur");
                params.put("automatic_payment_methods[enabled]","true");
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(CartActivity.this);
        requestQueue.add(stringRequest);
    }

    private double readTotalFromFile() {
        try {
            FileInputStream fis = openFileInput("total.txt"); // Otvaramo datoteku za čitanje
            InputStreamReader isr = new InputStreamReader(fis); // Omogućava čitanje bajtova iz ulaznog toka u znakove
            BufferedReader br = new BufferedReader(isr); // Omogućava čitanje linije po liniju
            String line = br.readLine(); // Čitamo prvu liniju datoteke
            double total = Double.parseDouble(line); // Pretvaramo pročitanu liniju u double vrijednost
            br.close(); // Zatvaramo BufferedReader
            return total; // Vraćamo pročitanu vrijednost
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace(); // Ako dođe do greške, ispisujemo ju
            return 0.0; // Vratite neku defaultnu vrijednost ako ne uspijete pročitati datoteku
        }
    }

    private void PaymentFlow() {

        paymentSheet.presentWithPaymentIntent(
                ClientSecret,new PaymentSheet.Configuration("ABC Company"
                        ,new PaymentSheet.CustomerConfiguration(
                        customerID,
                        EphericalKey
                ))
        );

    }

    private void initList() {
        if(managmentCart.getListCart().isEmpty()){
            binding.emptyTxt.setVisibility(View.VISIBLE);
            binding.scrollViewCart.setVisibility(View.GONE);
        }else{
            binding.emptyTxt.setVisibility(View.GONE);
            binding.scrollViewCart.setVisibility(View.VISIBLE);
        }

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        binding.cardView.setLayoutManager(linearLayoutManager);
        adapter = new CartAdapter(managmentCart.getListCart(), this, new ChangeNumberItemsListener() {
            @Override
            public void change() {
                calculateCart();
            }
        });
        binding.cardView.setAdapter(adapter);
    }

    private void calculateCart() {
        double percentTax=0.02;
        double delivery=2;

        tax=Math.round(managmentCart.getTotalFee() * percentTax * 100.0)/100;

        double total= Math.ceil((managmentCart.getTotalFee() + tax + delivery)*100)/100;
        double itemTotal= Math.ceil(managmentCart.getTotalFee() * 100)/100;

        binding.totalFeeTxt.setText("€"+itemTotal);
        binding.taxTxt.setText("€"+tax);
        binding.deliveryTxt.setText("€"+delivery);
        binding.totalTxt.setText("€"+total);
        saveTotalToFile(total);
        if(customerID != null && EphericalKey!=null){
            getClientSecret(customerID,EphericalKey);
        }
    }
    private void saveTotalToFile(double total) {
        try {
            FileOutputStream fos = openFileOutput("total.txt", Context.MODE_PRIVATE);
            fos.write(String.valueOf(total).getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setVariable() {
        binding.backBtnn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }
}