package my.food.foodapp.Api;

import java.util.List;
import java.util.Map;

import my.food.foodapp.Domain.Category;
import my.food.foodapp.Domain.Foods;
import my.food.foodapp.Domain.Location;
import my.food.foodapp.Domain.OrderRequest;
import my.food.foodapp.Domain.Price;
import my.food.foodapp.Domain.Time;
import my.food.foodapp.Domain.Users;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    @POST("/api/users/register")
    Call<ResponseBody> registerUser(@Body Users user);

    @POST("/api/users/login")
    Call<ResponseBody> loginUser(@Body Map<String,String> loginRequest);

    @GET("/api/users/{userId}")
    Call<ResponseBody> getUser(@Path("userId") long userId);

    @GET("/api/foods/{id}")
    Call<Foods> getFoodById(@Path("id") long id);

    @GET("/api/foods/best")
    Call<List<Foods>> getBestFoods();

    @GET("/api/categories")
    Call<List<Category>> getCategories();

    @GET("/api/location")
    Call<List<Location>> getLocations();

    @GET("/api/time")
    Call<List<Time>> getTimes();

    @GET("/api/price")
    Call<List<Price>> getPrices();

    @POST("/api/users/logout")
    Call<Void> logout();

    @GET("/api/foods/search")
    Call<List<Foods>> searchFoods(@Query("text") String searchText);

    @GET("/api/foods/category")
    Call<List<Foods>> getFoodsByCategory(@Query("categoryId") int categoryId);

    @POST("/api/orders")
    Call<Void> placeOrder(@Body OrderRequest orderRequest);

    @GET("/api/orders")
    Call<List<OrderRequest>> getOrders();

    @GET("/api/orders/recent")
    Call<List<OrderRequest>> getRecentOrders();

    @POST("/api/orders/{id}")
    Call<Void> updateOrder(@Path("id") long id, @Body OrderRequest orderRequest);

    @PUT("/api/orders/{orderId}/accept")
    Call<Void> acceptOrder(@Path("orderId") long orderId);
}
