package vk.kolosov.smokedigression.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import vk.kolosov.smokedigression.db.converters.DateConverter;
import vk.kolosov.smokedigression.db.dao.SettingsDao;
import vk.kolosov.smokedigression.db.entity.SettingsEntity;

@Database(entities = {SettingsEntity.class}, version = 4, exportSchema = false)
@TypeConverters({DateConverter.class})
public abstract class SettingsDatabase extends RoomDatabase {
    public abstract SettingsDao settingsDao();
}
