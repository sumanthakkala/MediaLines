package com.example.medialines.entities;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.io.Serializable;

@Entity(tableName = "attachments")
public class Attachments implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int attachmentId;

    @ColumnInfo(name = "date_time")
    private String dateTime;

    @ColumnInfo(name = "associated_note_id")
    private Long associatedNoteId;

    @ColumnInfo(name = "attachment_type")
    private String attachmentType;

    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    private byte[] attachmentBlob;

    public int getAttachmentId() {
        return attachmentId;
    }

    public void setAttachmentId(int attachmentId) {
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

    public String getAttachmentType() {
        return attachmentType;
    }

    public void setAttachmentType(String attachmentType) {
        this.attachmentType = attachmentType;
    }

    public byte[] getAttachmentBlob() {
        return attachmentBlob;
    }

    public void setAttachmentBlob(byte[] attachmentBlob) {
        this.attachmentBlob = attachmentBlob;
    }
}
