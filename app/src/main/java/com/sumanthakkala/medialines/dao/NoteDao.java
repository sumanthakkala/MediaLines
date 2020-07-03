package com.sumanthakkala.medialines.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.sumanthakkala.medialines.entities.Note;
import com.sumanthakkala.medialines.entities.NoteWithData;

import java.util.List;

@Dao
public interface NoteDao {

    @Query("SELECT * FROM notes ORDER BY noteId DESC")
    List<Note> getAllNotes();

    @Insert(onConflict = OnConflictStrategy.ABORT)
    long insertNote(Note note);

    @Update
    void updateNote(Note note);

    @Delete
    void deleteNote(Note note);

    @Transaction
    @Query("SELECT * FROM notes ORDER BY noteId DESC")
    public List<NoteWithData> getNotesWithData();

    @Transaction
    @Query("SELECT * FROM notes WHERE noteId = :noteID")
    public NoteWithData getNoteWithDataByNoteId(long noteID);

    @Transaction
    @Query("DELETE FROM notes WHERE noteId in (:ids)")
    public void deleteNotesWithId(List<Long> ids);

}
