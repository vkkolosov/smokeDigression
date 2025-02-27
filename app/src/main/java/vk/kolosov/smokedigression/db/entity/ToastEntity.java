package vk.kolosov.smokedigression.db.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class ToastEntity {
    public static int toastNumber = 12;

    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "toast")
    public String toast;

    @ColumnInfo(name = "marked")
    public boolean marked;

}
