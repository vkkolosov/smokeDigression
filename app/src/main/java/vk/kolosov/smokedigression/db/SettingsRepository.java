package vk.kolosov.smokedigression.db;

import android.content.Context;

import androidx.room.Room;

import vk.kolosov.smokedigression.db.dao.SettingsDao;
import vk.kolosov.smokedigression.db.entity.SettingsEntity;

public class SettingsRepository {

    private String settingsRepositoryName = "settingsentity";
    public SettingsDao settingsDao;

    private SettingsRepository (Context context) {
        SettingsDatabase db = Room.databaseBuilder(context, SettingsDatabase.class, settingsRepositoryName)
                .fallbackToDestructiveMigration() //СТИРАЕТ ВСЕ ПРИ МИГРАЦИИ
                .build();

        settingsDao = db.settingsDao();
    }

    private static SettingsRepository INSTANCE = null;

    public static void initialize(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new SettingsRepository(context);
        }
    }

    public static SettingsRepository get() {
        return INSTANCE;
    }
}
