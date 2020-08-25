package com.sumanthakkala.medialines.entities;
        import androidx.room.ColumnInfo;
        import androidx.room.Entity;
        import androidx.room.ForeignKey;
        import androidx.room.PrimaryKey;
        import java.io.Serializable;

        import static androidx.room.ForeignKey.CASCADE;

@Entity(tableName = "reminders", foreignKeys = @ForeignKey(entity = Note.class, parentColumns = "noteId", childColumns = "associated_note_id", onDelete = CASCADE))
public class Reminders implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int reminderId;

    @ColumnInfo(name = "date_time_in_millis")
    private Long dateTimeInMillis;

    @ColumnInfo(name = "repeat_type")
    private String repeatType;

    @ColumnInfo(name = "associated_note_id")
    private Long associatedNoteId;


    public int getReminderId() {
        return reminderId;
    }

    public void setReminderId(int reminderId) {
        this.reminderId = reminderId;
    }

    public Long getDateTimeInMillis() {
        return dateTimeInMillis;
    }

    public void setDateTimeInMillis(Long dateTimeInMillis) {
        this.dateTimeInMillis = dateTimeInMillis;
    }

    public String getRepeatType() {
        return repeatType;
    }

    public void setRepeatType(String repeatType) {
        this.repeatType = repeatType;
    }

    public Long getAssociatedNoteId() {
        return associatedNoteId;
    }

    public void setAssociatedNoteId(Long associatedNoteId) {
        this.associatedNoteId = associatedNoteId;
    }
}
