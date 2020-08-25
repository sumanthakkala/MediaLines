package com.sumanthakkala.medialines.entities;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.io.Serializable;
import java.util.List;

public class NoteWithData implements Serializable {

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

    @Relation(
            parentColumn = "noteId",
            entityColumn = "associated_note_id"
    )
    public List<Reminders> reminder;
}
