package com.sumanthakkala.medialines.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.sumanthakkala.medialines.R;
import com.sumanthakkala.medialines.entities.Attachments;
import com.sumanthakkala.medialines.entities.NoteWithData;
import com.makeramen.roundedimageview.RoundedImageView;
import com.sumanthakkala.medialines.listeners.NotesListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder> {

    private List<NoteWithData> noteWithData;
    private NotesListener notesListener;
    private static Context context;

    private boolean multiSelect = false;
    private List<NoteWithData> selectedNotes = new ArrayList<>();
    private List<LinearLayout> selectedNotesLayouts = new ArrayList<>();

    public NotesAdapter(List<NoteWithData> notes, NotesListener listener) {
        this.noteWithData = notes;
        this.notesListener = listener;
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        return new NoteViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.note_container_layout,
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull final NoteViewHolder holder, final int position) {
        holder.roundedImageViewContainer.setVisibility(View.GONE);
        holder.attachmentsCount.setVisibility(View.VISIBLE);
        holder.setNote(noteWithData.get(position));
        holder.noteLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(multiSelect){
                    selectNote(holder.noteLayout, noteWithData.get(position));
                }
                else {
                    notesListener.onNoteCLicked(noteWithData.get(position), position);
                }
            }
        });
        holder.noteLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                notesListener.onMultiSelectBegin();
                if(!multiSelect){
                    multiSelect = true;
                    selectNote(holder.noteLayout, noteWithData.get(position));
                }
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return noteWithData.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        //Return the stable ID for the item at position
        return noteWithData.get(position).note.getNoteId();
    }

    private void selectNote(LinearLayout noteLayout, NoteWithData noteData) {
        if(selectedNotes.contains(noteData)){
            selectedNotes.remove(noteData);
            selectedNotesLayouts.remove(noteLayout);
            noteLayout.setAlpha(1.0f);
            notesListener.onNoteClickInMultiSelectMode(noteData, 0);
            if(selectedNotes.size() == 0){
                multiSelect = false;
                notesListener.onMultiSelectEnd();
            }
        }
        else {
            selectedNotes.add(noteData);
            selectedNotesLayouts.add(noteLayout);
            noteLayout.setAlpha(0.2f);
            notesListener.onNoteClickInMultiSelectMode(noteData, 1);
        }
    }

    public void cancelMultiSelect(){
        multiSelect = false;
        for(LinearLayout layout: selectedNotesLayouts){
            layout.setAlpha(1.0f);
        }
        selectedNotes.clear();
        selectedNotesLayouts.clear();
        notesListener.onMultiSelectEnd();
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder{

        TextView titleTV, noteDescriptionTV, dateTimeTV;
        LinearLayout noteLayout;
        RoundedImageView roundedImageView;
        ConstraintLayout roundedImageViewContainer;
        TextView attachmentsCount;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);

            titleTV = itemView.findViewById(R.id.titleTextView);
            noteDescriptionTV = itemView.findViewById(R.id.noteDescriptionTextView);
            dateTimeTV = itemView.findViewById(R.id.dateTimeTextView);
            noteLayout = itemView.findViewById(R.id.noteLayout);
            roundedImageView = itemView.findViewById(R.id.noteImageRoundedView);
            roundedImageViewContainer = itemView.findViewById(R.id.roundedImageViewContainer);
            attachmentsCount = itemView.findViewById(R.id.attachmentsCountTV);

        }

        void setNote(NoteWithData noteWithData){
            titleTV.setText(noteWithData.note.getTitle());
            if(noteWithData.note.getNoteText().trim().isEmpty()){
                noteDescriptionTV.setVisibility(View.GONE);
            }
            else {
                noteDescriptionTV.setText(noteWithData.note.getNoteText());
            }
            dateTimeTV.setText(noteWithData.note.getDateTime());

            GradientDrawable gradientDrawable = (GradientDrawable) noteLayout.getBackground();
            if(noteWithData.note.getColor() != null){
                gradientDrawable.setColor(Color.parseColor(noteWithData.note.getColor()));
            }
            else {
                gradientDrawable.setColor(Color.parseColor("#333333"));
            }

            if(noteWithData.attachments.size() > 0){
                String fileName = "";
                for(Attachments attach : noteWithData.attachments){
                    if(attach.getAttachmentType().equals("image")){
                        fileName = attach.getAttachmentUniqueFileName();
                        break;
                    }
                }

                if(fileName != ""){
//                    try {
                        File file = new File(context.getExternalFilesDir(null), fileName);
                        Picasso.get().load(file).into(roundedImageView);
//                        FileInputStream fis = new FileInputStream(file);
//                        Bitmap bitmap = BitmapFactory.decodeStream(fis);
//                        fis.close();
//                        roundedImageView.setImageBitmap(bitmap);
                        roundedImageViewContainer.setVisibility(View.VISIBLE);
//                    } catch (FileNotFoundException e) {
//                        Toast.makeText(itemView.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
                }

                if(noteWithData.attachments.size() > 1){
                    attachmentsCount.setText("+" + (noteWithData.attachments.size() - 1));
                }
                else {
                    attachmentsCount.setVisibility(View.GONE);
                }
            }
        }
    }
}
