package com.sumanthakkala.medialines.ui.home;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.sumanthakkala.medialines.R;
import com.sumanthakkala.medialines.activities.CreateNoteActivity;
import com.sumanthakkala.medialines.adapters.NotesAdapter;
import com.sumanthakkala.medialines.database.MediaLinesDatabase;
import com.sumanthakkala.medialines.entities.NoteWithData;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.sumanthakkala.medialines.listeners.NotesListener;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements NotesListener {
    public static final int REQUEST_CODE_ADD_NOTE = 1;
    public static final int REQUEST_CODE_UPDATE_NOTE = 2;


    private RecyclerView notesRecyclerView;
    private BottomAppBar quickActions;
    private List<NoteWithData> notesList;
    private NotesAdapter notesAdapter;

    private int noteClickedPosition = -1;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        FloatingActionButton fab = root.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(getActivity().getApplicationContext(), CreateNoteActivity.class),
                        REQUEST_CODE_ADD_NOTE);
            }
        });

        quickActions = root.findViewById(R.id.layoutQuickActions);

        notesRecyclerView = root.findViewById(R.id.notesRecyclerView);
        notesRecyclerView.setLayoutManager(
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        );

//        notesRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
//                super.onScrollStateChanged(recyclerView, newState);
//
//                if (!recyclerView.canScrollVertically(1) && newState==RecyclerView.SCROLL_STATE_IDLE) {
//                    quickActions.animate().alpha(0.0f).setDuration(500);;
//                }
//                else {
//                    quickActions.animate().alpha(0.95f).setDuration(100);;
//                }
//            }
//        });

        notesList = new ArrayList<>();
        notesAdapter = new NotesAdapter(notesList, this);
        notesRecyclerView.setAdapter(notesAdapter);
        getNotes();
        return root;
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
                    notesAdapter.notifyDataSetChanged();
                }
            }
        }

        new GetNotesTask().execute();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(data != null){
            NoteWithData noteWithData = (NoteWithData) data.getSerializableExtra("note");
            if(data.getBooleanExtra("isNoteUpdated", false)){
                int position = data.getIntExtra("position", -1);
                notesList.remove(noteClickedPosition);
                notesList.add(noteClickedPosition, noteWithData);
                notesAdapter.notifyItemChanged(noteClickedPosition);
            }
            else {

                notesList.add(0, noteWithData);
                notesAdapter.notifyItemInserted(0);
                notesRecyclerView.smoothScrollToPosition(0);
            }
        }

    }

}
