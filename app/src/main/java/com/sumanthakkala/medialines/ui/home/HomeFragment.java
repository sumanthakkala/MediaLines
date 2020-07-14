package com.sumanthakkala.medialines.ui.home;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.navigation.NavigationView;
import com.sumanthakkala.medialines.R;
import com.sumanthakkala.medialines.activities.CreateNoteActivity;
import com.sumanthakkala.medialines.adapters.NotesAdapter;
import com.sumanthakkala.medialines.constants.Constants;
import com.sumanthakkala.medialines.database.MediaLinesDatabase;
import com.sumanthakkala.medialines.entities.Attachments;
import com.sumanthakkala.medialines.entities.NoteWithData;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.sumanthakkala.medialines.listeners.NotesListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static android.app.Activity.RESULT_OK;

public class HomeFragment extends Fragment implements NotesListener, SearchView.OnQueryTextListener, ActivityCompat.OnRequestPermissionsResultCallback {
    public static final int REQUEST_CODE_ADD_NOTE = 1;
    public static final int REQUEST_CODE_UPDATE_NOTE = 2;
    public static final int REQUEST_CODE_SELECT_IMAGE = 3;
    public static final int REQUEST_CODE_READ_EXTERNAL_STORAGE_PERMISSION = 4;
    public static final int REQUEST_CODE_SPEECH_INPUT = 5;

    public static final String SORT_BY_DATE = "date";
    public static final String SORT_BY_TITLE = "title";



    private ConstraintLayout noDataScreen;
    private RecyclerView notesRecyclerView;
    private RecyclerView bookmarkedNotesRecyclerView;
    private TextView othersTV;
    private TextView bookmarksTV;
    private List<NoteWithData> notesList;
    private List<NoteWithData> bookmarkedNotesList;
    private List<NoteWithData> intactBookmarkedNotesList = new ArrayList<>();
    private List<NoteWithData> intactNotesList = new ArrayList<>();
    private List<NoteWithData> intactOtherNotesList = new ArrayList<>();
    private List<NoteWithData> selectedNotes = new ArrayList<>();
    private List<NoteWithData> notesToBookmarkInSelectedNotes = new ArrayList<>();
    private List<NoteWithData> notesToUnBookmarkInSelectedNotes = new ArrayList<>();
    private NoteWithData clickedNote;
    private NotesAdapter notesAdapter;
    private NotesAdapter bookmarkedNotesAdapter;
    private FloatingActionButton fab;
    private LinearLayout quickActionsLayout;
    private LinearLayout multiSelectActionsLayout;
    private TextView selectionCountTV;
    private ImageView deleteSelectedNotesIV;
    private ImageView archiveSelectedNotesIV;
    private ImageView unArchiveSelectedNotesIV;
    private ImageView bookmarkHandlerSelectedNotesIV;
    private ImageView addImageQuickActionIV;
    private ImageView addUrlQuickActionIV;
    private ImageView transcribeSpeechQuickActionIV;
    private ImageView cancelMultiSelectIV;
    private ImageView sortModeIV;
    private SearchView searchView;
    private MenuItem mSearchMenuItem;
    private AlertDialog addUrlDialog;
    private BottomAppBar bottomAppBar;

    private int notesType = 1;

    private int noteClickedPosition = -1;
    private boolean isInMultiSelectMode = false;
    private String currentSortMode = SORT_BY_DATE;
    private String bookmarkHandlerIVTag = "";
    private String bookmarkMode;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.fragment_home, container, false);
        setHasOptionsMenu(true);
        requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if(shouldInterceptBackPress()){
                    // in here you can do logic when backPress is clicked
                    cancelMultiSelectIV.performClick();
                }
                else {
                    setEnabled(false);
                    requireActivity().onBackPressed();
                }
            }
        });
        NavigationView navigationView = getActivity().findViewById(R.id.nav_view);
        MenuItem selectedMenuItem = navigationView.getCheckedItem();
        if(selectedMenuItem.getItemId() == R.id.nav_home){
            notesType = Constants.IS_ACTIVE;
        }
        if(selectedMenuItem.getItemId() == R.id.nav_archive){
            notesType = Constants.IS_ARCHIVE;
        }
        fab = root.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(getActivity().getApplicationContext(), CreateNoteActivity.class),
                        REQUEST_CODE_ADD_NOTE);
            }
        });

        noDataScreen = root.findViewById(R.id.noDataLayout);

        bookmarksTV = root.findViewById(R.id.bookmarksTV);
        bookmarkedNotesRecyclerView = root.findViewById(R.id.bookmarkedNotesRecyclerView);
        bookmarkedNotesRecyclerView.setLayoutManager(
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        );
        bookmarkedNotesRecyclerView.setNestedScrollingEnabled(false);

        othersTV = root.findViewById(R.id.othersTV);
        notesRecyclerView = root.findViewById(R.id.notesRecyclerView);
        notesRecyclerView.setLayoutManager(
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        );
        notesRecyclerView.setNestedScrollingEnabled(false);


        notesList = new ArrayList<>();
        notesAdapter = new NotesAdapter(notesList, HomeFragment.this);
        notesRecyclerView.setAdapter(notesAdapter);

        bookmarkedNotesList = new ArrayList<>();
        bookmarkedNotesAdapter = new NotesAdapter(bookmarkedNotesList, HomeFragment.this);
        bookmarkedNotesRecyclerView.setAdapter(bookmarkedNotesAdapter);


        bottomAppBar = root.findViewById(R.id.bottomAppBar);
        quickActionsLayout = root.findViewById(R.id.quickActionsLayout);
        multiSelectActionsLayout = root.findViewById(R.id.multiSelectActionsLayout);
        selectionCountTV = root.findViewById(R.id.multiSelectCount);
        cancelMultiSelectIV = root.findViewById(R.id.closeMultiSelect);
        cancelMultiSelectIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedNotes.clear();
                notesToBookmarkInSelectedNotes.clear();
                notesToUnBookmarkInSelectedNotes.clear();
                notesAdapter.cancelMultiSelect();
                bookmarkedNotesAdapter.cancelMultiSelect();
            }
        });
        deleteSelectedNotesIV = root.findViewById(R.id.deleteSelectedNotes);
        deleteSelectedNotesIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteSelectedNotes();
            }
        });

        archiveSelectedNotesIV = root.findViewById(R.id.archiveSelectedNotes);
        archiveSelectedNotesIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                archiveSelectedNotes();
            }
        });

        bookmarkHandlerSelectedNotesIV = root.findViewById(R.id.bookmarkHandlerSelectedNotes);
        bookmarkHandlerSelectedNotesIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bookmarkHandlerIVTag = view.getTag().toString();
                bookmarkHandler();
            }
        });

        unArchiveSelectedNotesIV = root.findViewById(R.id.unArchiveSelectedNotes);
        unArchiveSelectedNotesIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                unArchiveSelectedNotes();
            }
        });

        transcribeSpeechQuickActionIV = root.findViewById(R.id.imageTranscribeSpeech);
        transcribeSpeechQuickActionIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onTranscribeSpeechQuickActionPressed();
            }
        });

        sortModeIV = root.findViewById(R.id.imageSortNotes);
        sortModeIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sortNotes();
            }
        });

        addImageQuickActionIV = root.findViewById(R.id.imageAddImage);
        addImageQuickActionIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    selectImageHandler();
            }
        });

        addUrlQuickActionIV = root.findViewById(R.id.imageAddWebLink);
        addUrlQuickActionIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddUrlDialig();
            }
        });



        switch (notesType){
            case Constants
                    .IS_ACTIVE:
                setupActiveNotesView();
                break;
            case Constants.IS_ARCHIVE:
                setupArchiveNotesView();
                break;
        }

        getNotes();
        return root;
    }

    private boolean shouldInterceptBackPress() {
        if(isInMultiSelectMode){
            return true;
        }
        else {
            return false;
        }
    }


    private void bookmarkHandler() {

        @SuppressLint("StaticFieldLeak")
        class BookmarkNotesTask extends AsyncTask<Void, Void, Void> {
            @Override
            protected Void doInBackground(Void... voids) {
                if(selectedNotes.size() > 0){
                    List<Long> noteIds = new ArrayList<>();

                    if(bookmarkHandlerIVTag.equals(Constants.BOOKMARK)){
                        for (NoteWithData note: notesToBookmarkInSelectedNotes){
                            noteIds.add(note.note.getNoteId());
                        }
                        MediaLinesDatabase.getMediaLinesDatabase(getContext()).noteDao().bookmarkNotesWithId(noteIds);
                    }
                    else {
                        for (NoteWithData note: notesToUnBookmarkInSelectedNotes){
                            noteIds.add(note.note.getNoteId());
                        }
                        MediaLinesDatabase.getMediaLinesDatabase(getContext()).noteDao().unBookmarkNotesWithId(noteIds);
                    }

                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

                //In the commented approach, once inserted the bookmarked not, it is inserting on top regardless of sort mode. on refresh, all notes are sorted.
//                if(bookmarkHandlerIVTag.equals(Constants.BOOKMARK)){
//                    notesList.removeAll(selectedNotes);
//                    intactOtherNotesList.removeAll(selectedNotes);
//                    notesAdapter.setIntactDataSource(intactOtherNotesList);
//                    notesAdapter.notifyDataSetChanged();
//                    notesAdapter.notifyItemRangeChanged(0, notesList.size());
//
//                    bookmarkedNotesList.addAll(0, notesToBookmarkInSelectedNotes);
//                    intactBookmarkedNotesList.addAll(0, notesToBookmarkInSelectedNotes);
//                    bookmarkedNotesAdapter.setIntactDataSource(intactBookmarkedNotesList);
//                    bookmarkedNotesAdapter.notifyDataSetChanged();
////                    bookmarkedNotesAdapter.notifyItemRangeInserted(0, notesToBookmarkInSelectedNotes.size());
//                    bookmarkedNotesAdapter.notifyItemRangeChanged(0, bookmarkedNotesList.size());
//                    cancelMultiSelectIV.performClick();
//
//                    bookmarkedNotesRecyclerView.setVisibility(View.VISIBLE);
//                    bookmarksTV.setVisibility(View.VISIBLE);
//                    othersTV.setVisibility(View.VISIBLE);
//                    if(bookmarkedNotesList.size() == 0){
//                        bookmarkedNotesRecyclerView.setVisibility(View.GONE);
//                        bookmarksTV.setVisibility(View.GONE);
//                    }
//                    if(notesList.size() == 0){
//                        notesRecyclerView.setVisibility(View.GONE);
//                        othersTV.setVisibility(View.GONE);
//                    }
//                }
//                else {
//                    cancelMultiSelectIV.performClick();
//                    getNotes();
//                }
//                bookmarkedNotesRecyclerView.setAdapter(null);
//                notesRecyclerView.setAdapter(null);

                cancelMultiSelectIV.performClick();
                collapseSearchView();
                getNotes();
            }
        }
        new BookmarkNotesTask().execute();
    }

    private void unArchiveSelectedNotes() {

        @SuppressLint("StaticFieldLeak")
        class UnArchiveNotesTask extends AsyncTask<Void, Void, Void> {
            @Override
            protected Void doInBackground(Void... voids) {
                if(selectedNotes.size() > 0){
                    List<Long> noteIds = new ArrayList<>();
                    for (NoteWithData note: selectedNotes){
                        noteIds.add(note.note.getNoteId());
                    }
                    MediaLinesDatabase.getMediaLinesDatabase(getContext()).noteDao().unArchiveNotesWithId(noteIds);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                notesList.removeAll(selectedNotes);
                bookmarkedNotesList.removeAll(selectedNotes);
                intactBookmarkedNotesList.removeAll(selectedNotes);
                intactNotesList.removeAll(selectedNotes);
                intactOtherNotesList.removeAll(selectedNotes);
                notesAdapter.setIntactDataSource(intactOtherNotesList);
                notesAdapter.notifyDataSetChanged();
                notesAdapter.notifyItemRangeChanged(0, notesList.size());
                bookmarkedNotesAdapter.setIntactDataSource(intactBookmarkedNotesList);
                bookmarkedNotesAdapter.notifyDataSetChanged();
                bookmarkedNotesAdapter.notifyItemRangeChanged(0, bookmarkedNotesList.size());
                if(bookmarkedNotesList.size() == 0){
                    bookmarkedNotesRecyclerView.setVisibility(View.GONE);
                    bookmarksTV.setVisibility(View.GONE);
                }
                if(notesList.size() == 0){
                    notesRecyclerView.setVisibility(View.GONE);
                    othersTV.setVisibility(View.GONE);
                }
                cancelMultiSelectIV.performClick();
                collapseSearchView();
                checkForEmptyData();
            }
        }
        new UnArchiveNotesTask().execute();

    }

    private void archiveSelectedNotes() {
        @SuppressLint("StaticFieldLeak")
        class ArchiveNotesTask extends AsyncTask<Void, Void, Void> {
            @Override
            protected Void doInBackground(Void... voids) {
                if(selectedNotes.size() > 0){
                    List<Long> noteIds = new ArrayList<>();
                    for (NoteWithData note: selectedNotes){
                        noteIds.add(note.note.getNoteId());
                    }
                    MediaLinesDatabase.getMediaLinesDatabase(getContext()).noteDao().archiveNotesWithId(noteIds);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                notesList.removeAll(selectedNotes);
                bookmarkedNotesList.removeAll(selectedNotes);
                intactBookmarkedNotesList.removeAll(selectedNotes);
                intactNotesList.removeAll(selectedNotes);
                intactOtherNotesList.removeAll(selectedNotes);
                notesAdapter.setIntactDataSource(intactOtherNotesList);
                notesAdapter.notifyDataSetChanged();
                notesAdapter.notifyItemRangeChanged(0, notesList.size());
                bookmarkedNotesAdapter.setIntactDataSource(intactBookmarkedNotesList);
                bookmarkedNotesAdapter.notifyDataSetChanged();
                bookmarkedNotesAdapter.notifyItemRangeChanged(0, bookmarkedNotesList.size());
                if(bookmarkedNotesList.size() == 0){
                    bookmarkedNotesRecyclerView.setVisibility(View.GONE);
                    bookmarksTV.setVisibility(View.GONE);
                }
                if(notesList.size() == 0){
                    notesRecyclerView.setVisibility(View.GONE);
                    othersTV.setVisibility(View.GONE);
                }
                cancelMultiSelectIV.performClick();
                collapseSearchView();
                checkForEmptyData();
            }
        }
        new ArchiveNotesTask().execute();
    }

    private void setupActiveNotesView() {
        addImageQuickActionIV.setVisibility(View.VISIBLE);
        addUrlQuickActionIV.setVisibility(View.VISIBLE);
        transcribeSpeechQuickActionIV.setVisibility(View.VISIBLE);
        archiveSelectedNotesIV.setVisibility(View.VISIBLE);
        unArchiveSelectedNotesIV.setVisibility(View.GONE);
        fab.show();
    }

    private void setupArchiveNotesView() {
        addImageQuickActionIV.setVisibility(View.GONE);
        addUrlQuickActionIV.setVisibility(View.GONE);
        transcribeSpeechQuickActionIV.setVisibility(View.GONE);
        archiveSelectedNotesIV.setVisibility(View.GONE);
        unArchiveSelectedNotesIV.setVisibility(View.VISIBLE);
        bookmarkHandlerSelectedNotesIV.setVisibility(View.GONE);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 0);
        sortModeIV.setLayoutParams(lp);
        fab.hide();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_CODE_READ_EXTERNAL_STORAGE_PERMISSION && grantResults.length > 0){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectImageHandler();
            }
            else {
                Toast.makeText(getContext(),"Permission Denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull final Menu menu) {
        super.onPrepareOptionsMenu(menu);
        mSearchMenuItem = menu.findItem(R.id.search_view);
        mSearchMenuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem menuItem) {
                //bottomAppBar.setVisibility(View.GONE);
                fab.hide();
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                //bottomAppBar.setVisibility(View.VISIBLE);
                notesAdapter.setMultiSelectMode(false);
                bookmarkedNotesAdapter.setMultiSelectMode(false);
                cancelMultiSelectIV.performClick();
                if(notesType == Constants.IS_ACTIVE){
                    fab.show();
                }
                return true;
            }
        });
        searchView = (SearchView) mSearchMenuItem.getActionView();
        searchView.setOnQueryTextListener(this);
    }

    @Override
    public void onNoteCLicked(NoteWithData noteWithData, int position) {
        noteClickedPosition = position;
        clickedNote = noteWithData;
        Intent intent = new Intent(getActivity().getApplicationContext(), CreateNoteActivity.class);
        intent.putExtra("isViewOrUpdate", true);
        intent.putExtra("noteData", noteWithData);
        intent.putExtra("position", position);
        startActivityForResult(intent, REQUEST_CODE_UPDATE_NOTE);
    }

    @Override
    public void onFilterNotesDone() {
        if(notesAdapter.getDataSource().size() == 0){
            othersTV.setVisibility(View.GONE);
            notesRecyclerView.setVisibility(View.GONE);
        }
        else {
            othersTV.setVisibility(View.VISIBLE);
            notesRecyclerView.setVisibility(View.VISIBLE);

        }

        if(bookmarkedNotesAdapter.getDataSource().size() == 0){
            bookmarksTV.setVisibility(View.GONE);
            bookmarkedNotesRecyclerView.setVisibility(View.GONE);
        }
        else {
            bookmarksTV.setVisibility(View.VISIBLE);
            bookmarkedNotesRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onMultiSelectBegin() {
        isInMultiSelectMode = true;
        quickActionsLayout.setVisibility(View.GONE);
        fab.hide();
        multiSelectActionsLayout.setVisibility(View.VISIBLE);
        notesAdapter.setMultiSelectMode(true);
        bookmarkedNotesAdapter.setMultiSelectMode(true);
    }

    @Override
    public void onMultiSelectEnd() {

        if(selectedNotes.size() == 0){
            isInMultiSelectMode = false;
            quickActionsLayout.setVisibility(View.VISIBLE);
            multiSelectActionsLayout.setVisibility(View.GONE);
            if(notesType == Constants.IS_ACTIVE){
                fab.show();
            }
            notesAdapter.setMultiSelectMode(false);
            bookmarkedNotesAdapter.setMultiSelectMode(false);
        }

    }

    @Override
    public void onNoteClickInMultiSelectMode(NoteWithData noteWithData, int action) {
        if(action == 1){
            //note selected

            if(selectedNotes.size() == 0){
                if(bookmarkedNotesList.contains(noteWithData)){
                    bookmarkMode = Constants.UN_BOOKMARK;
                    //pin to unbookmark
                    bookmarkHandlerSelectedNotesIV.setImageDrawable(getResources().getDrawable(R.drawable.ic_bookmark_fill));
                    bookmarkHandlerSelectedNotesIV.setTag(Constants.UN_BOOKMARK);
                }
                else {
                    bookmarkMode = Constants.BOOKMARK;
                    //pin to bookmark
                    bookmarkHandlerSelectedNotesIV.setImageDrawable(getResources().getDrawable(R.drawable.ic_bookmark_border));
                    bookmarkHandlerSelectedNotesIV.setTag(Constants.BOOKMARK);
                }
            }
            if((bookmarkMode == Constants.UN_BOOKMARK) && notesList.contains(noteWithData)){
                bookmarkMode = Constants.BOOKMARK;
                //pin to bookmark
                bookmarkHandlerSelectedNotesIV.setImageDrawable(getResources().getDrawable(R.drawable.ic_bookmark_border));
                bookmarkHandlerSelectedNotesIV.setTag(Constants.BOOKMARK);
            }
            if(bookmarkedNotesList.contains(noteWithData)){
                notesToUnBookmarkInSelectedNotes.add(noteWithData);
            }
            else {
                notesToBookmarkInSelectedNotes.add(noteWithData);
            }
            selectedNotes.add(noteWithData);
            selectionCountTV.setText("" + selectedNotes.size());
        }
        else {
            //note removed from selection
            selectedNotes.remove(noteWithData);
            selectionCountTV.setText("" + selectedNotes.size());
            if(bookmarkedNotesList.contains(noteWithData)){
                notesToUnBookmarkInSelectedNotes.remove(noteWithData);
            }
            else {
                notesToBookmarkInSelectedNotes.remove(noteWithData);
            }

            if(notesToBookmarkInSelectedNotes.size() == 0){
                bookmarkMode = Constants.UN_BOOKMARK;
                //pin to unbookmark
                bookmarkHandlerSelectedNotesIV.setImageDrawable(getResources().getDrawable(R.drawable.ic_bookmark_fill));
                bookmarkHandlerSelectedNotesIV.setTag(Constants.UN_BOOKMARK);
            }

            if(notesToUnBookmarkInSelectedNotes.size() == 0){
                bookmarkMode = Constants.BOOKMARK;
                //pin to bookmark
                bookmarkHandlerSelectedNotesIV.setImageDrawable(getResources().getDrawable(R.drawable.ic_bookmark_border));
                bookmarkHandlerSelectedNotesIV.setTag(Constants.BOOKMARK);
            }

        }

    }


    private void selectImageHandler(){
        if(isReadExternalStoragePermissionGranted()){
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            if(intent.resolveActivity(getActivity().getPackageManager()) != null){
                startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE);
            }
        }
        else {
            requestPermissions(
                    new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_CODE_READ_EXTERNAL_STORAGE_PERMISSION
            );
        }

    }

    private boolean isReadExternalStoragePermissionGranted(){
        if(ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        else {
            return false;
        }
    }

    private void deleteSelectedNotes(){

        @SuppressLint("StaticFieldLeak")
        class DeleteNotesTask extends AsyncTask<Void, Void, Void> {
            @Override
            protected Void doInBackground(Void... voids) {
                if(selectedNotes.size() > 0){
                    List<Long> noteIds = new ArrayList<>();
                    for (NoteWithData note: selectedNotes){
                        noteIds.add(note.note.getNoteId());
                        deleteAttachmentsInAppStorage(note.attachments);
                    }
                    MediaLinesDatabase.getMediaLinesDatabase(getContext()).noteDao().deleteNotesWithId(noteIds);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                notesList.removeAll(selectedNotes);
                intactNotesList.removeAll(selectedNotes);
                intactOtherNotesList.removeAll(selectedNotes);
                notesAdapter.setIntactDataSource(intactOtherNotesList);
                notesAdapter.notifyDataSetChanged();
                notesAdapter.notifyItemRangeChanged(0, notesList.size());

                bookmarkedNotesList.removeAll(selectedNotes);
                intactBookmarkedNotesList.removeAll(selectedNotes);
                bookmarkedNotesAdapter.setIntactDataSource(intactBookmarkedNotesList);
                bookmarkedNotesAdapter.notifyDataSetChanged();
                bookmarkedNotesAdapter.notifyItemRangeChanged(0, notesList.size());

                cancelMultiSelectIV.performClick();
                collapseSearchView();
                checkForEmptyData();
            }
        }
        new DeleteNotesTask().execute();
    }

    private void deleteAttachmentsInAppStorage(List<Attachments> attachments){
        for(Attachments attachment: attachments){
            File attachmentFile = new File(getContext().getExternalFilesDir(null), attachment.getAttachmentUniqueFileName());
            attachmentFile.delete();
        }
    }

    private void getNotes(){
        @SuppressLint("StaticFieldLeak")
        class GetNotesTask extends AsyncTask<Void, Void, List<NoteWithData>>{
            @Override
            protected List<NoteWithData> doInBackground(Void... voids) {
                if(notesType == Constants.IS_ACTIVE){
                    return MediaLinesDatabase.getMediaLinesDatabase(getActivity().getApplicationContext()).noteDao().getActiveNotesWithData();
                }
                else {
                    return MediaLinesDatabase.getMediaLinesDatabase(getActivity().getApplicationContext()).noteDao().getArchiveNotesWithData();
                }
            }

            @Override
            protected void onPostExecute(List<NoteWithData> notes) {
                super.onPostExecute(notes);
                notesList.clear();
                bookmarkedNotesList.clear();
                notesAdapter.notifyDataSetChanged();
                bookmarkedNotesAdapter.notifyDataSetChanged();
                if(notesList.size() == 0){
                    for (NoteWithData noteWithData: notes){
                        if(noteWithData.note.getIsBookmarked() == Constants.IS_BOOKMARKED){
                            bookmarkedNotesList.add(noteWithData);
                        }
                        else {
                            notesList.add(noteWithData);
                        }
                    }
                    intactNotesList.clear();
                    intactNotesList.addAll(0,notes);
                    intactOtherNotesList.clear();
                    intactOtherNotesList.addAll(0,notesList);
                    intactBookmarkedNotesList.clear();
                    intactBookmarkedNotesList.addAll(0,bookmarkedNotesList);
                    notesAdapter.setIntactDataSource(intactOtherNotesList);
                    bookmarkedNotesAdapter.setIntactDataSource(intactBookmarkedNotesList);
                    if(notesList.size() == 0){
                        notesRecyclerView.setVisibility(View.GONE);
                        othersTV.setVisibility(View.GONE);
                    }
                    else {
                        notesRecyclerView.setVisibility(View.VISIBLE);
                        othersTV.setVisibility(View.VISIBLE);
                    }
                    if(bookmarkedNotesList.size() == 0){
                        bookmarkedNotesRecyclerView.setVisibility(View.GONE);
                        bookmarksTV.setVisibility(View.GONE);
                        othersTV.setVisibility(View.GONE);
                    }
                    else {
                        bookmarkedNotesRecyclerView.setVisibility(View.VISIBLE);
                        bookmarksTV.setVisibility(View.VISIBLE);
                    }
                    notesAdapter.notifyDataSetChanged();
                    bookmarkedNotesAdapter.notifyDataSetChanged();
                    checkForEmptyData();
                }
            }
        }

        new GetNotesTask().execute();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if((requestCode == REQUEST_CODE_ADD_NOTE || requestCode == REQUEST_CODE_UPDATE_NOTE) && resultCode == RESULT_OK){
            if(data != null){
                NoteWithData noteWithData = (NoteWithData) data.getSerializableExtra("note");
                if(data.getBooleanExtra("isNoteUpdated", false)){
                    int position = data.getIntExtra("position", -1);
                    if(notesList.contains(clickedNote)){
                        NoteWithData removedNote = notesList.remove(noteClickedPosition);
                        notesList.add(noteClickedPosition, noteWithData);
                        intactOtherNotesList.add(noteClickedPosition, noteWithData);
                        int indexInIntactSource = intactNotesList.indexOf(removedNote);
                        intactNotesList.remove(removedNote);
                        intactNotesList.add(indexInIntactSource, noteWithData);
                        notesAdapter.setIntactDataSource(intactOtherNotesList);
                        notesAdapter.notifyItemChanged(noteClickedPosition);
                    }
                    else {
                        NoteWithData removedNote = bookmarkedNotesList.remove(noteClickedPosition);
                        bookmarkedNotesList.add(noteClickedPosition, noteWithData);
                        intactBookmarkedNotesList.remove(removedNote);
                        intactBookmarkedNotesList.add(noteClickedPosition, noteWithData);
                        int indexInIntactSource = intactNotesList.indexOf(removedNote);
                        intactNotesList.remove(removedNote);
                        intactNotesList.add(indexInIntactSource, noteWithData);
                        bookmarkedNotesAdapter.setIntactDataSource(intactBookmarkedNotesList);
                        bookmarkedNotesAdapter.notifyItemChanged(noteClickedPosition);
                    }
                }
                else if(data.getBooleanExtra("isNoteArchivedOrUnArchived", false)){
                    int position = data.getIntExtra("position", -1);

                    if(notesList.contains(clickedNote)){
                        NoteWithData removedNote = notesList.remove(noteClickedPosition);
                        intactNotesList.remove(removedNote);
                        intactOtherNotesList.remove(removedNote);
                        notesAdapter.setIntactDataSource(intactOtherNotesList);
                        notesAdapter.notifyItemRemoved(noteClickedPosition);
                        notesAdapter.notifyItemRangeChanged(0, notesList.size());
                    }
                    else {
                        NoteWithData removedNote = bookmarkedNotesList.remove(noteClickedPosition);
                        intactNotesList.remove(removedNote);
                        intactBookmarkedNotesList.remove(removedNote);
                        bookmarkedNotesAdapter.setIntactDataSource(intactBookmarkedNotesList);
                        bookmarkedNotesAdapter.notifyItemRemoved(noteClickedPosition);
                        bookmarkedNotesAdapter.notifyItemRangeChanged(0, notesList.size());
                    }


                }
                else {
                    //When new note is created
                    notesRecyclerView.setVisibility(View.VISIBLE);
                    switch (currentSortMode){
                        case SORT_BY_DATE:
                            notesList.add(0, noteWithData);
                            intactNotesList.add(0, noteWithData);
                            intactOtherNotesList.add(0, noteWithData);
                            notesAdapter.setIntactDataSource(intactOtherNotesList);
                            notesAdapter.notifyItemInserted(0);
                            notesRecyclerView.smoothScrollToPosition(0);
                            break;
                        case SORT_BY_TITLE:
                            List<NoteWithData> tempNotesList = new ArrayList<>(notesList);
                            tempNotesList.add(noteWithData);
                            Collections.sort(tempNotesList, new Comparator<NoteWithData>() {
                                @Override
                                public int compare(NoteWithData note1, NoteWithData note2) {
                                    return note1.note.getTitle().toLowerCase().compareTo(note2.note.getTitle().toLowerCase());
                                }
                            });
                            int sortedIndex = tempNotesList.indexOf(noteWithData);
                            notesList.add(sortedIndex, noteWithData);
                            intactNotesList.add(0, noteWithData);
                            intactOtherNotesList.add(0, noteWithData);
                            notesAdapter.setIntactDataSource(intactOtherNotesList);
                            notesAdapter.notifyItemInserted(sortedIndex);
                            break;
                    }


                }
            }
        }

        if(requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK){
            if( data != null){

                try {
                    if(isExternalStorageWritable()){

                        String fileName = generateUUID() + new SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm a", Locale.getDefault())
                                .format(new Date());
                        File attachmentFile = new File(getContext().getExternalFilesDir(null),fileName);
                        FileOutputStream fos = new FileOutputStream(attachmentFile);
                        fos.write(byteArrayFromUri(data.getData()));
                        fos.close();

                        Intent intent = new Intent(getContext(), CreateNoteActivity.class);
                        intent.putExtra("isFromQuickActions", true);
                        intent.putExtra("quickActionsType", "image");
                        intent.putExtra("imageUniqueFileName", fileName);
                        startActivityForResult(intent, REQUEST_CODE_ADD_NOTE);
                    }
                    else {
                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_READ_EXTERNAL_STORAGE_PERMISSION);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        }

        if(requestCode == REQUEST_CODE_SPEECH_INPUT && resultCode == RESULT_OK){
                if(data != null){
                    try {
                        // the resulting text is in the getExtras:
                        Bundle bundle = data.getExtras();
                        ArrayList<String> transcribedStrings = bundle.getStringArrayList(RecognizerIntent.EXTRA_RESULTS);
                        Intent intent = new Intent(getContext(), CreateNoteActivity.class);
                        intent.putExtra("isFromQuickActions", true);
                        intent.putExtra("quickActionsType", "transcribe");
                        intent.putExtra("transcribedText", transcribedStrings.get(0) + "\n");
                        startActivityForResult(intent, REQUEST_CODE_ADD_NOTE);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }


        }
        collapseSearchView();
        checkForEmptyData();
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        notesAdapter.getFilter().filter(newText);
        bookmarkedNotesAdapter.getFilter().filter(newText);
        return false;
    }

    public void collapseSearchView(){
        searchView.setIconified(true);
        MenuItemCompat.collapseActionView(mSearchMenuItem);
    }


    //image related code

    private boolean isExternalStorageWritable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    public String generateUUID() throws Exception{
        return UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
    }

    private byte[] byteArrayFromUri(Uri contentUri) throws IOException {
        InputStream iStream =   getActivity().getContentResolver().openInputStream(contentUri);
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

    private void showAddUrlDialig(){
        if(addUrlDialog == null){
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            View view = LayoutInflater.from(getActivity()).inflate(
                    R.layout.add_url_layout,
                    (ViewGroup) getActivity().findViewById(R.id.addUrlContainerLayout)
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
                        Toast.makeText(getActivity(), "Enter URL", Toast.LENGTH_SHORT).show();
                    }
                    else if(!Patterns.WEB_URL.matcher(inputURL.getText().toString()).matches()){
                        Toast.makeText(getActivity(), "Enter Valid URL", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        addUrlDialog.dismiss();
                        Intent intent = new Intent(getContext(), CreateNoteActivity.class);
                        intent.putExtra("isFromQuickActions", true);
                        intent.putExtra("quickActionsType", "addUrl");
                        intent.putExtra("url", inputURL.getText().toString());
                        startActivityForResult(intent, REQUEST_CODE_ADD_NOTE);
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

    private void onTranscribeSpeechQuickActionPressed(){
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
            Toast.makeText(getContext(),"Transcribing speech requires Google app. Please install and try again.", Toast.LENGTH_LONG).show();
        }
    }

    private void sortNotes(){

        switch (currentSortMode){
            case SORT_BY_DATE:
                Collections.sort(notesList, new Comparator<NoteWithData>() {
                    @Override
                    public int compare(NoteWithData note1, NoteWithData note2) {
                        return note1.note.getTitle().toLowerCase().compareTo(note2.note.getTitle().toLowerCase());
                    }
                });
                sortModeIV.setImageResource(R.drawable.ic_sort_by_alpha);
                notesAdapter.notifyDataSetChanged();
                currentSortMode = SORT_BY_TITLE;
                break;
            case SORT_BY_TITLE:
                notesList.clear();
                notesList.addAll(intactOtherNotesList);
                sortModeIV.setImageResource(R.drawable.ic_date);
                notesAdapter.notifyDataSetChanged();
                notesAdapter.notifyItemRangeChanged(0, notesList.size());
                currentSortMode = SORT_BY_DATE;
                break;
        }


    }

    private void checkForEmptyData(){
        if(intactNotesList.size() == 0){
            noDataScreen.setVisibility(View.VISIBLE);
        }
        else {
            noDataScreen.setVisibility(View.GONE);
        }
    }
}
