package com.example.nihit.blog;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.Random;

public class SetupActivity extends AppCompatActivity {

    private static final int GALLERY_REQUEST = 1 ;
    private static final int MAX_LENGTH = 10;
    private EditText mName;
    private ImageButton mImage;
    private Button mSubmit;

    private ProgressDialog mProgress;

    private Uri mImageUri;

    private StorageReference mStorage;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        mStorage = FirebaseStorage.getInstance().getReference().child("Profile_images");
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mAuth = FirebaseAuth.getInstance();

        mProgress = new ProgressDialog(this);

        mName = (EditText) findViewById(R.id.setupNameField);
        mImage = (ImageButton) findViewById(R.id.setupImageBtn);
        mSubmit = (Button) findViewById(R.id.setupSubmitBtn);

        mImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_REQUEST);

            }
        });

        mSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String name = mName.getText().toString().trim();

                if(!TextUtils.isEmpty(name) && mImageUri != null) {

                    mProgress.setMessage("Setting up your account...");
                    mProgress.show();

                    final String uid = mAuth.getCurrentUser().getUid();

                    StorageReference filepath = mStorage.child(random());

                    filepath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            Uri downloadUrl = taskSnapshot.getDownloadUrl();

                            DatabaseReference current_user_db = mDatabase.child(uid);

                            current_user_db.child("name").setValue(name);
                            current_user_db.child("image").setValue(downloadUrl.toString().trim());

                            mProgress.dismiss();

                         /*   Intent mainIntent = new Intent(SetupActivity.this, MainActivity.class);
                            mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(mainIntent);
                           */

                        }
                    });


                }
                else
                {
                    Toast.makeText(SetupActivity.this,"fill all details...",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public String random() {

        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(MAX_LENGTH);
        char tempChar;
        for(int i = 0; i < randomLength; i++)
        {
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_REQUEST && resultCode == RESULT_OK){

             mImageUri = data.getData();


            CropImage.activity(mImageUri)
                    .setAspectRatio(1,1)
             //       .setCropShape(CropImageView.CropShape.OVAL)
                    .setFixAspectRatio(true)
                    .setSnapRadius(5)
                    .start(this);

        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
               mImageUri = result.getUri();
                mImage.setImageURI(mImageUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

    }
}
