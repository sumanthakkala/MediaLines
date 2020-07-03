package com.sumanthakkala.medialines.ui.home;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.sumanthakkala.medialines.R;
import com.sumanthakkala.medialines.activities.CreateNoteActivity;
import com.sumanthakkala.medialines.adapters.NotesAdapter;
import com.sumanthakkala.medialines.database.MediaLinesDatabase;
import com.sumanthakkala.medialines.entities.Attachments;
import com.sumanthakkala.medialines.entities.EditedLocations;
import com.sumanthakkala.medialines.entities.NoteWithData;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.sumanthakkala.medialines.listeners.NotesListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements NotesListener, SearchView.OnQueryTextListener {
    public static final int REQUEST_CODE_ADD_NOTE = 1;
    public static final int REQUEST_CODE_UPDATE_NOTE = 2;


    private RecyclerView notesRecyclerView;
    private List<NoteWithData> notesList;
    private List<NoteWithData> intactNotesList = new ArrayList<>();
    private List<NoteWithData> selectedNotes = new ArrayList<>();
    private NotesAdapter notesAdapter;
    private FloatingActionButton fab;
    private LinearLayout quickActionsLayout;
    private LinearLayout multiSelectActionsLayout;
    private TextView selectionCountTV;
    private ImageView deleteSelectedNotesIV;
    private ImageView cancelMultiSelectIV;
    private SearchView searchView;
    private MenuItem mSearchMenuItem;

    private int noteClickedPosition = -1;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        setHasOptionsMenu(true);

        fab = root.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(getActivity().getApplicationContext(), CreateNoteActivity.class),
                        REQUEST_CODE_ADD_NOTE);
            }
        });

        notesRecyclerView = root.findViewById(R.id.notesRecyclerView);
        notesRecyclerView.setLayoutManager(
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        );

        quickActionsLayout = root.findViewById(R.id.quickActionsLayout);
        multiSelectActionsLayout = root.findViewById(R.id.multiSelectActionsLayout);
        selectionCountTV = root.findViewById(R.id.multiSelectCount);
        cancelMultiSelectIV = root.findViewById(R.id.closeMultiSelect);
        cancelMultiSelectIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedNotes.clear();
                notesAdapter.cancelMultiSelect();
            }
        });
        deleteSelectedNotesIV = root.findViewById(R.id.deleteSelectedNotes);
        deleteSelectedNotesIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteSelectedNotes();
            }
        });

        notesList = new ArrayList<>();
        notesAdapter = new NotesAdapter(notesList, this);
        notesRecyclerView.setAdapter(notesAdapter);
        getNotes();
        return root;
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        mSearchMenuItem = menu.findItem(R.id.search_view);
        searchView = (SearchView) mSearchMenuItem.getActionView();
        searchView.setOnQueryTextListener(this);
    }

    @Override
    public void onNoteCLicked(NoteWithData noteWithData, int position) {
        noteClickedPosition = position;
        Intent intent = new Intent(getActivity().getApplicationContext(), CreateNoteActivity.class);
        intent.putExtra("isViewOrUpdate", true);
        intent.putExtra("noteData", noteWithData);
        intent.putExtra("position", position);
        startActivityForResult(intent, REQUEST_CODE_UPDATE_NOTE);
    }

    @Override
    public void onMultiSelectBegin() {
        quickActionsLayout.setVisibility(View.GONE);
        fab.hide();
        multiSelectActionsLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onMultiSelectEnd() {
        quickActionsLayout.setVisibility(View.VISIBLE);
        fab.show();
        multiSelectActionsLayout.setVisibility(View.GONE);
    }

    @Override
    public void onNoteClickInMultiSelectMode(NoteWithData noteWithData, int action) {
        if(action == 1){
            //note selected
            selectedNotes.add(noteWithData);
            selectionCountTV.setText("" + selectedNotes.size());
        }
        else {
            //note removed from selection
            selectedNotes.remove(noteWithData);
            selectionCountTV.setText("" + selectedNotes.size());
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
                notesAdapter.setIntactDataSource(intactNotesList);
                notesAdapter.notifyDataSetChanged();
                notesAdapter.notifyItemRangeChanged(0, notesList.size());
                cancelMultiSelectIV.performClick();
                collapseSearchView();
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
                return MediaLinesDatabase.getMediaLinesDatabase(getActivity().getApplicationContext()).noteDao().getNotesWithData();
            }

            @Override
            protected void onPostExecute(List<NoteWithData> notes) {
                super.onPostExecute(notes);
                if(notesList.size() == 0){
                    notesList.addAll(notes);
                    intactNotesList.clear();
                    intactNotesList.addAll(notes);
                    notesAdapter.setIntactDataSource(intactNotesList);
                    notesAdapter.notifyDataSetChanged();
                }
            }
        }

        class GetAttachmentsCountTask extends AsyncTask<Void,Void,Void>{
            @Override
            protected Void doInBackground(Void... voids) {
                List<Attachments> count =MediaLinesDatabase.getMediaLinesDatabase(getActivity().getApplicationContext()).attachmentsDao().getAllAttachments();
                System.out.println("Count" + count.size());
                return null;
            }
        }

        class GetLocCountTask extends AsyncTask<Void,Void,Void>{
            @Override
            protected Void doInBackground(Void... voids) {
                List<EditedLocations> count =MediaLinesDatabase.getMediaLinesDatabase(getActivity().getApplicationContext()).editedLocationsDao().getAllEditedLocations();
                System.out.println("Loc Count" + count.size());
                return null;
            }
        }

        new GetNotesTask().execute();
        new GetAttachmentsCountTask().execute();
        new GetLocCountTask().execute();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(data != null){
            NoteWithData noteWithData = (NoteWithData) data.getSerializableExtra("note");
            if(data.getBooleanExtra("isNoteUpdated", false)){
                int position = data.getIntExtra("position", -1);
                NoteWithData removedNote = notesList.remove(noteClickedPosition);
                notesList.add(noteClickedPosition, noteWithData);
                int indexInIntactSource = intactNotesList.indexOf(removedNote);
                intactNotesList.remove(removedNote);
                intactNotesList.add(indexInIntactSource, noteWithData);
                notesAdapter.setIntactDataSource(intactNotesList);
                notesAdapter.notifyItemChanged(noteClickedPosition);
            }
            else {

                notesList.add(0, noteWithData);
                intactNotesList.add(0, noteWithData);
                notesAdapter.setIntactDataSource(intactNotesList);
                notesAdapter.notifyItemInserted(0);
                notesRecyclerView.smoothScrollToPosition(0);
            }
        }
        collapseSearchView();

    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        notesAdapter.getFilter().filter(newText);
        return false;
    }

    public void collapseSearchView(){
        searchView.setIconified(true);
        MenuItemCompat.collapseActionView(mSearchMenuItem);
    }
}
