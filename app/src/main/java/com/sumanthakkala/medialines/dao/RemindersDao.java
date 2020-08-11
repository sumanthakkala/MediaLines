package com.sumanthakkala.medialines.dao;
        import androidx.room.Dao;
        import androidx.room.Delete;
        import androidx.room.Insert;
        import androidx.room.OnConflictStrategy;
        import androidx.room.Query;
        import com.sumanthakkala.medialines.entities.EditedLocations;
        import com.sumanthakkala.medialines.entities.Reminders;

        import java.util.List;

@Dao
public interface RemindersDao {

    @Query("SELECT * FROM reminders ORDER BY reminderId DESC")
    List<Reminders> getAllReminders();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertReminder(Reminders reminder);

    @Delete
    void deleteReminder(Reminders reminder);

    @Query("DELETE FROM reminders WHERE associated_note_id = :noteId")
    void deleteReminderByNoteId(long noteId);

}