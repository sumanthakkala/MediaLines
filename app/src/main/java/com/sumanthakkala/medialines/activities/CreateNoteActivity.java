package com.sumanthakkala.medialines.activities;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.exifinterface.media.ExifInterface;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.location.Location;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.util.Patterns;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;
import com.sumanthakkala.medialines.R;
import com.sumanthakkala.medialines.adapters.NoteAudiosAdapter;
import com.sumanthakkala.medialines.adapters.NoteImagesAdapter;
import com.sumanthakkala.medialines.constants.Constants;
import com.sumanthakkala.medialines.database.MediaLinesDatabase;
import com.sumanthakkala.medialines.entities.Attachments;
import com.sumanthakkala.medialines.entities.EditedLocations;
import com.sumanthakkala.medialines.entities.Note;
import com.sumanthakkala.medialines.entities.NoteWithData;
import com.sumanthakkala.medialines.listeners.NoteAudiosListener;
import com.sumanthakkala.medialines.listeners.NoteImagesListener;
import com.sumanthakkala.medialines.viewmodels.NoteAudioViewModel;
import com.sumanthakkala.medialines.viewmodels.NoteImageViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class CreateNoteActivity extends AppCompatActivity implements  OnRequestPermissionsResultCallback, NoteImagesListener, NoteAudiosListener, OnMapReadyCallback {

    private static final int REQUEST_CODE_LOCATION_PERMISSION = 1;
    private static final int REQUEST_CODE_RECORD_AUDIO_PERMISSION = 2;
    private static final int REQUEST_CODE_CAMERA_PERMISSION = 3;
    private static final int REQUEST_CODE_READ_EXTERNAL_STORAGE_PERMISSION = 4;
    private static final int REQUEST_CODE_SELECT_IMAGE = 5;
    private static final int REQUEST_CODE_SPEECH_INPUT = 6;
    private static final int REQUEST_CODE_CAPTURE_IMAGE = 7;

    private EditText noteTitle, noteText;
    private ImageView imageDone;
    private ImageView imageUnArchive;
    private ImageView imageArchive;
    private TextView textDateTime;
    private View noteColorIndicator;
    private ViewPager2 imagesViewPager;
    private RecyclerView audiosRecyclerView;
    private TextView imagePositionIndicator;
    private FusedLocationProviderClient locationClient;
    private TextView webUrlTV;
    private LinearLayout webUrlLayout;
    private AlertDialog addUrlDialog;
    private AlertDialog recordAudioDialog;
    private AlertDialog showImageDialog;
    private AlertDialog exitDialog;
    private String currentLocationLatLong;
    private String currentDateTime;
    private String selectedNoteColor;
    private GoogleMap map;
    private BottomSheetBehavior<LinearLayout> bottomSheetBehavior;
    private BottomSheetBehavior<LinearLayout> infoSheetBehavior;

    private Boolean isExistingNote = false;
    private MediaRecorder mediaRecorder;

    private boolean isRecording = false;

    private String currentTempPhotoPath = "";


    private List<NoteImageViewModel> totalImages;
    private List<NoteImageViewModel> selectedImages;
    private List<NoteImageViewModel> imagesToDeleteFromDB = new ArrayList<>();
    private List<NoteImageViewModel> existingimagesInImageViewModel = new ArrayList<>();


    private List<NoteAudioViewModel> totalAudios = new ArrayList<>();
    private List<NoteAudioViewModel> newAudios = new ArrayList<>();
    private List<NoteAudioViewModel> audiosToDeleteFromDB = new ArrayList<>();
    private List<NoteAudioViewModel> existingAudiosInAudioViewModel = new ArrayList<>();


    private List<Attachments> existingImageAttachments = new ArrayList<>();
    private List<Attachments> existingAudioAttachments = new ArrayList<>();
    private NoteImagesAdapter noteImagesAdapter;
    private NoteAudiosAdapter noteAudiosAdapter;

    private NoteWithData existingNoteWithData;
    private int existingNotePosition;
    private LinearLayout moreOptionsLayout;
    private LinearLayout infoSheetLayout;
    private TextView imagesCountTV;
    private TextView audiosCountTV;





    private static final String IMAGE_TYPE = "image";
    private static final String AUDIO_TYPE = "audio";

    ViewPager2.OnPageChangeCallback pageChangeCallback = new ViewPager2.OnPageChangeCallback() {
        @Override
        public void onPageSelected(int position) {
            super.onPageSelected(position);
            imagePositionIndicator.setText("" + (position + 1) + "/" + totalImages.size());
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            imagePositionIndicator.setText("" + (imagesViewPager.getCurrentItem() + 1) + "/" + totalImages.size());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note);
        moreOptionsLayout = findViewById(R.id.moreOptionsLayout);
        initMoreOptions();

        infoSheetLayout = findViewById(R.id.infoSheetLayout);
        imagesCountTV = infoSheetLayout.findViewById(R.id.imageAttachmentsCountTV);
        audiosCountTV = infoSheetLayout.findViewById(R.id.audioAttachmentsCountTV);

        ImageView imageBack = findViewById(R.id.imageBack);
        imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        imageDone = findViewById(R.id.imageSaveNote);
        imageDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveNote();
            }
        });

        imageUnArchive = findViewById(R.id.imageUnArchiveNote);
        imageUnArchive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                unArchiveNote();
            }
        });

        imageArchive = findViewById(R.id.imageArchiveNote);
        imageArchive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                archiveNote();
            }
        });

        noteTitle = findViewById(R.id.inputNoteTitle);
        noteText = findViewById(R.id.inputNoteText);
        textDateTime = findViewById(R.id.textDateTime);
        noteColorIndicator = findViewById(R.id.viewInfoIndicatior);
        imagesViewPager = findViewById(R.id.imagesViewPager);
        audiosRecyclerView = findViewById(R.id.audiosRecyclerView);
        audiosRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        webUrlTV = findViewById(R.id.webUrlText);
        webUrlLayout = findViewById(R.id.webUrlLayout);
        imagePositionIndicator = findViewById(R.id.positionIndicatorInViewPager);

        currentDateTime = new SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm a", Locale.getDefault())
                .format(new Date());
        textDateTime.setText(
                currentDateTime
        );
        locationClient = LocationServices.getFusedLocationProviderClient(this);

        //Default note color
        selectedNoteColor = "#333333";
        selectedImages = new ArrayList<>();
        totalImages = new ArrayList<>();
        noteImagesAdapter = new NoteImagesAdapter(totalImages, this);
        imagesViewPager.setAdapter(noteImagesAdapter);
        noteAudiosAdapter = new NoteAudiosAdapter(totalAudios, this);
        audiosRecyclerView.setAdapter(noteAudiosAdapter);

        setNoteIndicatorColor();

        if(getIntent().getBooleanExtra("isViewOrUpdate", false)){
            isExistingNote = true;
            existingNoteWithData = (NoteWithData) getIntent().getSerializableExtra("noteData");
            existingNotePosition = (int) getIntent().getIntExtra("position", -1);
            if(existingNoteWithData.note.getIsActive() == Constants.IS_ACTIVE){
                imageArchive.setVisibility(View.VISIBLE);
            }
            else {
                imageUnArchive.setVisibility(View.VISIBLE);
                imageDone.setVisibility(View.GONE);
            }
            initInfoSheet();
            setExistingNoteData();
        }

        if(getIntent().getBooleanExtra("isFromQuickActions", false)){
            String type = getIntent().getStringExtra("quickActionsType");
            if(type != null && type.equals("image")){
                NoteImageViewModel imageViewModel = new NoteImageViewModel();
                imageViewModel.index = 0;
                imageViewModel.imageUniqueFileName = getIntent().getStringExtra("imageUniqueFileName");
                selectedImages.add(imageViewModel);
                totalImages.add(imageViewModel);
                noteImagesAdapter.notifyDataSetChanged();
            }

            if(type != null && type.equals("addUrl")){
                webUrlTV.setText(getIntent().getStringExtra("url"));
                webUrlLayout.setVisibility(View.VISIBLE);
            }

            if(type != null && type.equals("transcribe")){
                noteText.setText(getIntent().getStringExtra("transcribedText"));
            }

        }

        findViewById(R.id.removeWebUrlImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                webUrlTV.setText(null);
                webUrlLayout.setVisibility(View.GONE);
            }
        });
        imagesViewPager.registerOnPageChangeCallback(pageChangeCallback);
        if(totalImages.size() > 0){
            imagePositionIndicator.setText("" + (imagesViewPager.getCurrentItem() + 1) + "/" + totalImages.size());
        }
        else {
            imagePositionIndicator.setVisibility(View.GONE);
        }
        getCurrentLocation();
    }

    private void archiveNote() {
        @SuppressLint("StaticFieldLeak")
        class ArchiveNoteTask extends AsyncTask<Void, Void, Void> {
            @Override
            protected Void doInBackground(Void... voids) {
                List<Long> noteIds = new ArrayList<>();
                noteIds.add(existingNoteWithData.note.getNoteId());
                MediaLinesDatabase.getMediaLinesDatabase(getApplicationContext()).noteDao().archiveNotesWithId(noteIds);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Intent intent = new Intent();
                intent.putExtra("note", existingNoteWithData);
                if(isExistingNote && existingNotePosition != -1){
                    intent.putExtra("isNoteArchivedOrUnArchived", true);
                    intent.putExtra("position", existingNotePosition);
                }
                setResult(RESULT_OK, intent);
                finish();
            }
        }
        new ArchiveNoteTask().execute();
    }

    private void unArchiveNote(){
        @SuppressLint("StaticFieldLeak")
        class UnArchiveNoteTask extends AsyncTask<Void, Void, Void> {
            @Override
            protected Void doInBackground(Void... voids) {
                List<Long> noteIds = new ArrayList<>();
                noteIds.add(existingNoteWithData.note.getNoteId());
                MediaLinesDatabase.getMediaLinesDatabase(getApplicationContext()).noteDao().unArchiveNotesWithId(noteIds);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Intent intent = new Intent();
                intent.putExtra("note", existingNoteWithData);
                if(isExistingNote && existingNotePosition != -1){
                    intent.putExtra("isNoteArchivedOrUnArchived", true);
                    intent.putExtra("position", existingNotePosition);
                }
                setResult(RESULT_OK, intent);
                finish();
            }
        }
        new UnArchiveNoteTask().execute();
    }

    @Override
    public void onImageCLicked(String uniqueImageName, int position) {
        showClickedImage(uniqueImageName, position);
    }

    @Override
    public void onDeleteImageCLicked(NoteImageViewModel imageModel) {
        int removedImageIndex = totalImages.indexOf(imageModel);
        if(existingimagesInImageViewModel.remove(imageModel)){
            imagesToDeleteFromDB.add(imageModel);
        }
        totalImages.remove(imageModel);
        selectedImages.remove(imageModel);

        noteImagesAdapter.notifyItemRemoved(removedImageIndex);
        noteImagesAdapter.notifyItemRangeChanged(removedImageIndex, totalImages.size());

        imagePositionIndicator.setText("" + (removedImageIndex + 1) + "/" + totalImages.size());
    }

    @Override
    public void onPlayPauseCLicked(String uniqueAudioName, int position) {

    }

    @Override
    public void onDeleteAudioCLicked(NoteAudioViewModel audioViewModel) {
        int removedImageIndex = totalAudios.indexOf(audioViewModel);
        if(existingAudiosInAudioViewModel.remove(audioViewModel)){
            audiosToDeleteFromDB.add(audioViewModel);
        }
        totalAudios.remove(audioViewModel);
        newAudios.remove(audioViewModel);

        noteAudiosAdapter.notifyItemRemoved(removedImageIndex);
        noteAudiosAdapter.notifyItemRangeChanged(removedImageIndex, totalAudios.size());
    }

    private void setExistingNoteData(){

        if(isExistingNote){
            findViewById(R.id.infoNoteOptionLayout).setVisibility(View.VISIBLE);
            findViewById(R.id.infoNoteOptionLayout).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    infoSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
            });
        }
        else {
            findViewById(R.id.infoNoteOptionLayout).setVisibility(View.GONE);
        }

        if(existingNoteWithData.note.getIsActive() == Constants.IS_ARCHIVE){
            imageDone.setVisibility(View.GONE);
            bottomSheetBehavior.setPeekHeight(0);
            infoSheetBehavior.setPeekHeight((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics()));
            imageDone.setVisibility(View.GONE);
            imageUnArchive.setVisibility(View.VISIBLE);
        }

        noteTitle.setText(existingNoteWithData.note.getTitle());
        textDateTime.setText(existingNoteWithData.note.getDateTime());
        noteText.setText(existingNoteWithData.note.getNoteText());
        if(existingNoteWithData.note.getWebLink() != null && !existingNoteWithData.note.getWebLink().trim().isEmpty()){
            webUrlTV.setText(existingNoteWithData.note.getWebLink());
            webUrlLayout.setVisibility(View.VISIBLE);
        }
        if(existingNoteWithData != null && existingNoteWithData.note.getColor() != null && !existingNoteWithData.note.getColor().trim().toString().isEmpty()){
            switch (existingNoteWithData.note.getColor()){
                case "#fffdbe38":
                    moreOptionsLayout.findViewById(R.id.viewColor2).performClick();
                    break;
                case "#ffff4842":
                    moreOptionsLayout.findViewById(R.id.viewColor3).performClick();
                    break;
                case "#ff3a52fc":
                    moreOptionsLayout.findViewById(R.id.viewColor4).performClick();
                    break;
                case "#ff000000":
                    moreOptionsLayout.findViewById(R.id.viewColor5).performClick();
                    break;
            }
        }
        int imageIndex = 0;
        int audioIndex = 0;
        for(Attachments attach: existingNoteWithData.attachments){
            if(attach.getAttachmentType().equals("image")){
                existingImageAttachments.add(attach);
                NoteImageViewModel image = new NoteImageViewModel();
                image.imageUniqueFileName = attach.getAttachmentUniqueFileName();
                image.index = imageIndex;
                imageIndex += 1;
                totalImages.add(image);
                existingimagesInImageViewModel.add(image);
                noteImagesAdapter.notifyDataSetChanged();
            }
            else {
                existingAudioAttachments.add(attach);
                NoteAudioViewModel audio = new NoteAudioViewModel();
                audio.audioUniqueFileName = attach.getAttachmentUniqueFileName();
                audio.index = audioIndex;
                audioIndex += 1;
                totalAudios.add(audio);
                existingAudiosInAudioViewModel.add(audio);
                noteAudiosAdapter.notifyDataSetChanged();
            }
        }
        if(existingNoteWithData != null && existingimagesInImageViewModel != null && existingAudiosInAudioViewModel != null){
            imagesCountTV.setText("Images: " + existingimagesInImageViewModel.size());
            audiosCountTV.setText("Recordings: " + existingAudiosInAudioViewModel.size());
        }

    }

    @Override
    public void onBackPressed() {
        if(bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED){
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
        else if(isExistingNote && (infoSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) && (existingNoteWithData.note.getIsActive() == Constants.IS_ACTIVE)){
            infoSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
        else if(isExistingNote && (infoSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) && (existingNoteWithData.note.getIsActive() == Constants.IS_ARCHIVE)){
            infoSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
        else {
            if(isExistingNote){
                finish();
                super.onBackPressed();
            }
            else {
                showExitDialog();
            }
        }

    }

    private void showExitDialog(){
        if(exitDialog == null){
            AlertDialog.Builder builder = new AlertDialog.Builder(CreateNoteActivity.this);
            View view = LayoutInflater.from(this).inflate(
                    R.layout.on_back_pressed,
                    (ViewGroup) findViewById(R.id.onBackPressedDialogContainer)
            );
            builder.setView(view);
            exitDialog = builder.create();

            if(exitDialog.getWindow() != null){
                exitDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }
            view.findViewById(R.id.yesGoBackTV).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(noteAudiosAdapter.isPlaying){
                        noteAudiosAdapter.stopAudioPlayback();
                    }
                    for (NoteImageViewModel attachment: selectedImages){
                        File attachmentFile = new File(getApplicationContext().getExternalFilesDir(null),attachment.imageUniqueFileName);
                        attachmentFile.delete();
                    }
                    for (NoteAudioViewModel attachment: newAudios){
                        File attachmentFile = new File(getApplicationContext().getExternalFilesDir(null),attachment.audioUniqueFileName);
                        attachmentFile.delete();
                    }
                    finish();
                }
            });

            view.findViewById(R.id.cancelGoBackTV).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    exitDialog.dismiss();
                }
            });
        }
        exitDialog.show();
    }
    private void saveNote() {
        if (noteTitle.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Title cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if(noteAudiosAdapter.isPlaying){
            noteAudiosAdapter.stopAudioPlayback();
        }

        final Note note = new Note();
        note.setTitle(noteTitle.getText().toString());
        note.setNoteText(noteText.getText().toString());
        note.setDateTime(textDateTime.getText().toString());
        note.setCreatedLocation(currentLocationLatLong);
        note.setIsActive(Constants.IS_ACTIVE);
        note.setIsBookmarked(0);
        note.setColor(selectedNoteColor);

        if(webUrlLayout.getVisibility() == View.VISIBLE){
            note.setWebLink(webUrlTV.getText().toString());
        }

        if(isExistingNote){
            note.setNoteId(existingNoteWithData.note.getNoteId());
            note.setCreatedLocation(existingNoteWithData.note.getCreatedLocation());
            note.setDateTime(existingNoteWithData.note.getDateTime());
            note.setIsBookmarked(existingNoteWithData.note.getIsBookmarked());
        }



        @SuppressLint("StaticFieldLeak")
        class SaveNoteTask extends AsyncTask<Void, Void, Void> {

            NoteWithData noteWithData = new NoteWithData();
            @Override
            protected Void doInBackground(Void... voids) {
                if(isExistingNote){
                    MediaLinesDatabase.getMediaLinesDatabase(getApplicationContext()).noteDao().updateNote(note);
                }
                else {
                    long noteId = MediaLinesDatabase.getMediaLinesDatabase(getApplicationContext()).noteDao().insertNote(note);
                    note.setNoteId(noteId);
                }

                // Saving newly added image attachments
                for (NoteImageViewModel imageViewModel : selectedImages){
                    final Attachments attachment = new Attachments();
                    attachment.setAssociatedNoteId(note.getNoteId());
                    attachment.setAttachmentUniqueFileName(imageViewModel.imageUniqueFileName);
                    attachment.setAttachmentType(IMAGE_TYPE);
                    attachment.setDateTime(currentDateTime);
                    attachment.setAttachmentId(
                            MediaLinesDatabase.getMediaLinesDatabase(getApplicationContext()).attachmentsDao().insertAttachment(attachment)
                    );
                    existingImageAttachments.add(attachment);
                }

                // Saving newly added audio attachments
                for (NoteAudioViewModel audioViewModel : newAudios){
                    final Attachments attachment = new Attachments();
                    attachment.setAssociatedNoteId(note.getNoteId());
                    attachment.setAttachmentUniqueFileName(audioViewModel.audioUniqueFileName);
                    attachment.setAttachmentType(AUDIO_TYPE);
                    attachment.setDateTime(currentDateTime);
                    attachment.setAttachmentId(
                            MediaLinesDatabase.getMediaLinesDatabase(getApplicationContext()).attachmentsDao().insertAttachment(attachment)
                    );
                    existingAudioAttachments.add(attachment);
                }

                // Saving edited location
                final EditedLocations editedLocation = new EditedLocations();
                editedLocation.setAssociatedNoteId(note.getNoteId());
                editedLocation.setDateTime(currentDateTime);
                editedLocation.setLocation(currentLocationLatLong);
                MediaLinesDatabase.getMediaLinesDatabase(getApplicationContext()).editedLocationsDao().insertEditedLocation(editedLocation);

                //Deleting existing images from DB & App storage if any
                if(imagesToDeleteFromDB.size() > 0){
                    for(NoteImageViewModel deleteImageModel: imagesToDeleteFromDB){
                        //Delete from DB
                        MediaLinesDatabase.getMediaLinesDatabase(getApplicationContext()).attachmentsDao().deleteAttachmentByUniqueFileName(deleteImageModel.imageUniqueFileName);

                        //Delete from app storage
                        File attachmentFile = new File(getApplicationContext().getExternalFilesDir(null), deleteImageModel.imageUniqueFileName);
                        attachmentFile.delete();
                    }

                }

                //Deleting existing audios from DB & App storage if any
                if(audiosToDeleteFromDB.size() > 0){
                    for(NoteAudioViewModel deleteAudioModel: audiosToDeleteFromDB){
                        //Delete from DB
                        MediaLinesDatabase.getMediaLinesDatabase(getApplicationContext()).attachmentsDao().deleteAttachmentByUniqueFileName(deleteAudioModel.audioUniqueFileName);

                        //Delete from app storage
                        File attachmentFile = new File(getApplicationContext().getExternalFilesDir(null), deleteAudioModel.audioUniqueFileName);
                        attachmentFile.delete();
                    }

                }
                noteWithData = MediaLinesDatabase.getMediaLinesDatabase(getApplicationContext()).noteDao().getNoteWithDataByNoteId(note.getNoteId());
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Intent intent = new Intent();
                intent.putExtra("note", noteWithData);
                if(isExistingNote && existingNotePosition != -1){
                    intent.putExtra("isNoteUpdated", true);
                    intent.putExtra("position", existingNotePosition);
                }
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
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_LOCATION_PERMISSION);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_CODE_LOCATION_PERMISSION && grantResults.length > 0){
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
        }


        if(requestCode == REQUEST_CODE_READ_EXTERNAL_STORAGE_PERMISSION && grantResults.length > 0){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectImageHandler();
            }
            else {
                Toast.makeText(this,"Permission Denied!", Toast.LENGTH_SHORT).show();
            }
        }

        if(requestCode == REQUEST_CODE_RECORD_AUDIO_PERMISSION && grantResults.length > 0){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showRecordAudioDialog();
            }
            else {
                Toast.makeText(this,"Permission Denied!", Toast.LENGTH_SHORT).show();
            }
        }

        if(requestCode == REQUEST_CODE_CAMERA_PERMISSION && grantResults.length > 0){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                takePhotoHandler();
            }
            else {
                Toast.makeText(this,"Permission Denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initMoreOptions(){
        bottomSheetBehavior = BottomSheetBehavior.from(moreOptionsLayout);
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
                selectImageHandler();
            }
        });

        moreOptionsLayout.findViewById(R.id.takePhotoLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                takePhotoHandler();
            }
        });

        moreOptionsLayout.findViewById(R.id.addUrlLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                showAddUrlDialig();
            }
        });

        moreOptionsLayout.findViewById(R.id.transcribeAudioLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                showTranscribeAudioUI();
            }
        });

        moreOptionsLayout.findViewById(R.id.recordAudioLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                showRecordAudioDialog();
            }
        });



    }

    private void initInfoSheet(){
        infoSheetBehavior = BottomSheetBehavior.from(infoSheetLayout);
        infoSheetLayout.findViewById(R.id.textInfo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(infoSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED){
                    infoSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
                else if(isExistingNote && (existingNoteWithData.note.getIsActive() == Constants.IS_ARCHIVE)){
                    infoSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
                else{
                    infoSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
            }
        });
        infoSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                    infoSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        if(isExistingNote){
            List<LatLng> pinnedLatLngs = new ArrayList<>();
            for(int i = 0; i < existingNoteWithData.editedLocations.size(); i++){
                if(i > 5){
                    break;
                }
                else {
                    if(i == 0){
                        EditedLocations location = existingNoteWithData.editedLocations.get(i);
                        String latlongStr = location.getLocation();
                        String[] latLongList = latlongStr.split(" ");
                        LatLng coordinate = new LatLng(Double.parseDouble(latLongList[0]), Double.parseDouble(latLongList[1]));
                        pinnedLatLngs.add(coordinate);
                        map.addMarker(new MarkerOptions().position(coordinate).title("Created here on " + location.getDateTime()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                    }
                    else {
                        EditedLocations location = existingNoteWithData.editedLocations.get(i);
                        String latlongStr = location.getLocation();
                        String[] latLongList = latlongStr.split(" ");
                        LatLng coordinate = new LatLng(Double.parseDouble(latLongList[0]), Double.parseDouble(latLongList[1]));
                        pinnedLatLngs.add(coordinate);
                        map.addMarker(new MarkerOptions().position(coordinate).title("Edited here on " + location.getDateTime()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
                    }
                }
            }
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for(LatLng location: pinnedLatLngs){
                builder.include(location);
            }
            LatLngBounds bounds = builder.build();
            map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 10));
        }
    }

    private void setNoteIndicatorColor(){
        GradientDrawable gradientDrawable = (GradientDrawable) noteColorIndicator.getBackground();
        gradientDrawable.setColor(Color.parseColor(selectedNoteColor));
    }

    private void selectImageHandler(){
        if(isReadExternalStoragePermissionGranted()){
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            if(intent.resolveActivity(getPackageManager()) != null){
                startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE);
            }
        }
        else {
            ActivityCompat.requestPermissions(
                    CreateNoteActivity.this,
                    new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_CODE_READ_EXTERNAL_STORAGE_PERMISSION
            );
        }

    }

    private boolean isReadExternalStoragePermissionGranted(){
        if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        else {
            return false;
        }
    }

    private void takePhotoHandler(){
        if(isCameraPermissionGranted()){
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            // Ensure that there's a camera activity to handle the intent
            if (intent.resolveActivity(getPackageManager()) != null) {
                // Create the File where the photo should go
                File photoFile = null;
                try {
                    photoFile = createTempImageFile();
                } catch (IOException ex) {
                    System.out.println(ex.getMessage());
                }
                // Continue only if the File was successfully created
                if (photoFile != null) {
                    Uri photoURI = FileProvider.getUriForFile(this,
                            getPackageName() + ".fileprovider",
                            photoFile);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    startActivityForResult(intent, REQUEST_CODE_CAPTURE_IMAGE);
                }
            }
        }
        else {
            ActivityCompat.requestPermissions(
                    CreateNoteActivity.this,
                    new String[] {Manifest.permission.CAMERA},
                    REQUEST_CODE_CAMERA_PERMISSION
            );
        }
    }

    private boolean isCameraPermissionGranted(){
        if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        else {
            return false;
        }
    }

    private File createTempImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalCacheDir();
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentTempPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if((requestCode == REQUEST_CODE_SELECT_IMAGE && data != null) || (requestCode == REQUEST_CODE_CAPTURE_IMAGE) && resultCode == RESULT_OK){
                try {
                    if(isExternalStorageWritable()){
                        if(requestCode == REQUEST_CODE_SELECT_IMAGE){
                            Uri imageUri = data.getData();
                            compressImageAndUpdateViewPager(getRealPathFromURI(imageUri));
                        }
                        else {
                            compressImageAndUpdateViewPager(currentTempPhotoPath);
                        }
                    }
                    else {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_READ_EXTERNAL_STORAGE_PERMISSION);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
        }

        if(requestCode == REQUEST_CODE_SPEECH_INPUT && resultCode == RESULT_OK){
                if(data != null){
                    try {
                        // the resulting text is in the getExtras:
                        Bundle bundle = data.getExtras();
                        ArrayList<String> transcribedStrings = bundle.getStringArrayList(RecognizerIntent.EXTRA_RESULTS);
                        noteText.setText(noteText.getText().toString() + transcribedStrings.get(0) + "\n");

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

        }
    }

    public String getRealPathFromURI(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        return cursor.getString(idx);
    }

    private byte[] byteArrayFromBitmap(Bitmap bitmap){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        bitmap.recycle();
        return byteArray;
    }

    private byte[] byteArrayFromUri(Uri contentUri) throws IOException {
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), contentUri);
        return byteArrayFromBitmap(bitmap);
//        InputStream iStream =   getContentResolver().openInputStream(contentUri);
//        byte[] inputData = getBytes(iStream);
//        return inputData;
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
    private boolean isExternalMountedStorageReadable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(Environment.getExternalStorageState());
    }

    private void showTranscribeAudioUI(){
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 2000);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Hi, Speak something.");
        try {
            startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT);
            //Toast.makeText(this, "Your audio will be sent to Google APIs to provide speech recognition service.", Toast.LENGTH_LONG).show();
        }
        catch (Exception e){
            Toast.makeText(getApplicationContext(),"Transcribing speech requires Google app. Please install and try again.", Toast.LENGTH_LONG).show();
        }
    }

    private void showRecordAudioDialog(){
        if(isAudioRecordingPermissionGranted()){
            isRecording = true;
            if(recordAudioDialog == null){
                AlertDialog.Builder builder = new AlertDialog.Builder(CreateNoteActivity.this);
                View view = LayoutInflater.from(this).inflate(
                        R.layout.record_audio_layout,
                        (ViewGroup) findViewById(R.id.recordAudioDialogLayout)
                );
                builder.setView(view);
                recordAudioDialog = builder.create();
                recordAudioDialog.setCancelable(false);
                recordAudioDialog.setCanceledOnTouchOutside(false);
                if(recordAudioDialog.getWindow() != null){
                    recordAudioDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
                }

                final Chronometer recordingTimerTV = view.findViewById(R.id.recordingTimerTV);
                recordingTimerTV.setBase(SystemClock.elapsedRealtime());
                recordingTimerTV.start();
                view.findViewById(R.id.stopRecordingTV).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        isRecording = false;
                        recordingTimerTV.stop();
                        stopRecordingAudio();
                        recordAudioDialog.dismiss();
                        recordAudioDialog = null;
                        noteAudiosAdapter.notifyItemInserted(totalAudios.size() - 1);
                    }
                });
            }
            recordAudioDialog.show();
            startRecordingAudio();
        }

    }

    private boolean isAudioRecordingPermissionGranted(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_CODE_RECORD_AUDIO_PERMISSION);
            return false;
        }
    }

    private void startRecordingAudio(){
        try{
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            String fileName = "AUDIO" + generateUUID() + new Date().getTime();
            String filePath = getApplicationContext().getExternalFilesDir("/").getAbsolutePath();
            mediaRecorder.setOutputFile(filePath + "/" + fileName);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mediaRecorder.prepare();
            mediaRecorder.start();
            NoteAudioViewModel audioViewModel = new NoteAudioViewModel();
            audioViewModel.audioUniqueFileName = fileName;
            audioViewModel.index = totalAudios.size();
            totalAudios.add(audioViewModel);
            newAudios.add(audioViewModel);
        }
        catch (Exception e){

        }

    }

    private void stopRecordingAudio(){
        mediaRecorder.stop();
        mediaRecorder.release();
        mediaRecorder = null;
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

    private void showClickedImage(String uniqueImageName, int position){
        if(showImageDialog == null){
            AlertDialog.Builder builder = new AlertDialog.Builder(CreateNoteActivity.this);
            View view = LayoutInflater.from(this).inflate(
                    R.layout.image_view_dialog_layout,
                    (ViewGroup) findViewById(R.id.imageViewDialogLayout)
            );
            builder.setView(view);
            showImageDialog = builder.create();

            showImageDialog.setCancelable(false);
            showImageDialog.setCanceledOnTouchOutside(false);
            if(showImageDialog.getWindow() != null){
                showImageDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }

            final ImageView attachmentImageViewInDialog = view.findViewById(R.id.selectedImageViewInModal);
            final TextView imagePositionTV = view.findViewById(R.id.imagePositionTV);

            view.findViewById(R.id.closeImageViewModalTV).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showImageDialog.dismiss();
                    showImageDialog = null;
                }
            });

            if(uniqueImageName != null){
                File file = new File(getExternalFilesDir(null), uniqueImageName);
                Picasso.get().load(file).into(attachmentImageViewInDialog);
                attachmentImageViewInDialog.setVisibility(View.VISIBLE);

                        attachmentImageViewInDialog.setMaxHeight(1080);

                imagePositionTV.setText("" + (position + 1) + "/" + totalImages.size());
            }
        }
        showImageDialog.show();
    }


    private void compressImageAndUpdateViewPager(String originalImagePath){
        class ImageCompression extends AsyncTask<String, Void, String> {
            private String mImageName = "";
            private static final float maxHeight = 1280.0f;
            private static final float maxWidth = 1280.0f;


            @Override
            protected String doInBackground(String... strings) {
                if(strings.length == 0 || strings[0] == null)
                    return null;

                return compressImage(strings[0]);
            }

            protected void onPostExecute(String imageName){
                // imagePath is path of new compressed image.
                addImageToViewPager(imageName);
                File tempFile = new File(currentTempPhotoPath);
                tempFile.delete();
                currentTempPhotoPath = "";
            }


            public String compressImage(String imagePath) {
                Bitmap scaledBitmap = null;

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                Bitmap bmp = BitmapFactory.decodeFile(imagePath, options);

                int actualHeight = options.outHeight;
                int actualWidth = options.outWidth;

                float imgRatio = (float) actualWidth / (float) actualHeight;
                float maxRatio = maxWidth / maxHeight;

                if (actualHeight > maxHeight || actualWidth > maxWidth) {
                    if (imgRatio < maxRatio) {
                        imgRatio = maxHeight / actualHeight;
                        actualWidth = (int) (imgRatio * actualWidth);
                        actualHeight = (int) maxHeight;
                    } else if (imgRatio > maxRatio) {
                        imgRatio = maxWidth / actualWidth;
                        actualHeight = (int) (imgRatio * actualHeight);
                        actualWidth = (int) maxWidth;
                    } else {
                        actualHeight = (int) maxHeight;
                        actualWidth = (int) maxWidth;

                    }
                }

                options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);
                options.inJustDecodeBounds = false;
                options.inDither = false;
                options.inPurgeable = true;
                options.inInputShareable = true;
                options.inTempStorage = new byte[16 * 1024];

                try {
                    bmp = BitmapFactory.decodeFile(imagePath, options);
                } catch (OutOfMemoryError exception) {
                    exception.printStackTrace();

                }
                try {
                    scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.RGB_565);
                } catch (OutOfMemoryError exception) {
                    exception.printStackTrace();
                }

                float ratioX = actualWidth / (float) options.outWidth;
                float ratioY = actualHeight / (float) options.outHeight;
                float middleX = actualWidth / 2.0f;
                float middleY = actualHeight / 2.0f;

                Matrix scaleMatrix = new Matrix();
                scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

                Canvas canvas = new Canvas(scaledBitmap);
                canvas.setMatrix(scaleMatrix);
                canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

                if(bmp!=null)
                {
                    bmp.recycle();
                }

                ExifInterface exif;
                try {
                    exif = new ExifInterface(imagePath);
                    int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
                    Matrix matrix = new Matrix();
                    if (orientation == 6) {
                        matrix.postRotate(90);
                    } else if (orientation == 3) {
                        matrix.postRotate(180);
                    } else if (orientation == 8) {
                        matrix.postRotate(270);
                    }
                    scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                FileOutputStream out = null;
                String filepath = getFilePath();
                try {
                    out = new FileOutputStream(filepath);

                    //write the compressed bitmap at the destination specified by filename.
                    scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                return getFileName();
            }

            public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
                final int height = options.outHeight;
                final int width = options.outWidth;
                int inSampleSize = 1;

                if (height > reqHeight || width > reqWidth) {
                    final int heightRatio = Math.round((float) height / (float) reqHeight);
                    final int widthRatio = Math.round((float) width / (float) reqWidth);
                    inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
                }
                final float totalPixels = width * height;
                final float totalReqPixelsCap = reqWidth * reqHeight * 2;

                while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
                    inSampleSize++;
                }

                return inSampleSize;
            }

            public String getFilePath() {
                mImageName="IMG_"+ String.valueOf(System.currentTimeMillis()) +".jpg";
                String uriString = (getApplicationContext().getExternalFilesDir("/").getAbsolutePath() + "/"+ mImageName);;
                return uriString;
            }

            public String getFileName(){
                return mImageName;
            }

        }
        new ImageCompression().execute(originalImagePath);
    }

    private void addImageToViewPager(String imageName){
        NoteImageViewModel noteImageViewModel = new NoteImageViewModel();
        noteImageViewModel.imageUniqueFileName = imageName;
        noteImageViewModel.index = totalImages.size();
        totalImages.add(noteImageViewModel);
        selectedImages.add(noteImageViewModel);
        noteImagesAdapter.notifyItemChanged(totalImages.size() - 1);
        imagePositionIndicator.setVisibility(View.VISIBLE);
        imagesViewPager.setCurrentItem(totalImages.size() - 1, true);
    }
}