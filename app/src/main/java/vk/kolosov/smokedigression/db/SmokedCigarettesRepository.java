package vk.kolosov.smokedigression.db;

import android.content.Context;

import androidx.room.Room;

import vk.kolosov.smokedigression.db.dao.SmokedCigarettesDao;

public class SmokedCigarettesRepository {

    private String smokedCigarettesRepositoryName = "smokedcigarettesentity";
    public SmokedCigarettesDao smokedCigarettesDao;

    private SmokedCigarettesRepository(Context context) {
        SmokedCigarettesDatabase db = Room.databaseBuilder(context, SmokedCigarettesDatabase.class, smokedCigarettesRepositoryName)
                //.fallbackToDestructiveMigration() //СТИРАЕТ ВСЕ ПРИ МИГРАЦИИ
                .build();

        smokedCigarettesDao = db.smokedCigarettesDao();
    }

    private static SmokedCigarettesRepository INSTANCE = null;

    public static void initialize(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new SmokedCigarettesRepository(context);
        }
    }

    public static SmokedCigarettesRepository get() {
        return INSTANCE;
    }
}
