package com.example.medialines.entities;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

public class NoteWithData {

    @Embedded public Note note;

    @Relation(
            parentColumn = "noteId",
            entityColumn = "associated_note_id"
    )
    public List<EditedLocations> editedLocations;

    @Relation(
            parentColumn = "noteId",
            entityColumn = "associated_note_id"
    )
    public List<Attachments> attachments;
}
