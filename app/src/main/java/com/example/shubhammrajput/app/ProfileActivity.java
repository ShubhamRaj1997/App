package com.example.shubhammrajput.app;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.Images.Media;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask.TaskSnapshot;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity implements OnClickListener {
    private static final int PICK_IMAGE_REQUEST = 234;
    private Button buttonLogout;
    private EditText dobText;
    private Uri filepath;
    private FirebaseAuth firebaseAuth;
    private Button getInfo;
    private Button loadPic;
    private StorageReference mStorageRef;
    private DocumentReference mdocRef;
    private EditText nameText;
    private EditText phoneText;
    private ImageView profileView;
    private Button saveButton;
    private TextView textDob;
    private TextView textName;
    private TextView textPhone;
    private TextView textViewUserEmail;
    private Button uploadButton;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView((int) R.layout.activity_profile);
        this.mdocRef = FirebaseFirestore.getInstance().collection("sampleData").document("info");
        this.mStorageRef = FirebaseStorage.getInstance().getReference();
        this.firebaseAuth = FirebaseAuth.getInstance();
        if (this.firebaseAuth.getCurrentUser() == null) {
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        }
        FirebaseUser user = this.firebaseAuth.getCurrentUser();
        this.textViewUserEmail = (TextView) findViewById(R.id.textViewUserEmail);
        this.buttonLogout = (Button) findViewById(R.id.buttonLogout);
        this.textViewUserEmail.setText("Welcome " + user.getEmail());
        this.saveButton = (Button) findViewById(R.id.saveButton);
        this.getInfo = (Button) findViewById(R.id.getInfo);
        this.buttonLogout.setOnClickListener(this);
        this.saveButton.setOnClickListener(this);
        this.getInfo.setOnClickListener(this);
        this.uploadButton = (Button) findViewById(R.id.uploadButton);
        this.uploadButton.setOnClickListener(this);
        this.nameText = (EditText) findViewById(R.id.nameText);
        this.phoneText = (EditText) findViewById(R.id.phoneText);
        this.dobText = (EditText) findViewById(R.id.DobText);
        this.textDob = (TextView) findViewById(R.id.textDob);
        this.textName = (TextView) findViewById(R.id.textName);
        this.textPhone = (TextView) findViewById(R.id.textPhone);
        this.loadPic = (Button) findViewById(R.id.loadButton);
        this.loadPic.setOnClickListener(this);
        this.mdocRef = FirebaseFirestore.getInstance().document("sampleData/"+textViewUserEmail.getText().toString().trim());
        this.profileView = (ImageView) findViewById(R.id.profileView);
    }

    private void saveInfo() {
        String Name = this.nameText.getText().toString().trim();
        String Phone = this.phoneText.getText().toString().trim();
        String Dob = this.dobText.getText().toString().trim();
        if (Name.isEmpty() || Phone.isEmpty() || Dob.isEmpty()) {
            Toast.makeText(this, "Please enter the information ", Toast.LENGTH_SHORT).show();
            return;
        }
        Map data = new HashMap();
        data.put("name", Name);
        data.put(PhoneAuthProvider.PROVIDER_ID, Phone);
        data.put("dob", Dob);
        Log.i(PhoneAuthProvider.PROVIDER_ID, Phone);
        this.mdocRef.set(data).addOnCompleteListener((Activity) this, new OnCompleteListener<Void>() {
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.i("here", "reached inside");
                    Toast.makeText(ProfileActivity.this.getApplicationContext(), "Information has been saved! ",Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(ProfileActivity.this.getApplicationContext(), "Information couldn't be saved!,Try again! ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showInfo() {
        this.mdocRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    String naam = documentSnapshot.getString("name");
                    String fon = documentSnapshot.getString(PhoneAuthProvider.PROVIDER_ID);
                    ProfileActivity.this.textDob.setText(documentSnapshot.getString("dob"));
                    ProfileActivity.this.textName.setText(naam);
                    ProfileActivity.this.textPhone.setText(fon);
                }
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == -1 && data != null && data.getData() != null) {
            this.filepath = data.getData();
            try {
                this.profileView.setImageBitmap(Media.getBitmap(getContentResolver(), this.filepath));
                if (this.filepath != null) {
                    final ProgressDialog progressDialog = new ProgressDialog(this);
                    progressDialog.setTitle("Uploading...");
                    progressDialog.show();
                    this.mStorageRef.child(this.textViewUserEmail.getText().toString().trim() + "/profile.jpg").putFile(this.filepath).addOnSuccessListener(new OnSuccessListener<TaskSnapshot>() {
                        public void onSuccess(TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(ProfileActivity.this.getApplicationContext(), "File Uploaded !", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        public void onFailure(@NonNull Exception exception) {
                            progressDialog.dismiss();
                            Toast.makeText(ProfileActivity.this.getApplicationContext(), exception.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }).addOnProgressListener(new OnProgressListener<TaskSnapshot>() {
                        public void onProgress(TaskSnapshot taskSnapshot) {
                            progressDialog.setMessage(((int) ((100.0d * ((double) taskSnapshot.getBytesTransferred())) / ((double) taskSnapshot.getTotalByteCount()))) + "% uploaded...");
                        }
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void startUpload() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction("android.intent.action.GET_CONTENT");
        startActivityForResult(Intent.createChooser(intent, "Select a pic"), PICK_IMAGE_REQUEST);
    }

    private void load() throws IOException {
        final File localFile = File.createTempFile("images", "jpg");
        this.mStorageRef.child(this.textViewUserEmail.getText().toString().trim() + "/profile.jpg").getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                ProfileActivity.this.profileView.setImageBitmap(BitmapFactory.decodeFile(localFile.getAbsolutePath()));
            }
        }).addOnFailureListener(new OnFailureListener() {
            public void onFailure(@NonNull Exception exception) {
            }
        });
    }

    public void onClick(View view) {
        if (view == this.buttonLogout) {
            this.firebaseAuth.signOut();
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        }
        if (view == this.saveButton) {
            saveInfo();
        }
        if (view == this.getInfo) {
            showInfo();
        }
        if (view == this.uploadButton) {
            startUpload();
        }
        if (view == this.loadPic) {
            try {
                load();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}