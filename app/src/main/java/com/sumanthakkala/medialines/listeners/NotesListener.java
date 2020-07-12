package com.sumanthakkala.medialines.listeners;

import com.sumanthakkala.medialines.entities.NoteWithData;

public interface NotesListener {
    void onNoteCLicked(NoteWithData noteWithData, int position);
    void onMultiSelectBegin();
    void onMultiSelectEnd();
    void onNoteClickInMultiSelectMode(NoteWithData noteWithData, int action);
    void onFilterNotesDone();

}
