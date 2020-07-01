package com.sumanthakkala.medialines.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.sumanthakkala.medialines.dao.AttachmentsDao;
import com.sumanthakkala.medialines.dao.EditedLocationsDao;
import com.sumanthakkala.medialines.dao.NoteDao;
import com.sumanthakkala.medialines.entities.Attachments;
import com.sumanthakkala.medialines.entities.EditedLocations;
import com.sumanthakkala.medialines.entities.Note;

@Database(entities = {Note.class, EditedLocations.class, Attachments.class}, version = 1, exportSchema = false)
public abstract class MediaLinesDatabase extends RoomDatabase {

    private static MediaLinesDatabase mediaLinesDatabase;
    public static synchronized MediaLinesDatabase getMediaLinesDatabase(Context context){
        if (mediaLinesDatabase == null){
            mediaLinesDatabase = Room.databaseBuilder(
                    context,
                    MediaLinesDatabase.class,
                    "media_lines_database"
            ).build();
        }
        return mediaLinesDatabase;
    }

    public abstract NoteDao noteDao();
    public abstract EditedLocationsDao editedLocationsDao();
    public abstract AttachmentsDao attachmentsDao();

}
