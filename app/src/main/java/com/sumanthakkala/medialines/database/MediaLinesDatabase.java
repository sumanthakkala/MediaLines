package com.sumanthakkala.medialines.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.sumanthakkala.medialines.dao.AttachmentsDao;
import com.sumanthakkala.medialines.dao.EditedLocationsDao;
import com.sumanthakkala.medialines.dao.NoteDao;
import com.sumanthakkala.medialines.dao.RemindersDao;
import com.sumanthakkala.medialines.entities.Attachments;
import com.sumanthakkala.medialines.entities.EditedLocations;
import com.sumanthakkala.medialines.entities.Note;
import com.sumanthakkala.medialines.entities.Reminders;

@Database(entities = {Note.class, EditedLocations.class, Attachments.class, Reminders.class}, version = 3, exportSchema = false)
public abstract class MediaLinesDatabase extends RoomDatabase {

    private static MediaLinesDatabase mediaLinesDatabase;

    static final Migration activeArchiveMigration = new Migration(1,2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE 'notes' ADD COLUMN 'is_active' INT NOT NULL DEFAULT 1");
            database.execSQL("ALTER TABLE 'notes' ADD COLUMN 'is_bookmarked' INT NOT NULL DEFAULT 0");
        }
    };

    static final Migration remindersMigration = new Migration(2,3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `reminders` (`reminderId` INTEGER NOT NULL, `date_time_in_millis` INTEGER, `repeat_type` TEXT, `associated_note_id` INTEGER, PRIMARY KEY(`reminderId`), FOREIGN KEY(`associated_note_id`) REFERENCES `notes`(`noteId`) ON DELETE CASCADE )");
        }
    };

    public static synchronized MediaLinesDatabase getMediaLinesDatabase(Context context){
        if (mediaLinesDatabase == null){
            mediaLinesDatabase = Room.databaseBuilder(
                    context,
                    MediaLinesDatabase.class,
                    "media_lines_database"
            ).addMigrations(activeArchiveMigration).addMigrations(remindersMigration).build();
        }
        return mediaLinesDatabase;
    }

    public abstract NoteDao noteDao();
    public abstract EditedLocationsDao editedLocationsDao();
    public abstract AttachmentsDao attachmentsDao();
    public abstract RemindersDao remindersDao();

}
