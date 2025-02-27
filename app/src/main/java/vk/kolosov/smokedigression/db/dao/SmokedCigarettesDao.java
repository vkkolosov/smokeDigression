package vk.kolosov.smokedigression.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;

import vk.kolosov.smokedigression.db.entity.SmokedCigarettesEntity;

@Dao
public interface SmokedCigarettesDao {
    @Query("SELECT * FROM smokedcigarettesentity")
    ListenableFuture<List<SmokedCigarettesEntity>> getAll();

    //WHERE SUBSTR(event_date,1,8) = '2023-02-' -- Single Month
    @Query("SELECT * FROM smokedcigarettesentity WHERE SUBSTR(date,1,7) = :yearMonth")
    ListenableFuture<List<SmokedCigarettesEntity>> getByMonth(String yearMonth);

    @Insert
    ListenableFuture<Void> insert(SmokedCigarettesEntity smokedCigarettesEntity);

    @Query("DELETE FROM smokedcigarettesentity")
    ListenableFuture<Void> deleteAll();

    @Query("SELECT * FROM smokedcigarettesentity ORDER BY date DESC LIMIT 1")
    ListenableFuture<SmokedCigarettesEntity> getLast();
}
