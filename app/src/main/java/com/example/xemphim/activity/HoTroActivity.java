package com.example.xemphim.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.xemphim.R;

public class HoTroActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 1; // Mã yêu cầu
    private ImageView imgUpload;
    private Bitmap selectedImageBitmap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ho_tro);
        imgUpload = findViewById(R.id.imgUpload);
        imgUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();
            }
        });
    }
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            // Hiển thị hình ảnh trên imgUpload
            imgUpload.setImageURI(imageUri);

            // Nếu cần, bạn cũng có thể chuyển đổi Uri thành Bitmap
            // try {
            //     selectedImageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            // } catch (IOException e) {
            //     e.printStackTrace();
            // }
        }
    }
}