package com.sumanthakkala.medialines.entities;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
import java.io.Serializable;

import static androidx.room.ForeignKey.CASCADE;

@Entity(tableName = "attachments", foreignKeys = @ForeignKey(entity = Note.class, parentColumns = "noteId", childColumns = "associated_note_id", onDelete = CASCADE))
public class Attachments implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private long attachmentId;

    @ColumnInfo(name = "date_time")
    private String dateTime;

    @ColumnInfo(name = "associated_note_id")
    private Long associatedNoteId;

    @ColumnInfo(name = "attachment_type")
    private String attachmentType;

    @ColumnInfo(name = "attachment_unique_file_name")
    private String attachmentUniqueFileName;

    public long getAttachmentId() {
        return attachmentId;
    }

    public void setAttachmentId(long attachmentId) {
        this.attachmentId = attachmentId;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public Long getAssociatedNoteId() {
        return associatedNoteId;
    }

    public void setAssociatedNoteId(Long associatedNoteId) {
        this.associatedNoteId = associatedNoteId;
    }

    public String getAttachmentUniqueFileName() {
        return attachmentUniqueFileName;
    }

    public void setAttachmentUniqueFileName(String attachmentUniqueFileName) {
        this.attachmentUniqueFileName = attachmentUniqueFileName;
    }

    public String getAttachmentType() {
        return attachmentType;
    }

    public void setAttachmentType(String attachmentType) {
        this.attachmentType = attachmentType;
    }

}
