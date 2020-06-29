package com.example.medialines.dao;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.example.medialines.entities.EditedLocations;
import java.util.List;

@Dao
public interface EditedLocationsDao {

    @Query("SELECT * FROM editedLocations ORDER BY editedLocationId DESC")
    List<EditedLocations> getAllEditedLocations();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertEditedLocation(EditedLocations location);

    @Delete
    void deleteEditedLocation(EditedLocations location);

}