package com.example.siddu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.icu.text.CaseMap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class NewNoteActivity extends AppCompatActivity {
private Button saveButton;
private String content, title;
private EditText noteTitle, noteNote;
    private Uri ImageUri;
private ImageView backButton, ImageSaveButton;
    private static final int GalleryPick=1;

  private String  saveCurrentDate,saveCurrentTime;
private TextView noteText;
    private String productRandomKey,downloadImageUrl;

    private StorageReference ProductImageRef;
    private DatabaseReference ProductsRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_note);

        ProductImageRef= FirebaseStorage.getInstance().getReference().child("Notes Images");
        ProductsRef= FirebaseDatabase.getInstance().getReference().child("Notes");

ImageSaveButton=(ImageView) findViewById(R.id.imageSave);
ImageSaveButton.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        openGallery();
    }
});
        saveButton=(Button)findViewById(R.id.btnCreate);
        noteTitle=(EditText)findViewById(R.id.inputNoteTitle);
        noteNote=(EditText)findViewById(R.id.inputNote);
        backButton=(ImageView)findViewById(R.id.imageBACK);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(NewNoteActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        noteText=(TextView)findViewById(R.id.inNote);
        saveButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
validateProduct();
            }
        });
    }

    private void openGallery() {
        Intent galleryIntent=new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,GalleryPick);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==GalleryPick && resultCode==RESULT_OK && data!=null){
            ImageUri=data.getData();
            ImageSaveButton.setImageURI(ImageUri);
        }
    }
private void validateProduct(){
    title= noteTitle.getText().toString().trim();
    content =noteNote.getText().toString().trim();
    if(!TextUtils.isEmpty(title)&& !TextUtils.isEmpty(content)){

        createNote();
    }else{
        Toast.makeText(NewNoteActivity.this, "Write a Note  ",Toast.LENGTH_SHORT).show();

    }
}
    private void createNote(){

        Calendar calendar=Calendar.getInstance();
        SimpleDateFormat currentDate=new SimpleDateFormat("MMM dd,yyy");
         saveCurrentDate = currentDate.format(calendar.getTime());
        SimpleDateFormat currentTime=new SimpleDateFormat("HH:mm:ss a");
         saveCurrentTime = currentTime.format(calendar.getTime());
        productRandomKey=saveCurrentDate+saveCurrentTime;
        final StorageReference filepath=ProductImageRef.child(ImageUri.getLastPathSegment()+productRandomKey+".jpg");
        final UploadTask uploadTask=filepath.putFile(ImageUri);


        uploadTask.addOnFailureListener(
                new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        String message=e.toString();

                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                Task<Uri>urlTask=uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception
                    {
                        if(!task.isSuccessful()){
                            throw task.getException();
                        }
                        downloadImageUrl=filepath.getDownloadUrl().toString();
                        return filepath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task)
                    {
                        if(task.isSuccessful()){
                            downloadImageUrl=task.getResult().toString();

                            saveProductInfoToDAtabase();

                        }
                    }
                });
            }
        });
    }



    private void saveProductInfoToDAtabase() {
        HashMap<String,Object> productMap=new HashMap<>();
        productMap.put("pid",productRandomKey);
        productMap.put("date",saveCurrentDate);
        productMap.put("image",downloadImageUrl);
        productMap.put("time",saveCurrentTime);
        productMap.put("notetitle",title);
        productMap.put("note",content);
        ProductsRef.child(productRandomKey).updateChildren(productMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            Intent intent=new Intent(NewNoteActivity.this,MainActivity.class);
                            startActivity(intent);

                            Toast.makeText(NewNoteActivity.this,"Note Added.....",Toast.LENGTH_SHORT).show();
                        }
                        else {

                            String message=task.getException().toString();
                            Toast.makeText(NewNoteActivity.this,"Error"+message,Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

}
