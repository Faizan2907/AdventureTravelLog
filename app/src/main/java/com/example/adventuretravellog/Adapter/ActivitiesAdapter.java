package com.example.adventuretravellog.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.adventuretravellog.R;

import java.util.List;

public class ActivitiesAdapter extends RecyclerView.Adapter<ActivitiesAdapter.ViewHolder> {
    private List<String> activitiesList;

    public ActivitiesAdapter(List<String> activitiesList) {
        this.activitiesList = activitiesList;
    }

    public void updateActivities(List<String> newActivitiesList) {
        activitiesList.clear();
        activitiesList.addAll(newActivitiesList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_activity, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String activityName = activitiesList.get(position);
        holder.bind(activityName);
    }

    @Override
    public int getItemCount() {
        return activitiesList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView activityNameTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            activityNameTextView = itemView.findViewById(R.id.activityNameTextView);
        }

        public void bind(String activityName) {
            activityNameTextView.setText(activityName);
        }
    }
}
