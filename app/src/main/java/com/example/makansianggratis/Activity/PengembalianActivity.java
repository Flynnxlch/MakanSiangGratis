package com.example.makansianggratis.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

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

public class PengembalianActivity extends AppCompatActivity {

    private Spinner spinnerPilihFasilitas;
    private TextView tvNamaPeminjam, tvTanggalPeminjaman, tvTanggalPengembalian;
    private Button btnKembalikan;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    private List<String> pinjamanIds = new ArrayList<>();
    private String selectedPinjamanId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pengembalian);

        // Inisialisasi View
        spinnerPilihFasilitas = findViewById(R.id.spinnerPilihFasilitas);
        tvNamaPeminjam = findViewById(R.id.tvNamaPeminjam);
        tvTanggalPeminjaman = findViewById(R.id.tvTanggalPeminjaman);
        tvTanggalPengembalian = findViewById(R.id.tvTanggalPengembalian);
        btnKembalikan = findViewById(R.id.btnKembalikan);

        // Firebase setup
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        loadPinjamanIds();

        // Set listener untuk Spinner
        spinnerPilihFasilitas.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedPinjamanId = pinjamanIds.get(position);
                loadPinjamanDetails(selectedPinjamanId);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedPinjamanId = null;
            }
        });


        // Set listener untuk Tombol Kembalikan
        btnKembalikan.setOnClickListener(v -> handlePengembalian());
    }

    private void loadPinjamanIds() {
        String userId = firebaseAuth.getCurrentUser().getUid(); // Ambil ID user
        DatabaseReference pinjamanRef = FirebaseDatabase.getInstance().getReference("Pinjaman").child(userId);

        pinjamanRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                pinjamanIds.clear();

                if (!snapshot.exists()) {
                    Toast.makeText(PengembalianActivity.this, "Tidak ada pinjaman ditemukan.", Toast.LENGTH_SHORT).show();
                    return;
                }

                for (DataSnapshot child : snapshot.getChildren()) {
                    String pinjamanId = child.getKey(); // Ambil kode unik (ID pinjaman)
                    String tanggalPengembalian = child.child("tanggalPengembalian").getValue(String.class);

                    if (pinjamanId != null && tanggalPengembalian == null) { // Hanya ambil pinjaman yang belum dikembalikan
                        pinjamanIds.add(pinjamanId);
                    }
                }

                if (pinjamanIds.isEmpty()) {
                    Toast.makeText(PengembalianActivity.this, "Tidak ada ID pinjaman yang tersedia untuk pengembalian.", Toast.LENGTH_SHORT).show();
                } else {
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(PengembalianActivity.this,
                            android.R.layout.simple_spinner_item, pinjamanIds);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerPilihFasilitas.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PengembalianActivity.this, "Gagal memuat data.", Toast.LENGTH_SHORT).show();
            }
        });
    }




    private void loadPinjamanDetails(String pinjamanId) {
        String userId = firebaseAuth.getCurrentUser().getUid();
        FirebaseDatabase.getInstance().getReference("Pinjaman").child(userId).child(pinjamanId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String namaPeminjam = snapshot.child("nama").getValue(String.class);
                        String tanggalPinjam = snapshot.child("tanggalPinjam").getValue(String.class);
                        String tanggalKembali = snapshot.child("tanggalKembali").getValue(String.class);

                        tvNamaPeminjam.setText(namaPeminjam != null ? namaPeminjam : "Tidak tersedia");
                        tvTanggalPeminjaman.setText(tanggalPinjam != null ? tanggalPinjam : "Tidak tersedia");
                        tvTanggalPengembalian.setText(tanggalKembali != null ? tanggalKembali : "Tidak tersedia");

                        // Perbarui status otomatis jika pinjaman belum dikembalikan dan sudah melewati tanggal kembali
                        if (snapshot.child("tanggalPengembalian").getValue(String.class) == null && tanggalKembali != null) {
                            try {
                                Date returnDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(tanggalKembali);
                                Date currentDate = new Date();

                                if (currentDate.after(returnDate)) {
                                    // Kondisi 3: Belum dikembalikan, sudah melewati tanggal kembali
                                    snapshot.getRef().child("status").setValue("Belum dikembalikan (Telat)");
                                } else {
                                    snapshot.getRef().child("status").setValue("Dipinjam");
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(PengembalianActivity.this, "Gagal memuat detail pinjaman.", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void handlePengembalian() {
        String tanggalKembali = tvTanggalPengembalian.getText().toString(); // Tanggal kembali yang diambil dari Firebase
        String tanggalPengembalian = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date()); // Tanggal saat ini (pengembalian)

        // Update ke Firebase
        String userId = firebaseAuth.getCurrentUser().getUid();
        DatabaseReference pinjamanRef = FirebaseDatabase.getInstance().getReference("Pinjaman").child(userId).child(selectedPinjamanId);

        // Periksa dan update status berdasarkan kondisi
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        try {
            Date returnDate = sdf.parse(tanggalKembali); // Tanggal Kembali yang seharusnya
            Date pengembalianDate = sdf.parse(tanggalPengembalian); // Tanggal Pengembalian saat ini

            if (pengembalianDate.before(returnDate) || pengembalianDate.equals(returnDate)) {
                // Kondisi 1: Dikembalikan tepat waktu
                pinjamanRef.child("tanggalPengembalian").setValue(tanggalPengembalian);
                pinjamanRef.child("status").setValue("Dikembalikan").addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Pengembalian berhasil dicatat. Status: Dikembalikan.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Gagal mencatat pengembalian.", Toast.LENGTH_SHORT).show();
                    }
                });
            } else if (pengembalianDate.after(returnDate)) {
                // Kondisi 2: Dikembalikan terlambat
                pinjamanRef.child("tanggalPengembalian").setValue(tanggalPengembalian);
                pinjamanRef.child("status").setValue("Telat Dikembalikan").addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Pengembalian berhasil dicatat. Status: Telat Dikembalikan.", Toast.LENGTH_SHORT).show();
                        showWarningDialog();
                    } else {
                        Toast.makeText(this, "Gagal mencatat pengembalian.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } catch (ParseException e) {
            e.printStackTrace();
            Toast.makeText(this, "Terjadi kesalahan dalam memproses tanggal.", Toast.LENGTH_SHORT).show();
        }

        // Refresh spinner setelah pengembalian
        loadPinjamanIds();
    }


    private void showWarningDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pengembalian Terlambat")
                .setMessage("Anda telah melewati tanggal pengembalian. \n\n" +
                        "Isi pesan ini dapat disesuaikan untuk memberi informasi lebih detail.")
                .setPositiveButton("OK", (dialog, which) -> {
                    // Tambahkan tindakan jika perlu
                    dialog.dismiss();
                })
                .setCancelable(false)
                .show();
    }
}
