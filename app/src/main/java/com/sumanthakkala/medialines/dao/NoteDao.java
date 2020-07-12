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
    @Query("SELECT * FROM notes WHERE is_active = 1 ORDER BY noteId DESC")
    public List<NoteWithData> getActiveNotesWithData();

    @Transaction
    @Query("SELECT * FROM notes WHERE is_active = 0 ORDER BY noteId DESC")
    public List<NoteWithData> getArchiveNotesWithData();

    @Transaction
    @Query("SELECT * FROM notes WHERE noteId = :noteID")
    public NoteWithData getNoteWithDataByNoteId(long noteID);

    @Transaction
    @Query("DELETE FROM notes WHERE noteId in (:ids)")
    public void deleteNotesWithId(List<Long> ids);

    @Transaction
    @Query("UPDATE notes SET is_active = 0 WHERE noteId in (:ids)")
    public void archiveNotesWithId(List<Long> ids);

    @Transaction
    @Query("UPDATE notes SET is_active = 1 WHERE noteId in (:ids)")
    public void unArchiveNotesWithId(List<Long> ids);

    @Transaction
    @Query("UPDATE notes SET is_bookmarked = 1 WHERE noteId in (:ids)")
    public void bookmarkNotesWithId(List<Long> ids);

    @Transaction
    @Query("UPDATE notes SET is_bookmarked = 0 WHERE noteId in (:ids)")
    public void unBookmarkNotesWithId(List<Long> ids);

}
