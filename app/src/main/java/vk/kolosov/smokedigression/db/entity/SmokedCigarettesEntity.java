package vk.kolosov.smokedigression.db.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
public class SmokedCigarettesEntity {
    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "date")
    public LocalDateTime date;

    @ColumnInfo(name = "marked")
    public boolean marked;

    public SmokedCigarettesEntity (LocalDateTime date, boolean marked) {
        this.date = date;
        this.marked = marked;
    }
}
