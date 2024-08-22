package my.food.foodapp.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.CenterInside;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;

import java.util.List;

import my.food.foodapp.Domain.OrderItem;
import my.food.foodapp.R;

// OrderAdapter.java
public class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.OrderItemViewHolder> {

    private List<OrderItem> orderItems;
    private Context context;

    public OrderItemAdapter(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    @NonNull
    @Override
    public OrderItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.viewholder_order_item, parent, false);
        return new OrderItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderItemViewHolder holder, int position) {
        OrderItem orderItem = orderItems.get(position);

        holder.productNameView.setText(orderItem.getTitle()); // Koristite naziv proizvoda
        holder.quantityView.setText("Quantity: " + orderItem.getQuantity());
        holder.priceView.setText("$" + orderItem.getPrice());

        Glide.with(context)
                .load(orderItem.getImagePath())
                .transform(new CenterInside(), new RoundedCorners(30))
                .into(holder.productImageView);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Ovdje možeš dodati Intent ili bilo koju drugu akciju pri kliku na stavku
            }
        });
    }

    @Override
    public int getItemCount() {
        return orderItems.size();
    }

    public static class OrderItemViewHolder extends RecyclerView.ViewHolder {

        private TextView productNameView;
        private TextView quantityView;
        private TextView priceView;
        private ImageView productImageView;

        public OrderItemViewHolder(@NonNull View itemView) {
            super(itemView);
            productNameView = itemView.findViewById(R.id.orderItemProductName);
            quantityView = itemView.findViewById(R.id.orderItemQuantity);
            priceView = itemView.findViewById(R.id.orderItemPrice);
            productImageView = itemView.findViewById(R.id.orderItemImage);
        }
    }
}
