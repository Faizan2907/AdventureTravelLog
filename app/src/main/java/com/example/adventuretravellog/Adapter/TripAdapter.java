package com.example.adventuretravellog.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.adventuretravellog.Model.Trip;
import com.example.adventuretravellog.R;
import com.example.adventuretravellog.TripDetailsActivity;

import java.util.List;

public class TripAdapter extends RecyclerView.Adapter<TripAdapter.TripViewHolder> {

    private List<String> tripNames;
    private List<String> tripIds;

    public TripAdapter(List<String> tripNames, List<String> tripIds) {
        this.tripNames = tripNames;
        this.tripIds = tripIds;
    }

    public void setTripData(List<String> tripNames, List<String> tripIds) {
        this.tripNames = tripNames;
        this.tripIds = tripIds;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TripViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_trip, parent, false);
        return new TripViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TripViewHolder holder, int position) {
        String tripName = tripNames.get(position);
        String tripId = tripIds.get(position);

        holder.textViewTripName.setText(tripName);

        holder.tripListClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the trip details or any additional information here
                String tripId = tripIds.get(holder.getAdapterPosition());

                Context context = holder.itemView.getContext();
                Intent intent = new Intent(context, TripDetailsActivity.class);
                intent.putExtra("tripId", tripId);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return tripNames.size();
    }

    static class TripViewHolder extends RecyclerView.ViewHolder {
        TextView textViewTripName;
        LinearLayout tripListClick;

        TripViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTripName = itemView.findViewById(R.id.textViewTripName);
            tripListClick = itemView.findViewById(R.id.tripListClick);
        }
    }

}