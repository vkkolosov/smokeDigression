package vk.kolosov.smokedigression.widget;

import static android.app.PendingIntent.FLAG_MUTABLE;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import vk.kolosov.smokedigression.R;
import vk.kolosov.smokedigression.db.SmokedCigarettesRepository;
import vk.kolosov.smokedigression.db.dao.SmokedCigarettesDao;
import vk.kolosov.smokedigression.db.entity.SmokedCigarettesEntity;

public class Widget extends AppWidgetProvider {

    final String LOG_TAG = "WidgetLogs";
    public static String CLICK_ACTION = "Clicked!";
    public static SmokedCigarettesDao smokedCigarettesDao;

    private static LocalDateTime lastDate;
    private static ExecutorService executors = Executors.newCachedThreadPool();

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        updateLastCigarette(context);
        Toast.makeText(context, context.getString(R.string.widget_added), Toast.LENGTH_SHORT).show();
        Log.d(LOG_TAG, "onEnabled");
    }

    //android:updatePeriodMillis="2400000"
    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        SmokedCigarettesRepository.initialize(context);
        smokedCigarettesDao = SmokedCigarettesRepository.get().smokedCigarettesDao;
        ListenableFuture<List<SmokedCigarettesEntity>> futureInDay = smokedCigarettesDao.getAll();
        Futures.addCallback(futureInDay, new FutureCallback<List<SmokedCigarettesEntity>>() {
            @RequiresApi(api = Build.VERSION_CODES.P)
            @Override
            public void onSuccess(List<SmokedCigarettesEntity> result) {
                //В БД УЖЕ ВСЕ УПОРЯДОЧЕНО
                if (!result.isEmpty()) {
                    lastDate = result.get(result.size() - 1).date;
                } else {
                    lastDate = LocalDateTime.now();
                }

                Duration difference = Duration.between(lastDate, LocalDateTime.now());
                long seconds = difference.getSeconds() * 1000;
                for (int appWidgetId : appWidgetIds) {
                    initWidgets(context, appWidgetManager, appWidgetId, seconds);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d("SmokeDigression", "failed to get entity from db");
            }
        }, executors);
        //Toast.makeText(context, "Widget added! ", Toast.LENGTH_SHORT).show();
        Log.d(LOG_TAG, "onUpdate " + Arrays.toString(appWidgetIds));
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    static void initWidgets(Context context, AppWidgetManager appWidgetManager, int appWidgetId, long seconds) {
        Intent intent = new Intent(context, Widget.class);
        intent.setAction(CLICK_ACTION);

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
        views.setChronometer(R.id.timer, SystemClock.elapsedRealtime() - seconds, "%tH:%tM:%tS", true);
        views.setOnClickPendingIntent(R.id.smokedButton, getPendingSelfIntent(context, CLICK_ACTION));

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    protected static PendingIntent getPendingSelfIntent(Context context, String action) {
        Intent intent = new Intent(context, Widget.class);
        intent.setAction(action);
        return PendingIntent.getBroadcast(context, 0, intent, FLAG_MUTABLE);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(final Context context, final Intent intent) {
        super.onReceive(context, intent);
        String action = intent.getAction();
        if (CLICK_ACTION.equals(action)) {
            updateWidgets(context); // ->
        }
        Log.d(LOG_TAG, "onReceive");
    }
    //-> onReceive сломался
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void updateWidgets(Context context) {
        //Toast.makeText(context, "updating widgets", Toast.LENGTH_SHORT).show();
        SmokedCigarettesRepository.initialize(context);
        smokedCigarettesDao = SmokedCigarettesRepository.get().smokedCigarettesDao;
        smokedCigarettesDao.insert(new SmokedCigarettesEntity(LocalDateTime.now(), true));

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
        views.setChronometer(R.id.timer, SystemClock.elapsedRealtime(), "%tH:%tM:%tS", true);

        ComponentName thisAppWidgetComponentName = new ComponentName("vk.kolosov.smokedigression", "vk.kolosov.smokedigression.widget.Widget");
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidgetComponentName);
        for (int appWidgetId : appWidgetIds) {
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
        //Toast.makeText(context, "Widget Clicked", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        Log.d(LOG_TAG, "onDeleted " + Arrays.toString(appWidgetIds));
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        Log.d(LOG_TAG, "onDisabled");
    }

    private void updateLastCigarette(Context context) {
        SmokedCigarettesRepository.initialize(context);
        smokedCigarettesDao = SmokedCigarettesRepository.get().smokedCigarettesDao;
        ListenableFuture<List<SmokedCigarettesEntity>> futureInDay = smokedCigarettesDao.getAll();
        Futures.addCallback(futureInDay, new FutureCallback<List<SmokedCigarettesEntity>>() {
            @RequiresApi(api = Build.VERSION_CODES.P)
            @Override
            public void onSuccess(List<SmokedCigarettesEntity> result) {
                //В БД УЖЕ ВСЕ УПОРЯДОЧЕНО
                if (!result.isEmpty()) {
                    lastDate = result.get(result.size() - 1).date;
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d("SmokeDigression", "failed to get entity from db");
            }
        }, executors);
    }
}
