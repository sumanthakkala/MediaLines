package com.sumanthakkala.medialines.dao;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.sumanthakkala.medialines.entities.Attachments;
import com.sumanthakkala.medialines.entities.EditedLocations;

import java.util.List;

@Dao
public interface AttachmentsDao {
    @Query("SELECT * FROM attachments ORDER BY attachmentId DESC")
    List<Attachments> getAllAttachments();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertAttachment(Attachments attachment);

    @Query("DELETE FROM attachments WHERE attachment_unique_file_name = :fileName")
    void deleteAttachmentByUniqueFileName(String fileName);

    @Delete
    void deleteAttachment(Attachments attachment);

}
