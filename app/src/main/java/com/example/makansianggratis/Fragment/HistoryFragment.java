    package com.example.makansianggratis.Fragment;

    import android.os.Bundle;
    import android.util.Log;
    import android.widget.Button;
    import android.widget.Toast;

    import androidx.annotation.NonNull;
    import androidx.annotation.Nullable;
    import androidx.fragment.app.Fragment;
    import androidx.recyclerview.widget.LinearLayoutManager;
    import androidx.recyclerview.widget.RecyclerView;

    import android.view.LayoutInflater;
    import android.view.View;
    import android.view.ViewGroup;

    import com.example.makansianggratis.Adapter.HistoryAdapter;
    import com.example.makansianggratis.Model.HistoryItem;
    import com.example.makansianggratis.R;
    import com.google.firebase.auth.FirebaseAuth;
    import com.google.firebase.database.DataSnapshot;
    import com.google.firebase.database.DatabaseError;
    import com.google.firebase.database.DatabaseReference;
    import com.google.firebase.database.FirebaseDatabase;
    import com.google.firebase.database.ValueEventListener;

    import java.text.ParseException;
    import java.text.SimpleDateFormat;
    import java.util.ArrayList;
    import java.util.Date;
    import java.util.List;
    import java.util.Locale;

    public class HistoryFragment extends Fragment {

        private RecyclerView rvHistory;
        private List<HistoryItem> historyList = new ArrayList<>();
        private HistoryAdapter adapter;

        private FirebaseAuth firebaseAuth;
        private DatabaseReference databaseReference;

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_history, container, false);

            rvHistory = view.findViewById(R.id.rvHistory);

            firebaseAuth = FirebaseAuth.getInstance();
            databaseReference = FirebaseDatabase.getInstance().getReference("Users");
            Button btnRefresh = view.findViewById(R.id.btnRefresh);

            btnRefresh.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    loadHistoryData();
                    Toast.makeText(getContext(),"Memuat Data", Toast.LENGTH_SHORT).show();
                }
            });

            setupRecyclerView();
            loadHistoryData();

            return view;
        }

        private void setupRecyclerView() {
            rvHistory.setLayoutManager(new LinearLayoutManager(getContext()));
            adapter = new HistoryAdapter(historyList);
            rvHistory.setAdapter(adapter);
        }

        private void loadHistoryData() {
            // Referensi ke node "Pinjaman" di Firebase
            databaseReference = FirebaseDatabase.getInstance().getReference("Pinjaman");
            String userId = firebaseAuth.getCurrentUser().getUid();

            // Cari berdasarkan User ID di dalam node Pinjaman
            databaseReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    historyList.clear(); // Hapus data lama sebelum memuat ulang

                    // Iterasi setiap kode unik pinjaman
                    for (DataSnapshot child : snapshot.getChildren()) {
                        String id = child.getKey();
                        String tanggalPinjam = child.child("tanggalPinjam").getValue(String.class);
                        String tanggalKembali = child.child("tanggalKembali").getValue(String.class);
                        String tanggalPengembalian = child.child("tanggalPengembalian").getValue(String.class);

                        // Perhitungan status
                        String status = calculateStatus(tanggalKembali, tanggalPengembalian);

                        // Tambahkan data ke dalam list
                        historyList.add(new HistoryItem(id, tanggalPinjam, tanggalKembali, status));
                    }

                    // Beritahu adapter bahwa data berubah
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("HistoryFragment", "Database Error: " + error.getMessage());
                    Toast.makeText(getContext(), "Gagal memuat data.", Toast.LENGTH_SHORT).show();
                }
            });
        }



        // Logika untuk menentukan status
        private String calculateStatus(String tanggalKembali, String tanggalPengembalian) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            try {
                Date returnDate = sdf.parse(tanggalKembali);
                Date currentDate = new Date();

                if (tanggalPengembalian != null) {
                    Date pengembalianDate = sdf.parse(tanggalPengembalian);
                    if (pengembalianDate.after(returnDate)) {
                        return "Sudah Dikembalikan (Telat)";
                    } else {
                        return "Sudah Dikembalikan";
                    }
                } else if (currentDate.after(returnDate)) {
                    return "Belum Dikembalikan (Telat)";
                } else {
                    return "Sedang Dipinjam";
                }
            } catch (ParseException e) {
                e.printStackTrace();
                return "Status Tidak Diketahui";
            }
        }

    }
