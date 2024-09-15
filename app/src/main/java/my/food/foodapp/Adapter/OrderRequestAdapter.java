package my.food.foodapp.Adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import my.food.foodapp.Domain.OrderRequest;
import my.food.foodapp.R;

public class OrderRequestAdapter extends RecyclerView.Adapter<OrderRequestAdapter.OrderRequestViewHolder> {

    private List<OrderRequest> orderRequests;
    private OnOrderClickListener listener;

    public interface OnOrderClickListener {
        void onOrderClick(Long orderId);
    }

    public OrderRequestAdapter(List<OrderRequest> orderRequests, OnOrderClickListener listener) {
        this.orderRequests = orderRequests;
        this.listener = listener;
    }

    @NonNull
    @Override
    public OrderRequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_order_request, parent, false);
        return new OrderRequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderRequestViewHolder holder, int position) {
        OrderRequest orderRequest = orderRequests.get(position);
        holder.bind(orderRequest, listener);

    }

    public void updateOrders(List<OrderRequest> newOrders) {
        this.orderRequests = newOrders;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return orderRequests.size();
    }

    public static class OrderRequestViewHolder extends RecyclerView.ViewHolder {

        private TextView orderStatusView;
        private TextView orderTotalAmountView;
        private RecyclerView orderItemsRecyclerView;
        private Button acceptOrderButton;

        public OrderRequestViewHolder(@NonNull View itemView) {
            super(itemView);
            orderStatusView = itemView.findViewById(R.id.totalTxt);
            orderTotalAmountView = itemView.findViewById(R.id.orderTotalAmount);
            orderItemsRecyclerView = itemView.findViewById(R.id.orderItemsRecyclerView);
            acceptOrderButton = itemView.findViewById(R.id.acceptOrderButton);
        }

        public void bind(final OrderRequest orderRequest, final OnOrderClickListener listener) {
            orderTotalAmountView.setText(String.valueOf(orderRequest.getTotalAmount()));

            OrderItemAdapter orderItemAdapter = new OrderItemAdapter(orderRequest.getItems());
            orderItemsRecyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            orderItemsRecyclerView.setAdapter(orderItemAdapter);

            acceptOrderButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("ORDER_VIEW_HOLDER", "Accept button clicked for order ID: " + orderRequest.getOrderId());
                    listener.onOrderClick(orderRequest.getOrderId());
                }
            });
        }
    }


}
