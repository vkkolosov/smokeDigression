package vk.kolosov.smokedigression.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import vk.kolosov.smokedigression.db.converters.DateConverter;
import vk.kolosov.smokedigression.db.dao.SmokedCigarettesDao;
import vk.kolosov.smokedigression.db.entity.SmokedCigarettesEntity;

@Database(entities = {SmokedCigarettesEntity.class}, version = 5, exportSchema = false)
@TypeConverters({DateConverter.class})
public abstract class SmokedCigarettesDatabase extends RoomDatabase {
    public abstract SmokedCigarettesDao smokedCigarettesDao();
}
