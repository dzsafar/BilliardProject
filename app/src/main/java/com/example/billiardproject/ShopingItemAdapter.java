package com.example.billiardproject;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ShopingItemAdapter extends RecyclerView.Adapter<ShopingItemAdapter.ViewHolder> {
    private ArrayList<ShoppingItem> mShoppingItemsData;
    private Context mContext;
    private int lastPosition = -1;

    ShopingItemAdapter(Context context, ArrayList<ShoppingItem> itemsData){
        this.mShoppingItemsData = itemsData;
        this.mContext = context;
    };

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.list_item, parent, false));
    }

    @Override
    public void onBindViewHolder( ShopingItemAdapter.ViewHolder holder, int position) {
        ShoppingItem currentItem = mShoppingItemsData.get(position);

        holder.bindTo(currentItem);

        if (holder.getAdapterPosition() > lastPosition){
            Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.slide_in_row);
            holder.itemView.startAnimation(animation);
            lastPosition = holder.getAdapterPosition();
        }
    }

    @Override
    public int getItemCount() {
        return mShoppingItemsData.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView mTitleText;
        private TextView mFoglalasText ;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            mTitleText = itemView.findViewById(R.id.textViewTableNumber);
            mFoglalasText = itemView.findViewById(R.id.textViewBookingTime);

            itemView.findViewById(R.id.buttonBookTable).setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Log.d("Activity", "Foglalva");
                }
            });
        }

        public void bindTo(ShoppingItem currentItem) {
            mTitleText.setText(currentItem.getAsztal());
            mFoglalasText.setText(currentItem.getIdopont());
        }
    };
};

