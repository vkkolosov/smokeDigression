package vk.kolosov.smokedigression.db.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.time.LocalDateTime;

@Entity
public class SettingsEntity {
    @PrimaryKey
    public int uid;

    @ColumnInfo(name = "cigarettes")
    public String cigarettes;

    @ColumnInfo(name = "cost")
    public String cost;

    @ColumnInfo(name = "sleep")
    public String sleep;

    @ColumnInfo(name = "sleepw")
    public String sleepW;

    @ColumnInfo(name = "rate")
    public String rate;

    @ColumnInfo(name = "date")
    public LocalDateTime date;

    @ColumnInfo(name = "set_on")
    public Integer setOn;
}
