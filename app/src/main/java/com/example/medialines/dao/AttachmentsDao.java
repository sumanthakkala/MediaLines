package com.example.medialines.dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.example.medialines.entities.Attachments;
import java.util.List;

public interface AttachmentsDao {
    @Query("SELECT * FROM attachments ORDER BY attachmentId DESC")
    List<Attachments> getAllAttachments();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAttachment(Attachments attachment);

    @Delete
    void deleteEditedLocation(Attachments attachment);
}
