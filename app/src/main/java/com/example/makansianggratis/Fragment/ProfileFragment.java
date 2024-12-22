package com.example.makansianggratis.Fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.makansianggratis.R;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.bumptech.glide.Glide;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ProfileFragment extends Fragment {

    private ImageView ivProfilePicture;
    private TextView tvChangePicture;
    private EditText etUsername, etNIM, etEmail;
    private CheckBox cbAgreement;
    private Button btnSaveChanges;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    private static final String SUPABASE_URL = "https://zchyizwloiivkhezoefe.supabase.co";
    private static final String SUPABASE_BUCKET = "Profileimg";
    private static final String SUPABASE_API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InpjaHlpendsb2lpdmtoZXpvZWZlIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTczMjY4MTU0OSwiZXhwIjoyMDQ4MjU3NTQ5fQ.ppEEvtI37otlcEw3wF14T-0hOUn_j-TTi3Y_Dcbkjy4";

    private static final int REQUEST_IMAGE_PICK = 1;

    private Uri selectedImageUri;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Inisialisasi Views
        ivProfilePicture = view.findViewById(R.id.ivProfilePicture);
        tvChangePicture = view.findViewById(R.id.tvChangePicture);
        etUsername = view.findViewById(R.id.etUsername);
        etNIM = view.findViewById(R.id.etNIM);
        etEmail = view.findViewById(R.id.etEmail);
        cbAgreement = view.findViewById(R.id.cbAgreement);
        btnSaveChanges = view.findViewById(R.id.btnSaveChanges);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        // Disable tombol Save Changes saat awal
        btnSaveChanges.setEnabled(false);
        btnSaveChanges.setBackgroundColor(getResources().getColor(R.color.Firefly2));

        loadUserProfile();

        cbAgreement.setOnCheckedChangeListener((buttonView, isChecked) -> {
            btnSaveChanges.setEnabled(isChecked);
            if (isChecked) {
                btnSaveChanges.setBackgroundColor(getResources().getColor(R.color.Firefly4));
            } else {
                btnSaveChanges.setBackgroundColor(getResources().getColor(R.color.Firefly2));
            }
        });

        btnSaveChanges.setOnClickListener(v -> updateUserProfile());

        // Menggunakan Dhaval2404 ImagePicker untuk crop dan ambil gambar
        tvChangePicture.setOnClickListener(v -> {
            ImagePicker.with(this)
                    .cropSquare() // Crop dalam bentuk kotak
                    .compress(1024) // Maksimal ukuran file 1MB
                    .maxResultSize(512, 512) // Resolusi maksimal
                    .start();
        });

        return view;
    }

    private void uploadImage(Bitmap bitmap) {
        String userId = firebaseAuth.getCurrentUser().getUid();
        String fileName = "profile_" + userId + ".jpg";

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 25, baos);
            byte[] imageData = baos.toByteArray();

            // URL untuk menghapus file lama
            String deleteUrl = SUPABASE_URL + "/storage/v1/object/" + SUPABASE_BUCKET + "/" + fileName;

            // Hapus file lama terlebih dahulu
            Request deleteRequest = new Request.Builder()
                    .url(deleteUrl)
                    .addHeader("Authorization", "Bearer " + SUPABASE_API_KEY)
                    .addHeader("apikey", SUPABASE_API_KEY)
                    .delete()
                    .build();

            OkHttpClient client = new OkHttpClient();
            client.newCall(deleteRequest).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    // Jika gagal menghapus gambar lama, tetap lanjut mengunggah gambar baru
                    uploadNewImage(bitmap, fileName, imageData);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        // Jika berhasil menghapus gambar lama, lanjut unggah gambar baru
                        uploadNewImage(bitmap, fileName, imageData);
                    } else {
                        // Tetap unggah gambar baru meskipun penghapusan gagal
                        uploadNewImage(bitmap, fileName, imageData);
                    }
                }
            });

        } catch (Exception e) {
            requireActivity().runOnUiThread(() ->
                    Toast.makeText(getContext(), "Kesalahan saat memulai unggah: " + e.getMessage(), Toast.LENGTH_SHORT).show()
            );
        }
    }

    private void uploadNewImage(Bitmap bitmap, String fileName, byte[] imageData) {
        String uploadUrl = SUPABASE_URL + "/storage/v1/object/" + SUPABASE_BUCKET + "/" + fileName;

        RequestBody requestBody = RequestBody.create(MediaType.parse("image/jpeg"), imageData);
        Request request = new Request.Builder()
                .url(uploadUrl)
                .addHeader("Authorization", "Bearer " + SUPABASE_API_KEY)
                .addHeader("apikey", SUPABASE_API_KEY)
                .addHeader("x-upsert", "true") // Mengizinkan file ditimpa
                .post(requestBody)
                .build();

        new OkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Gagal mengunggah gambar baru: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String publicUrl = SUPABASE_URL + "/storage/v1/object/public/" + SUPABASE_BUCKET + "/" + fileName;
                    saveImageUrlToFirebase(publicUrl); // Simpan URL baru ke Firebase
                } else {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Gagal menyimpan gambar baru ke Supabase. Error: " + response.message(), Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }

    private void saveImageUrlToFirebase(String imageUrl) {
        String userId = firebaseAuth.getCurrentUser().getUid();

        // Reset URL lama dengan null sebelum mengganti
        databaseReference.child(userId).child("profilePicture").setValue(null)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Set URL baru setelah reset berhasil
                        databaseReference.child(userId).child("profilePicture").setValue(imageUrl)
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        requireActivity().runOnUiThread(() -> {
                                            Toast.makeText(getContext(), "Gambar berhasil diperbarui!", Toast.LENGTH_SHORT).show();
                                            loadUserProfile(); // Refresh data profil
                                        });
                                    } else {
                                        Toast.makeText(getContext(), "Gagal menyimpan URL gambar baru.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(getContext(), "Gagal mereset URL gambar lama.", Toast.LENGTH_SHORT).show();
                    }
                });
    }



    private void loadUserProfile() {
        String userId = firebaseAuth.getCurrentUser().getUid();
        databaseReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String username = snapshot.child("username").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    String nim = snapshot.child("nim").getValue(String.class);
                    String profilePicture = snapshot.child("profilePicture").getValue(String.class);

                    etUsername.setText(username);
                    etEmail.setText(email);
                    if (nim != null) {
                        etNIM.setText(nim);
                    }
                    if (profilePicture != null) {
                        Glide.with(getContext())
                                .load(profilePicture)
                                .into(ivProfilePicture);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Gagal memuat profil pengguna.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void updateUserProfile() {
        String userId = firebaseAuth.getCurrentUser().getUid();
        String newUsername = etUsername.getText().toString().trim();
        String newNIM = etNIM.getText().toString().trim();
        String newEmail = etEmail.getText().toString().trim();

        if (TextUtils.isEmpty(newUsername)) {
            etUsername.setError("Username tidak boleh kosong.");
            return;
        }
        if (TextUtils.isEmpty(newEmail)) {
            etEmail.setError("Email tidak boleh kosong.");
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("username", newUsername);
        updates.put("email", newEmail);
        if (!TextUtils.isEmpty(newNIM)) {
            updates.put("nim", newNIM);
        }

        databaseReference.child(userId).updateChildren(updates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getContext(), "Profil berhasil diperbarui!", Toast.LENGTH_SHORT).show();
                loadUserProfile();
            } else {
                Toast.makeText(getContext(), "Gagal memperbarui profil.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == getActivity().RESULT_OK && data != null) {
            Uri imageUri = data.getData(); // URI gambar yang sudah di-crop

            if (imageUri != null) {
                ivProfilePicture.setImageURI(imageUri); // Tampilkan di ImageView

                // Proses gambar dalam thread terpisah untuk mencegah UI freeze
                new Thread(() -> {
                    try {
                        Bitmap bitmap;
                        if (imageUri.getScheme().equals("content")) {
                            // Baca dari InputStream
                            try (InputStream inputStream = requireContext().getContentResolver().openInputStream(imageUri)) {
                                bitmap = BitmapFactory.decodeStream(inputStream);
                            }
                        } else {
                            // URI dalam format file
                            bitmap = MediaStore.Images.Media.getBitmap(
                                    requireContext().getContentResolver(), imageUri);
                        }

                        // Pastikan Bitmap berhasil diambil
                        if (bitmap != null) {
                            requireActivity().runOnUiThread(() -> ivProfilePicture.setImageBitmap(bitmap));
                            uploadImage(bitmap); // Unggah ke Supabase
                        } else {
                            requireActivity().runOnUiThread(() ->
                                    Toast.makeText(getContext(), "Gagal memproses gambar.", Toast.LENGTH_SHORT).show()
                            );
                        }
                    } catch (IOException e) {
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(getContext(), "Gagal memuat gambar: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                        );
                    }
                }).start();
            } else {
                Toast.makeText(getContext(), "URI gambar tidak valid.", Toast.LENGTH_SHORT).show();
            }
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(getContext(), ImagePicker.getError(data), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Gambar tidak dipilih.", Toast.LENGTH_SHORT).show();
        }
    }
}