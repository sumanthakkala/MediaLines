package com.sumanthakkala.medialines.listeners;

import com.sumanthakkala.medialines.viewmodels.NoteImageViewModel;

public interface NoteImagesListener {
    void onImageCLicked(String uniqueImageName, int position);
    void onDeleteImageCLicked(NoteImageViewModel imageViewModel);
}
