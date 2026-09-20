package vk.kolosov.smokedigression.util;

import android.content.Context;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import vk.kolosov.smokedigression.db.dao.SmokedCigarettesDao;
import vk.kolosov.smokedigression.db.entity.SmokedCigarettesEntity;

public class TestDatabaseSeeder {

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void seedTestData(Context context, SmokedCigarettesDao dao, ExecutorService executor) {
        executor.execute(() -> {
            // 1. Очищаем старую базу для чистоты эксперимента
            dao.deleteAll();

            List<SmokedCigarettesEntity> testList = new ArrayList<>();

            // --- 1. ОКТЯБРЬ 2025 (Начало активности) ---
            testList.add(new SmokedCigarettesEntity(LocalDateTime.of(2025, 10, 15, 10, 30, 0), true));
            testList.add(new SmokedCigarettesEntity(LocalDateTime.of(2025, 10, 20, 18, 45, 0), true));

            // --- НОЯБРЬ 2025 ПУСТОЙ (Пропускаем полностью) ---

            // --- 2. ДЕКАБРЬ 2025 (Канун Нового Года) ---
            testList.add(new SmokedCigarettesEntity(LocalDateTime.of(2025, 12, 31, 15, 0, 0), true));
            testList.add(new SmokedCigarettesEntity(LocalDateTime.of(2025, 12, 31, 22, 15, 0), true));

            // --- 3. ПЕРЕХОД ГОДА И КРАЕВОЙ СЛУЧАЙ 00:00:00 ---
            // Точка ровно в полночь при смене года
            testList.add(new SmokedCigarettesEntity(LocalDateTime.of(2026, 1, 1, 0, 0, 0), true));
            testList.add(new SmokedCigarettesEntity(LocalDateTime.of(2026, 1, 1, 12, 30, 0), true));
            testList.add(new SmokedCigarettesEntity(LocalDateTime.of(2026, 1, 1, 19, 10, 0), true));

            testList.add(new SmokedCigarettesEntity(LocalDateTime.of(2026, 1, 5, 14, 20, 0), true));

            // --- ФЕВРАЛЬ 2026 ПУСТОЙ (Пропускаем полностью) ---

            // --- 5. МАРТ 2026 ---
            testList.add(new SmokedCigarettesEntity(LocalDateTime.of(2026, 3, 10, 9, 15, 0), true));

            // Вставляем все записи в БД Room
            for (SmokedCigarettesEntity entity : testList) {
                dao.insert(entity);
            }

            // Показываем Toast об успехе
            if (context instanceof android.app.Activity) {
                ((android.app.Activity) context).runOnUiThread(() ->
                        Toast.makeText(context, "Тестовая база (2025-2026) загружена!", Toast.LENGTH_LONG).show()
                );
            }
        });
    }
}