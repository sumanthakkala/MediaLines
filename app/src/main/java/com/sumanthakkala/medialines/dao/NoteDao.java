package com.sumanthakkala.medialines.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import com.sumanthakkala.medialines.entities.Note;
import com.sumanthakkala.medialines.entities.NoteWithData;

import java.util.List;

@Dao
public interface NoteDao {

    @Query("SELECT * FROM notes ORDER BY noteId DESC")
    List<Note> getAllNotes();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertNote(Note note);

    @Delete
    void deleteNote(Note note);

    @Transaction
    @Query("SELECT * FROM notes ORDER BY noteId DESC")
    public List<NoteWithData> getNotesWithData();
}
