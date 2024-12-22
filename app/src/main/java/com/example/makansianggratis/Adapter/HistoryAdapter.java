package com.example.makansianggratis.Adapter;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.makansianggratis.Model.HistoryItem;
import com.example.makansianggratis.R;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private final List<HistoryItem> historyList;

    public HistoryAdapter(List<HistoryItem> historyList) {
        this.historyList = historyList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.itemcard_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HistoryItem item = historyList.get(position);

        Log.d("HistoryAdapter", "Binding item: " + item.getId());

        holder.tvIdPeminjaman.setText(item.getId());
        holder.tvTanggalPeminjaman.setText(item.getTanggalPinjam());
        holder.tvTanggalKembali.setText(item.getTanggalKembali());
        holder.tvStatus.setText(item.getStatus());

        // Atur warna bullet berdasarkan status
        if (item.getStatus().contains("Sedang Dipinjam")) {
            holder.statusBullet.setBackgroundColor(Color.BLUE);
        } else if (item.getStatus().contains("Sudah Dikembalikan")) {
            holder.statusBullet.setBackgroundColor(Color.GREEN);
        } else if (item.getStatus().contains("Sudah Dikembalikan (Telat)")) {
            holder.statusBullet.setBackgroundColor(Color.YELLOW);
        }else if (item.getStatus().contains("Belum Dikembalikan (Telat)")) {
            holder.statusBullet.setBackgroundColor(Color.RED);
        }else {
            holder.statusBullet.setBackgroundColor(Color.GRAY);
        }
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvIdPeminjaman, tvTanggalPeminjaman, tvTanggalKembali, tvStatus;
        View statusBullet;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvIdPeminjaman = itemView.findViewById(R.id.tvCourseName);
            tvTanggalPeminjaman = itemView.findViewById(R.id.tvTanggalPinjam);
            tvTanggalKembali = itemView.findViewById(R.id.tvTanggalKembali);
            tvStatus = itemView.findViewById(R.id.tvTotalScore);
            statusBullet = itemView.findViewById(R.id.statusBullet);
        }
    }
}
