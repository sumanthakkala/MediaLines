package com.sumanthakkala.medialines.activities;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.sumanthakkala.medialines.R;
import com.sumanthakkala.medialines.adapters.NoteImagesAdapter;
import com.sumanthakkala.medialines.database.MediaLinesDatabase;
import com.sumanthakkala.medialines.entities.Attachments;
import com.sumanthakkala.medialines.entities.Note;
import com.sumanthakkala.medialines.entities.NoteWithData;
import com.sumanthakkala.medialines.viewmodels.NoteImageViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class CreateNoteActivity extends AppCompatActivity implements  OnRequestPermissionsResultCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int STORAGE_PERMISSION_REQUEST_CODE = 2;

    private EditText noteTitle, noteText;
    private TextView textDateTime;
    private View noteColorIndicator;
    private ViewPager2 imagesViewPager;
    private FusedLocationProviderClient locationClient;
    private TextView webUrlTV;
    private LinearLayout webUrlLayout;
    private AlertDialog addUrlDialog;
    private String currentLocationLatLong;
    private String noteDateTime;
    private String selectedNoteColor;

    private List<NoteImageViewModel> totalImages;
    private List<NoteImageViewModel> selectedImages;
    private NoteImagesAdapter noteImagesAdapter;

    private static final int REQUEST_CODE_STORAGE_PERMISSION = 1;
    private static final int REQUEST_CODE_SELECT_IMAGE = 2;
    private static final String IMAGE_TYPE = "image";
    private static final String AUDIO_TYPE = "audio";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note);

        ImageView imageBack = findViewById(R.id.imageBack);
        imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        ImageView imageDone = findViewById(R.id.imageSaveNote);
        imageDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveNote();
            }
        });

        noteTitle = findViewById(R.id.inputNoteTitle);
        noteText = findViewById(R.id.inputNoteText);
        textDateTime = findViewById(R.id.textDateTime);
        noteColorIndicator = findViewById(R.id.viewInfoIndicatior);
        imagesViewPager = findViewById(R.id.imagesViewPager);
        webUrlTV = findViewById(R.id.webUrlText);
        webUrlLayout = findViewById(R.id.webUrlLayout);

        noteDateTime = new SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm a", Locale.getDefault())
                .format(new Date());
        textDateTime.setText(
                noteDateTime
        );
        locationClient = LocationServices.getFusedLocationProviderClient(this);

        //Default note color
        selectedNoteColor = "#333333";
        selectedImages = new ArrayList<>();
        totalImages = new ArrayList<>();

        noteImagesAdapter = new NoteImagesAdapter(totalImages);
        imagesViewPager.setAdapter(noteImagesAdapter);
        getCurrentLocation();
        initMoreOptions();
        setNoteIndicatorColor();

    }

    @Override
    public void onBackPressed() {
        for (NoteImageViewModel attachment: selectedImages){
            File attachmentFile = new File(getApplicationContext().getExternalFilesDir(null),attachment.imageUniqueFileName);
            attachmentFile.delete();
        }
        super.onBackPressed();
    }

    private void saveNote() {
        if (noteTitle.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Title cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        final Note note = new Note();
        note.setTitle(noteTitle.getText().toString());
        note.setNoteText(noteText.getText().toString());
        note.setDateTime(textDateTime.getText().toString());
        note.setCreatedLocation(currentLocationLatLong);
        note.setColor(selectedNoteColor);

        if(webUrlLayout.getVisibility() == View.VISIBLE){
            note.setWebLink(webUrlTV.getText().toString());
        }

        final NoteWithData noteWithData = new NoteWithData();

        @SuppressLint("StaticFieldLeak")
        class SaveNoteTask extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {
                long noteId = MediaLinesDatabase.getMediaLinesDatabase(getApplicationContext()).noteDao().insertNote(note);
                note.setNoteId(noteId);
                List<Attachments> attachments = new ArrayList<>();
                for (NoteImageViewModel imageViewModel : selectedImages){
                    final Attachments attachment = new Attachments();
                    attachment.setAssociatedNoteId(noteId);
                    attachment.setAttachmentUniqueFileName(imageViewModel.imageUniqueFileName);
                    attachment.setAttachmentType(IMAGE_TYPE);
                    attachment.setDateTime(noteDateTime);
                    attachment.setAttachmentId(
                            MediaLinesDatabase.getMediaLinesDatabase(getApplicationContext()).attachmentsDao().insertAttachment(attachment)
                    );
                    attachments.add(attachment);
                }
                noteWithData.attachments = attachments;
                noteWithData.note = note;
                noteWithData.editedLocations = new ArrayList<>();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Intent intent = new Intent();
                intent.putExtra("addedNote", noteWithData);
                setResult(RESULT_OK, intent);
                finish();
            }
        }

        new SaveNoteTask().execute();
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            Task<Location> task = locationClient.getLastLocation();
            task.addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if(location != null){
                        currentLocationLatLong = String.valueOf(location.getLatitude()) + " " + String.valueOf(location.getLongitude());
                        System.out.println(currentLocationLatLong);
                    }
                }
            });
            return;
        }
        else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            Task<Location> task = locationClient.getLastLocation();
            task.addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if(location != null){
                        currentLocationLatLong = String.valueOf(location.getLatitude()) + " " + String.valueOf(location.getLongitude());
                        System.out.println(currentLocationLatLong);
                    }
                }
            });
            return;
        }

        if(requestCode == REQUEST_CODE_STORAGE_PERMISSION && grantResults.length > 0){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectImageHandler();
            }
            else {
                Toast.makeText(this,"Permission Denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initMoreOptions(){
        final LinearLayout moreOptionsLayout = findViewById(R.id.moreOptionsLayout);
        final BottomSheetBehavior<LinearLayout> bottomSheetBehavior = BottomSheetBehavior.from(moreOptionsLayout);
        moreOptionsLayout.findViewById(R.id.textMoreOptions).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED){
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
                else{
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }
        });

        final ImageView imageColor1 = moreOptionsLayout.findViewById(R.id.imageForColor1);
        final ImageView imageColor2 = moreOptionsLayout.findViewById(R.id.imageForColor2);
        final ImageView imageColor3 = moreOptionsLayout.findViewById(R.id.imageForColor3);
        final ImageView imageColor4 = moreOptionsLayout.findViewById(R.id.imageForColor4);
        final ImageView imageColor5 = moreOptionsLayout.findViewById(R.id.imageForColor5);

        moreOptionsLayout.findViewById(R.id.viewColor1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedNoteColor = getResources().getString(R.color.colorDefaultNoteColor);
                imageColor1.setImageResource(R.drawable.ic_done);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(0);
                setNoteIndicatorColor();
            }
        });

        moreOptionsLayout.findViewById(R.id.viewColor2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedNoteColor = getResources().getString(R.color.colorNoteColor2);
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(R.drawable.ic_done);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(0);
                setNoteIndicatorColor();
            }
        });

        moreOptionsLayout.findViewById(R.id.viewColor3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedNoteColor = getResources().getString(R.color.colorNoteColor3);
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(R.drawable.ic_done);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(0);
                setNoteIndicatorColor();
            }
        });

        moreOptionsLayout.findViewById(R.id.viewColor4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedNoteColor = getResources().getString(R.color.colorNoteColor4);
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(R.drawable.ic_done);
                imageColor5.setImageResource(0);
                setNoteIndicatorColor();
            }
        });

        moreOptionsLayout.findViewById(R.id.viewColor5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedNoteColor = getResources().getString(R.color.colorNoteColor5);
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(R.drawable.ic_done);
                setNoteIndicatorColor();
            }
        });

        moreOptionsLayout.findViewById(R.id.addImageLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                if(ContextCompat.checkSelfPermission(
                        getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(
                            CreateNoteActivity.this,
                            new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},
                            REQUEST_CODE_STORAGE_PERMISSION
                    );
                }
                else {
                    selectImageHandler();
                }
            }
        });

        moreOptionsLayout.findViewById(R.id.addUrlLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                showAddUrlDialig();
            }
        });
    }

    private void setNoteIndicatorColor(){
        GradientDrawable gradientDrawable = (GradientDrawable) noteColorIndicator.getBackground();
        gradientDrawable.setColor(Color.parseColor(selectedNoteColor));
    }

    private void selectImageHandler(){
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if(intent.resolveActivity(getPackageManager()) != null){
            startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK){
            if( data != null){

                try {
                    if(isExternalStorageWritable() && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){

                        String fileName = generateUUID() + textDateTime.getText().toString();
                        File attachmentFile = new File(getApplicationContext().getExternalFilesDir(null),fileName);
                        FileOutputStream fos = new FileOutputStream(attachmentFile);
                        fos.write(byteArrayFromUri(data.getData()));
                        fos.close();
                        NoteImageViewModel noteImageViewModel = new NoteImageViewModel();
                        noteImageViewModel.imageUniqueFileName = fileName;
                        noteImageViewModel.index = totalImages.size();
                        totalImages.add(noteImageViewModel);
                        selectedImages.add(noteImageViewModel);
                        noteImagesAdapter.notifyItemChanged(totalImages.size() - 1);
                    }
                    else {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_REQUEST_CODE);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        }
    }

    private byte[] byteArrayFromUri(Uri contentUri) throws IOException {
        InputStream iStream =   getContentResolver().openInputStream(contentUri);
        byte[] inputData = getBytes(iStream);
        return inputData;
    }
    public byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }
    public String generateUUID() throws Exception{
        return UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
    }


    // Checks if a volume containing external storage is available
// for read and write.
    private boolean isExternalStorageWritable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    // Checks if a volume containing external storage is available to at least read.
    private boolean isExternalStorageReadable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(Environment.getExternalStorageState());
    }

    private void showAddUrlDialig(){
        if(addUrlDialog == null){
            AlertDialog.Builder builder = new AlertDialog.Builder(CreateNoteActivity.this);
            View view = LayoutInflater.from(this).inflate(
                    R.layout.add_url_layout,
                    (ViewGroup) findViewById(R.id.addUrlContainerLayout)
            );
            builder.setView(view);
            addUrlDialog = builder.create();

            if(addUrlDialog.getWindow() != null){
                addUrlDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }

            final EditText inputURL = view.findViewById(R.id.inputURL);
            inputURL.requestFocus();

            view.findViewById(R.id.addURLTV).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(inputURL.getText().toString().trim().isEmpty()){
                        Toast.makeText(CreateNoteActivity.this, "Enter URL", Toast.LENGTH_SHORT).show();
                    }
                    else if(!Patterns.WEB_URL.matcher(inputURL.getText().toString()).matches()){
                        Toast.makeText(CreateNoteActivity.this, "Enter Valid URL", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        webUrlTV.setText(inputURL.getText().toString());
                        webUrlLayout.setVisibility(View.VISIBLE);
                        addUrlDialog.dismiss();
                    }
                }
            });

            view.findViewById(R.id.cancelAddUrlTV).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    addUrlDialog.dismiss();
                }
            });
        }
        addUrlDialog.show();
    }
}