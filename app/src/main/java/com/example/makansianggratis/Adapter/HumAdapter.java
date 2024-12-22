package com.example.makansianggratis.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.makansianggratis.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class HumAdapter extends RecyclerView.Adapter<HumAdapter.ViewHolder> {

    private final List<String> imageUrls; // Daftar URL gambar dari Supabase

    public HumAdapter(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_fachome, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String imageUrl = imageUrls.get(position);

        // Memuat gambar dari URL ke dalam ImageView menggunakan Picasso
        Picasso.get()
                .load(imageUrl)
                .placeholder(R.drawable.dswertype)
                .error(R.drawable.dswertype)
                .fit() // Sesuaikan ukuran gambar dengan ImageView
                .centerCrop() // Memotong gambar agar pas ke dalam ImageView
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.item_image); // ID untuk ImageView di layout item
        }
    }
}
