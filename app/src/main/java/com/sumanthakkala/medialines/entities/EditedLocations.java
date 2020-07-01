package com.sumanthakkala.medialines.entities;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.io.Serializable;

@Entity(tableName = "editedLocations")
public class EditedLocations implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int editedLocationId;

    @ColumnInfo(name = "date_time")
    private String dateTime;

    @ColumnInfo(name = "location")
    private String location;

    @ColumnInfo(name = "associated_note_id")
    private Long associatedNoteId;

    public int getEditedLocationId() {
        return editedLocationId;
    }

    public void setEditedLocationId(int editedLocationId) {
        this.editedLocationId = editedLocationId;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Long getAssociatedNoteId() {
        return associatedNoteId;
    }

    public void setAssociatedNoteId(Long associatedNoteId) {
        this.associatedNoteId = associatedNoteId;
    }

}
