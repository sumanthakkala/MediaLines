package com.sumanthakkala.medialines.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sumanthakkala.medialines.R;
import com.sumanthakkala.medialines.entities.NoteWithData;
import com.makeramen.roundedimageview.RoundedImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder> {

    private List<NoteWithData> noteWithData;
    private static Context context;

    public NotesAdapter(List<NoteWithData> notes) {
        this.noteWithData = notes;
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
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        holder.setNote(noteWithData.get(position));
    }

    @Override
    public int getItemCount() {
        return noteWithData.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder{

        TextView titleTV, noteDescriptionTV, dateTimeTV;
        LinearLayout noteLayout;
        RoundedImageView roundedImageView;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);

            titleTV = itemView.findViewById(R.id.titleTextView);
            noteDescriptionTV = itemView.findViewById(R.id.noteDescriptionTextView);
            dateTimeTV = itemView.findViewById(R.id.dateTimeTextView);
            noteLayout = itemView.findViewById(R.id.noteLayout);
            roundedImageView = itemView.findViewById(R.id.noteImageRoundedView);
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
                String fileName = noteWithData.attachments.get(0).getAttachmentUniqueFileName();

                if(fileName != null){
                    try {
                        File file = new File(context.getExternalFilesDir(null), fileName);
                        FileInputStream fis = new FileInputStream(file);
                        Bitmap bitmap = BitmapFactory.decodeStream(fis);
                        fis.close();
                        roundedImageView.setImageBitmap(bitmap);
                        roundedImageView.setVisibility(View.VISIBLE);
                    } catch (FileNotFoundException e) {
                        Toast.makeText(itemView.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
