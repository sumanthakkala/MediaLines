package com.sumanthakkala.medialines.dao;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.sumanthakkala.medialines.entities.Attachments;
import java.util.List;

@Dao
public interface AttachmentsDao {
    @Query("SELECT * FROM attachments ORDER BY attachmentId DESC")
    List<Attachments> getAllAttachments();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertAttachment(Attachments attachment);

    @Delete
    void deleteEditedLocation(Attachments attachment);
}