package com.example.makansianggratis.Adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.makansianggratis.Model.CC;
import com.example.makansianggratis.R;

import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {

    private final List<CC> contactList;
    private final Context context;

    public ContactAdapter(Context context, List<CC> contactList) {
        this.context = context;
        this.contactList = contactList;
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.itemcard_contacts, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        CC contact = contactList.get(position);

        // Bind data to UI components
        holder.tvName.setText(contact.getName());
        holder.tvEmail.setText(contact.getEmail());
        holder.tvPhone.setText(contact.getPhone());

        // Set click listener on the item view
        holder.itemView.setOnClickListener(v -> {
            String phoneNumber = contact.getPhone();
            if (phoneNumber != null && !phoneNumber.isEmpty()) {
                // Create an Intent to dial the phone number
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + phoneNumber));
                try {
                    context.startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(context, "Unable to open dialer", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context, "Phone number is invalid", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    static class ContactViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail, tvPhone;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvPhone = itemView.findViewById(R.id.tvPhone);
        }
    }
}
