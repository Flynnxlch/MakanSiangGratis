package com.example.makansianggratis.Activity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.makansianggratis.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PinjamActivity extends AppCompatActivity {

    private EditText etTanggalPeminjaman, etTanggalPengembalian, etNama, etEmail;
    private TextView tvStatusGambar;
    private Spinner spinnerFakultas, spinnerJenis;
    private Button btnAmbilGambar, btnPilihTanggalPeminjaman, btnPilihTanggalPengembalian, btnPinjam;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private Uri photoUri; // Untuk URI gambar lokal
    private String uploadedImageUrl; // Untuk URL hasil upload gambar

    private final Map<String, String[]> jenisMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pinjam);

        // Firebase setup
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        // Initialize UI elements
        etTanggalPeminjaman = findViewById(R.id.etTanggalPeminjaman);
        etTanggalPengembalian = findViewById(R.id.etTanggalPengembalian);
        etNama = findViewById(R.id.etNama);
        etEmail = findViewById(R.id.etEmail);
        tvStatusGambar = findViewById(R.id.tvStatusGambar);
        spinnerFakultas = findViewById(R.id.spinnerFakultas);
        spinnerJenis = findViewById(R.id.spinnerJenis);
        btnAmbilGambar = findViewById(R.id.btnAmbilGambar);
        btnPilihTanggalPeminjaman = findViewById(R.id.btnPilihTanggalPeminjaman);
        btnPilihTanggalPengembalian = findViewById(R.id.btnPilihTanggalPengembalian);
        btnPinjam = findViewById(R.id.btnPinjam);

        setupUserInfo();
        setupSpinners();
        setupButtonListeners();
    }

    private void setupUserInfo() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            etEmail.setText(user.getEmail());
            databaseReference.child(user.getUid()).child("username").get().addOnSuccessListener(dataSnapshot -> {
                if (dataSnapshot.exists()) {
                    etNama.setText(dataSnapshot.getValue(String.class));
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Gagal mengambil data nama: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void setupSpinners() {
        setupJenisMap();
        String[] fakultasList = {"Fakultas Sains Dan Teknologi -FST-", "Fakultas Syariah & Hukum -FSH-", "Fakultas Ilmu Tarbiyah dan Keguruan -FITK-"};
        ArrayAdapter<String> fakultasAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, fakultasList);
        fakultasAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFakultas.setAdapter(fakultasAdapter);
        updateJenisSpinner(fakultasList[0]);

        spinnerFakultas.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                updateJenisSpinner(fakultasList[position]);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
    }

    private void setupButtonListeners() {
        btnAmbilGambar.setOnClickListener(v -> {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(intent, 100);
            } else {
                Toast.makeText(this, "Kamera tidak tersedia", Toast.LENGTH_SHORT).show();
            }
        });

        btnPilihTanggalPeminjaman.setOnClickListener(v -> showDatePickerDialog(etTanggalPeminjaman, null));
        btnPilihTanggalPengembalian.setOnClickListener(v -> showDatePickerDialog(etTanggalPengembalian, etTanggalPeminjaman));

        btnPinjam.setOnClickListener(v -> handlePinjamAction());
    }

    private void handlePinjamAction() {
        String nama = etNama.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String fakultas = spinnerFakultas.getSelectedItem().toString();
        String jenis = spinnerJenis.getSelectedItem().toString();
        String tanggalPinjam = etTanggalPeminjaman.getText().toString().trim();
        String tanggalKembali = etTanggalPengembalian.getText().toString().trim();

        if (nama.isEmpty() || email.isEmpty() || tanggalPinjam.isEmpty() || tanggalKembali.isEmpty() || uploadedImageUrl == null) {
            Toast.makeText(this, "Harap lengkapi semua data dan unggah gambar!", Toast.LENGTH_SHORT).show();
            return;
        }

        saveDataToFirebase(nama, email, fakultas, jenis, tanggalPinjam, tanggalKembali, generateKodeUnik(), uploadedImageUrl);
    }

    private void saveDataToFirebase(String nama, String email, String fakultas, String jenis, String tanggalPinjam, String tanggalKembali, String kodeUnik, String imageUrl) {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Gagal: User tidak ditemukan!", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference pinjamanRef = FirebaseDatabase.getInstance().getReference("Pinjaman").child(user.getUid());
        Map<String, Object> pinjamanData = new HashMap<>();
        pinjamanData.put("nama", nama);
        pinjamanData.put("email", email);
        pinjamanData.put("fakultas", fakultas);
        pinjamanData.put("jenis", jenis);
        pinjamanData.put("tanggalPinjam", tanggalPinjam);
        pinjamanData.put("tanggalKembali", tanggalKembali);
        pinjamanData.put("imageUrl", imageUrl);
        pinjamanData.put("status", "Dipinjam");

        pinjamanRef.child(kodeUnik).setValue(pinjamanData).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                showSuccessDialog(); // Tampilkan popup jika berhasil
            } else {
                Toast.makeText(this, "Gagal menyimpan data: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showSuccessDialog() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Peminjaman Berhasil")
                .setMessage("Data peminjaman telah berhasil disimpan.")
                .setPositiveButton("OK", (dialog, which) -> {
                    clearForm(); // Hapus data dari form
                    navigateToHomeFragment(); // Kembali ke HomeFragment
                })
                .show();
    }

    private void clearForm() {
        etNama.setText("");
        etEmail.setText("");
        etTanggalPeminjaman.setText("");
        etTanggalPengembalian.setText("");
        spinnerFakultas.setSelection(0); // Atur spinner ke posisi awal
        updateJenisSpinner(spinnerFakultas.getSelectedItem().toString());
        tvStatusGambar.setText("Belum ada gambar diunggah");
        uploadedImageUrl = null; // Reset URL gambar
    }

    private void navigateToHomeFragment() {
        Intent intent = new Intent(this, MainActivity.class); // Pastikan MainActivity mengatur HomeFragment sebagai default
        intent.putExtra("navigateTo", "HomeFragment"); // Opsi ini opsional jika diperlukan untuk navigasi
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish(); // Hentikan aktivitas saat ini
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            Bundle extras = data.getExtras();
            if (extras != null) {
                Bitmap bitmap = (Bitmap) extras.get("data");
                if (bitmap != null) {
                    tvStatusGambar.setText("Gambar berhasil diambil");
                    uploadBitmapToSupabase(bitmap, generateKodeUnik(), (success, imageUrl) -> {
                        if (success) {
                            uploadedImageUrl = imageUrl; // Simpan URL gambar hasil upload
                            Toast.makeText(this, "Gambar berhasil diunggah ke Supabase", Toast.LENGTH_SHORT).show();
                        } else {
                            tvStatusGambar.setText("Gagal mengunggah gambar");
                        }
                    });
                }
            }
        }
    }

    private void uploadBitmapToSupabase(Bitmap bitmap, String kodeUnik, UploadCallback callback) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 25, baos);
            byte[] imageData = baos.toByteArray();

            String supabaseUrl = "https://zchyizwloiivkhezoefe.supabase.co";
            String bucketName = "MakanSiangGratisSto";
            String supabaseApiKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InpjaHlpendsb2lpdmtoZXpvZWZlIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTczMjY4MTU0OSwiZXhwIjoyMDQ4MjU3NTQ5fQ.ppEEvtI37otlcEw3wF14T-0hOUn_j-TTi3Y_Dcbkjy4";

            String uploadUrl = supabaseUrl + "/storage/v1/object/" + bucketName + "/" + kodeUnik + ".jpg";
            RequestBody requestBody = RequestBody.create(MediaType.parse("image/jpeg"), imageData);
            Request request = new Request.Builder()
                    .url(uploadUrl)
                    .addHeader("Authorization", "Bearer " + supabaseApiKey)
                    .post(requestBody)
                    .build();

            new OkHttpClient().newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> callback.onComplete(false, null));
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        runOnUiThread(() -> callback.onComplete(true, uploadUrl));
                    } else {
                        runOnUiThread(() -> callback.onComplete(false, null));
                    }
                }
            });
        } catch (Exception e) {
            callback.onComplete(false, null);
        }
    }

    private void showDatePickerDialog(EditText targetField, EditText relatedField) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 2); // Tambahkan 2 hari ke tanggal saat ini

        // Cek apakah ini untuk memilih tanggal pengembalian
        long minDate;
        if (relatedField != null && !relatedField.getText().toString().isEmpty()) {
            // Jika tanggal peminjaman sudah diisi, gunakan tanggal tersebut sebagai minimum
            String[] dateParts = relatedField.getText().toString().split("/");
            int day = Integer.parseInt(dateParts[0]);
            int month = Integer.parseInt(dateParts[1]) - 1; // Bulan dimulai dari 0
            int year = Integer.parseInt(dateParts[2]);
            Calendar relatedDate = Calendar.getInstance();
            relatedDate.set(year, month, day);
            relatedDate.add(Calendar.DAY_OF_MONTH, 1); // Tanggal pengembalian harus setelah tanggal peminjaman
            minDate = relatedDate.getTimeInMillis();
        } else {
            // Default untuk tanggal peminjaman
            minDate = calendar.getTimeInMillis();
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String date = dayOfMonth + "/" + (month + 1) + "/" + year;
            targetField.setText(date);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.getDatePicker().setMinDate(minDate); // Atur minimum tanggal
        datePickerDialog.show();
    }

    private String generateKodeUnik() {
        return String.valueOf(System.currentTimeMillis());
    }

    private void setupJenisMap() {
        jenisMap.put("Fakultas Sains Dan Teknologi -FST-", new String[]{"Proyektor", "Tabel Periodik" , "Spidol" , "Kursi bermeja" , "Kabel HDMI" , "HDMI to TypeC port", "Tangga" , "Water Blower"});
        jenisMap.put("Fakultas Syariah & Hukum -FSH-", new String[]{"Jurnal Kajian Hukum Islam" , "Spidol" , "Kursi bermeja" , "Kabel HDMI" , "HDMI to TypeC port"});
        jenisMap.put("Fakultas Ilmu Tarbiyah dan Keguruan -FITK-", new String[]{"Buku Pendidikan" , "Spidol" , "Kursi bermeja" , "Kabel HDMI" , "HDMI to TypeC port"});
    }

    private void updateJenisSpinner(String fakultas) {
        String[] jenisList = jenisMap.getOrDefault(fakultas, new String[]{});
        ArrayAdapter<String> jenisAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, jenisList);
        jenisAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerJenis.setAdapter(jenisAdapter);
    }

    private interface UploadCallback {
        void onComplete(boolean success, String imageUrl);
    }
}
