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

import com.example.adventuretravellog.DiaryDetailsActivity;
import com.example.adventuretravellog.Model.DiaryEntry;
import com.example.adventuretravellog.R;
import com.example.adventuretravellog.TripDetailsActivity;

import java.util.List;

public class DiaryAdapter extends RecyclerView.Adapter<TripAdapter.TripViewHolder> {

    private List<DiaryEntry> diaryEntries;
    private String tripId;

    public DiaryAdapter(List<DiaryEntry> diaryEntries, String tripId) {
        this.diaryEntries = diaryEntries;
        this.tripId = tripId;
    }

    public void addDiaryEntry(String id, String mediaType, String mediaUrl, String text) {
        DiaryEntry diaryEntry = new DiaryEntry(id, mediaType, mediaUrl, text);
        diaryEntries.add(diaryEntry);
        notifyItemInserted(diaryEntries.size() - 1);
    }

    @NonNull
    @Override
    public TripAdapter.TripViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_trip, parent, false);
        return new TripAdapter.TripViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TripAdapter.TripViewHolder holder, int position) {
        DiaryEntry diaryEntry = diaryEntries.get(position);

        holder.textViewTripName.setText(diaryEntry.getText());

        holder.tripListClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the diary ID or any additional information here
                DiaryEntry diaryEntry = diaryEntries.get(holder.getAdapterPosition());
                String diaryId = diaryEntry.getId();

                Context context = holder.itemView.getContext();
                Intent intent = new Intent(context, DiaryDetailsActivity.class);
                intent.putExtra("diaryId", diaryId);
                intent.putExtra("tripId", tripId);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return diaryEntries.size();
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