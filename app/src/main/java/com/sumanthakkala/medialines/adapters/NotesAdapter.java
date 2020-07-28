package com.sumanthakkala.medialines.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.sumanthakkala.medialines.R;
import com.sumanthakkala.medialines.constants.Constants;
import com.sumanthakkala.medialines.entities.Attachments;
import com.sumanthakkala.medialines.entities.NoteWithData;
import com.makeramen.roundedimageview.RoundedImageView;
import com.sumanthakkala.medialines.listeners.NotesListener;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder> implements Filterable {

    private List<NoteWithData> notesWithData = new ArrayList<>();
    private List<NoteWithData> intactNotesWithData = new ArrayList<>();
    private NotesListener notesListener;
    private static Context context;

    private boolean multiSelect = false;
    private List<NoteWithData> selectedNotes = new ArrayList<>();
    private List<LinearLayout> selectedNotesLayouts = new ArrayList<>();

    public NotesAdapter(List<NoteWithData> notes, NotesListener listener) {
        this.notesWithData = notes;
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
        holder.setNote(notesWithData.get(position));
        holder.noteLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(multiSelect){
                    selectNote(holder.noteLayout, notesWithData.get(position));
                }
                else {
                    notesListener.onNoteCLicked(notesWithData.get(position), position);
                }
            }
        });
        holder.noteLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                notesListener.onMultiSelectBegin();
                if(multiSelect){
                    multiSelect = true;
                    selectNote(holder.noteLayout, notesWithData.get(position));
                }
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return notesWithData.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        //Return the stable ID for the item at position
        return notesWithData.get(position).note.getNoteId();
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    Filter filter = new Filter() {

        //runs on background thread
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            List<NoteWithData> filteredList = new ArrayList<>();
            if(charSequence.toString().isEmpty()){
                filteredList.addAll(intactNotesWithData);
            }
            else {
                for(NoteWithData noteWithData: intactNotesWithData){
                    if((noteWithData.note.getTitle() != null && noteWithData.note.getTitle().toLowerCase().contains(charSequence.toString().toLowerCase()))
                            || (noteWithData.note.getNoteText() != null && noteWithData.note.getNoteText().toLowerCase().contains(charSequence.toString().toLowerCase()))
                            || (noteWithData.note.getWebLink() != null && noteWithData.note.getWebLink().toLowerCase().contains(charSequence.toString().toLowerCase()))){
                        filteredList.add(noteWithData);
                    }
                }
            }

            FilterResults filterResults = new FilterResults();
            filterResults.values = filteredList;
            return filterResults;
        }

        //runs on UI thread
        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {

            notesWithData.clear();
            notesWithData.addAll((Collection<? extends NoteWithData>) filterResults.values);
            notifyDataSetChanged();
            notesListener.onFilterNotesDone();
        }
    };

    public List<NoteWithData> getDataSource(){
        return notesWithData;
    }

    private void selectNote(LinearLayout noteLayout, NoteWithData noteData) {
        if(selectedNotes.contains(noteData)){
            selectedNotes.remove(noteData);
            selectedNotesLayouts.remove(noteLayout);
            noteLayout.setAlpha(1.0f);
            notesListener.onNoteClickInMultiSelectMode(noteData, 0);
            if(selectedNotes.size() == 0){
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

    public void setIntactDataSource(List<NoteWithData> data){
        intactNotesWithData.clear();
        intactNotesWithData.addAll(data);
    }
    public void setMultiSelectMode(boolean option){
        multiSelect = option;
    }
    public boolean getMultiSelectMode(){
        return multiSelect;
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
            if(noteWithData.note.getTitle().trim().isEmpty()){
                titleTV.setVisibility(View.GONE);
            }
            else {
                titleTV.setText(noteWithData.note.getTitle());
                titleTV.setVisibility(View.VISIBLE);
            }
            String noteText = noteWithData.note.getNoteText();
            String regex = "\\Q" + Constants.CHECKBOXES_SEPERATOR + "\\E";
            if(noteText.trim().isEmpty()){
                noteDescriptionTV.setVisibility(View.GONE);
            }
            else {
                if(noteText.contains(Constants.CHECKBOXES_SEPERATOR)){
                    String[] list = noteText.split(regex);
                    noteText = list[0];
                    String[] bookmarks = list[1].split("\n");
                    for(int i = 0; i< bookmarks.length; i++){
                        if(i == 0){
                            if(!noteText.isEmpty()){
                                noteText += "\n";
                            }
                            continue;
                        }
                        if(bookmarks[i].contains(Constants.CHECKBOX_VALUE_CHECKED.substring(2))){
                            bookmarks[i] = bookmarks[i].replace(Constants.CHECKBOX_VALUE_CHECKED.substring(1), Constants.CHECKBOX_DISPLAY_CHARACTER_CHECKED);
                        }
                        else {
                            bookmarks[i] = bookmarks[i].replace(Constants.CHECKBOX_VALUE_UNCHECKED.substring(1), Constants.CHECKBOX_DISPLAY_CHARACTER_UNCHECKED);
                        }
                        noteText += bookmarks[i];
                        noteDescriptionTV.setText(noteText);
                    }
                }
                else {
                    noteDescriptionTV.setText(noteText);
                }
                noteDescriptionTV.setVisibility(View.VISIBLE);
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
                    File file = new File(context.getExternalFilesDir(null), fileName);
                    Picasso.get().load(file).into(roundedImageView);
                    roundedImageViewContainer.setVisibility(View.VISIBLE);
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
