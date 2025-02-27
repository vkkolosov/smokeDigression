package vk.kolosov.smokedigression.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;

import vk.kolosov.smokedigression.db.entity.SettingsEntity;

@Dao
public interface SettingsDao {
    @Query("SELECT * FROM settingsentity")
    ListenableFuture<List<SettingsEntity>> getAll();

    @Query("SELECT * FROM settingsentity WHERE uid IN (:settingsEntityIds)")
    ListenableFuture<List<SettingsEntity>> loadAllByIds(int[] settingsEntityIds);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    ListenableFuture<List<Long>> insertAll(SettingsEntity... settingsEntities);

    @Delete
    ListenableFuture<Integer> delete(SettingsEntity settingsEntity);
}
