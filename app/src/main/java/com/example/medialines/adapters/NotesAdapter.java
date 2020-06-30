package com.example.medialines.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medialines.R;
import com.example.medialines.entities.Note;

import java.util.List;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder> {

    private List<Note> notes;

    public NotesAdapter(List<Note> notes) {
        this.notes = notes;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NoteViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.note_container_layout,
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        holder.setNote(notes.get(position));
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder{

        TextView titleTV, noteDescriptionTV, dateTimeTV;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);

            titleTV = itemView.findViewById(R.id.titleTextView);
            noteDescriptionTV = itemView.findViewById(R.id.noteDescriptionTextView);
            dateTimeTV = itemView.findViewById(R.id.dateTimeTextView);
        }

        void setNote(Note note){
            titleTV.setText(note.getTitle());
            if(note.getNoteText().trim().isEmpty()){
                noteDescriptionTV.setVisibility(View.GONE);
            }
            else {
                noteDescriptionTV.setText(note.getNoteText());
            }
            dateTimeTV.setText(note.getDateTime());
        }
    }
}
