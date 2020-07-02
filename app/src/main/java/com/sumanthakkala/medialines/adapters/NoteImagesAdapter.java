package com.sumanthakkala.medialines.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.sumanthakkala.medialines.R;
import com.sumanthakkala.medialines.listeners.NoteImagesListener;
import com.sumanthakkala.medialines.listeners.NotesListener;
import com.sumanthakkala.medialines.viewmodels.NoteImageViewModel;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

public class NoteImagesAdapter extends RecyclerView.Adapter<NoteImagesAdapter.NoteImageViewHolder>{

    private List<NoteImageViewModel> noteImagesList;
    private NoteImagesListener imagesListener;
    private static Context context = null;

    public NoteImagesAdapter(List<NoteImageViewModel> noteImagesList, NoteImagesListener noteImagesListener) {
        this.noteImagesList = noteImagesList;
        this.imagesListener = noteImagesListener;
    }

    @NonNull
    @Override
    public NoteImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        return new NoteImageViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.image_container,
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull NoteImageViewHolder holder, final int position) {
        holder.setImageViewData(noteImagesList.get(position));
        holder.noteImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imagesListener.onImageCLicked(noteImagesList.get(position).imageUniqueFileName, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return noteImagesList.size();
    }

    static class NoteImageViewHolder extends RecyclerView.ViewHolder{

        private ImageView noteImageView;
        public NoteImageViewHolder(@NonNull View itemView) {
            super(itemView);

            noteImageView = itemView.findViewById(R.id.noteImageView);
        }

        void setImageViewData(NoteImageViewModel noteImageViewModel){
            if(noteImageViewModel.imageUniqueFileName != null){
                try {
                    File file = new File(context.getExternalFilesDir(null), noteImageViewModel.imageUniqueFileName);
                    FileInputStream fis = new FileInputStream(file);
                    Bitmap bitmap = BitmapFactory.decodeStream(fis);
                    fis.close();
                    noteImageView.setImageBitmap(bitmap);
                    noteImageView.setVisibility(View.VISIBLE);
                } catch (FileNotFoundException e) {
                    Toast.makeText(itemView.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
