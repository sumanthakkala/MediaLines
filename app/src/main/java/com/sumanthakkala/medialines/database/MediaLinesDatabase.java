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
import com.sumanthakkala.medialines.entities.Attachments;
import com.sumanthakkala.medialines.entities.EditedLocations;
import com.sumanthakkala.medialines.entities.Note;

@Database(entities = {Note.class, EditedLocations.class, Attachments.class}, version = 2, exportSchema = false)
public abstract class MediaLinesDatabase extends RoomDatabase {

    private static MediaLinesDatabase mediaLinesDatabase;

    static final Migration activeArchiveMigration = new Migration(1,2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE 'notes' ADD COLUMN 'is_active' INT NOT NULL DEFAULT 1");
        }
    };

    public static synchronized MediaLinesDatabase getMediaLinesDatabase(Context context){
        if (mediaLinesDatabase == null){
            mediaLinesDatabase = Room.databaseBuilder(
                    context,
                    MediaLinesDatabase.class,
                    "media_lines_database"
            ).addMigrations(activeArchiveMigration).build();
        }
        return mediaLinesDatabase;
    }

    public abstract NoteDao noteDao();
    public abstract EditedLocationsDao editedLocationsDao();
    public abstract AttachmentsDao attachmentsDao();

}
