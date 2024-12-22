package com.example.makansianggratis.Fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.makansianggratis.Activity.PengembalianActivity;
import com.example.makansianggratis.Activity.PinjamActivity;
import com.example.makansianggratis.Adapter.HumAdapter;
import com.example.makansianggratis.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView rvFirst, rvSecond;
    private LinearLayout btnPinjam, btnKembali, btnTeat;
    private TextView txtUser;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private DatabaseReference databaseReference;

    public HomeFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Firebase initialization
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        txtUser = view.findViewById(R.id.username_nav);
        rvFirst = view.findViewById(R.id.rv_home_first);
        rvSecond = view.findViewById(R.id.rv_home_second);
        btnPinjam = view.findViewById(R.id.button_peminjaman);
        btnKembali = view.findViewById(R.id.button_pengembalian);
        btnTeat = view.findViewById(R.id.button_teater);

        setupUserProfile(); // Mengambil username dari Realtime Database
        setupButton(); // Setup tombol untuk navigasi
        setupProfileImage(); // Memuat gambar profil dari Realtime Database
        setupRecyclerView(); // Menggunakan Firestore untuk RecyclerView
    }

    private void setupUserProfile() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();

            // Ambil username dari Firebase Realtime Database
            databaseReference.child(userId).addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String username = dataSnapshot.child("username").getValue(String.class);
                        if (username != null && !username.isEmpty()) {
                            txtUser.setText(username); // Tampilkan username pada UI
                        } else {
                            txtUser.setText("Pengguna");
                        }
                    } else {
                        txtUser.setText("Pengguna");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    txtUser.setText("Pengguna");
                    Toast.makeText(getContext(), "Gagal memuat username: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            txtUser.setText("Guest");
        }
    }

    private void setupButton() {
        btnPinjam.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), PinjamActivity.class);
            startActivity(intent);
        });
        btnKembali.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), PengembalianActivity.class);
            startActivity(intent);
        });
        btnTeat.setOnClickListener(v ->
                Toast.makeText(getContext(), "Fitur Belum Selesai", Toast.LENGTH_SHORT).show());
    }

    private void setupRecyclerView() {
        // RecyclerView untuk branches
        setupBranchRecyclerView();

        // RecyclerView untuk categories
        setupCategoryRecyclerView();
    }

    private void setupProfileImage() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();

            // Ambil link profilePicture dari Firebase Realtime Database
            databaseReference.child(userId).addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String profilePictureUrl = dataSnapshot.child("profilePicture").getValue(String.class);
                        if (profilePictureUrl != null && !profilePictureUrl.isEmpty()) {
                            // Gunakan Glide untuk memuat gambar ke dalam ImageView
                            Glide.with(requireContext())
                                    .load(profilePictureUrl)
                                    .placeholder(R.drawable.placeholderimg) // Gambar sementara saat memuat
                                    .error(R.drawable.placeholderimg) // Gambar jika terjadi error
                                    .into((de.hdodenhof.circleimageview.CircleImageView) requireView().findViewById(R.id.profile_image));
                        } else {
                            Toast.makeText(getContext(), "Foto profil tidak ditemukan.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(getContext(), "Gagal memuat foto profil: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


    private void setupBranchRecyclerView() {
        List<String> branchImages = new ArrayList<>();

        firebaseFirestore.collection("branches")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String imageUrl = document.getString("img_url"); // Gambar dari Supabase
                        if (imageUrl != null) {
                            branchImages.add(imageUrl);
                        }
                    }

                    rvFirst.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
                    rvFirst.setAdapter(new HumAdapter(branchImages)); // Gunakan HumAdapter
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to load branches: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void setupCategoryRecyclerView() {
        List<String> categoryImages = new ArrayList<>();

        firebaseFirestore.collection("collection")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String imageUrl = document.getString("img_url"); // Gambar dari Supabase
                        if (imageUrl != null) {
                            categoryImages.add(imageUrl);
                        }
                    }

                    rvSecond.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
                    rvSecond.setAdapter(new HumAdapter(categoryImages)); // Gunakan HumAdapter
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to load categories: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
