package com.sumanthakkala.medialines.listeners;

import com.sumanthakkala.medialines.viewmodels.NoteAudioViewModel;
import com.sumanthakkala.medialines.viewmodels.NoteImageViewModel;

public interface NoteAudiosListener {
    void onPlayPauseCLicked(String uniqueAudioName, int position);
    void onDeleteAudioCLicked(NoteAudioViewModel audioViewModel);
}
