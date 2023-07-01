package com.example.adventuretravellog.Adapter;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.adventuretravellog.Model.Trip;
import com.example.adventuretravellog.R;
import com.example.adventuretravellog.TripDetailsActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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

    public void addTrip(String tripName, String tripId) {
        tripNames.add(tripName);
        tripIds.add(tripId);
        notifyItemInserted(tripNames.size() - 1);
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
        String tripId = tripIds.get(holder.getAdapterPosition());

        holder.textViewTripName.setText(tripName);

        holder.tripListClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the trip details or any additional information here

                Context context = holder.itemView.getContext();
                Intent intent = new Intent(context, TripDetailsActivity.class);
                intent.putExtra("tripId", tripId);
                context.startActivity(intent);
            }
        });

        holder.tripListClick.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Dialog deleteDialog = new Dialog(holder.itemView.getContext());

                deleteDialog.setContentView(R.layout.confirm_delete);
                deleteDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                CardView confirmCard = deleteDialog.findViewById(R.id.delete_cardD);
                deleteDialog.show();

                FrameLayout confirmButton = deleteDialog.findViewById(R.id.confirm_delete);
                FrameLayout cancelButton = deleteDialog.findViewById(R.id.cancel_delete);

                confirmButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteTripAndEntries(tripId, holder.getLayoutPosition());
                        deleteDialog.dismiss();
                    }
                });

                cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteDialog.dismiss();
                    }
                });

                return true;
            }
        });


    }


    private void deleteTripAndEntries(String tripKey, int position) {
        DatabaseReference tripsRef = FirebaseDatabase.getInstance().getReference().child("trips");

        tripsRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(tripKey).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    // Trip deleted successfully, now delete its entries
                    DatabaseReference entriesRef = FirebaseDatabase.getInstance().getReference().child("entries").child(tripKey);
                    entriesRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                tripNames.remove(position);
                                tripIds.remove(position);
                                notifyItemRemoved(position);
                                notifyItemRangeChanged(position, tripNames.size());
                            } else {
                                // Failed to delete entries
                            }
                        }
                    });
                } else {
                    // Failed to delete trip
                }
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