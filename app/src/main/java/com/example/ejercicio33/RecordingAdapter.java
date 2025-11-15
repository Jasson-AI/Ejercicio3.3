package com.example.ejercicio33;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RecordingAdapter extends RecyclerView.Adapter<RecordingAdapter.RecViewHolder> {
    private List<AudioRecording> items;
    private OnItemActionListener listener;
    public interface OnItemActionListener {
        void onPlay(AudioRecording rec);
        void onDelete(AudioRecording rec);
    }
    public RecordingAdapter(List<AudioRecording> items, OnItemActionListener listener) {
        this.items = items;
        this.listener = listener;
    }
    public static class RecViewHolder extends RecyclerView.ViewHolder {
        TextView txtName, txtDuration, txtDate;
        ImageButton btnPlay, btnDelete;
        public RecViewHolder(View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtItemName);
            txtDuration = itemView.findViewById(R.id.txtItemDuration);
            txtDate = itemView.findViewById(R.id.txtItemDate);
            btnPlay = itemView.findViewById(R.id.btnItemPlay);
            btnDelete = itemView.findViewById(R.id.btnItemDelete);
        }
    }
    @Override
    public RecViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recording, parent, false);
        return new RecViewHolder(v);
    }
    @Override
    public void onBindViewHolder(RecViewHolder holder, int position) {
        AudioRecording rec = items.get(position);
        holder.txtName.setText(rec.name);
        holder.txtDuration.setText(formatDuration(rec.durationSeconds));
        holder.txtDate.setText(rec.recordDate);
        holder.btnPlay.setOnClickListener(v -> listener.onPlay(rec));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(rec));
    }
    @Override
    public int getItemCount() { return items.size(); }
    private String formatDuration(int seconds) {
        int m = seconds / 60; int s = seconds % 60;
        return String.format("%02d:%02d", m, s);
    }
}
