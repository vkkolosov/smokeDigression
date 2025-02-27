package vk.kolosov.smokedigression;

import static java.time.Instant.now;
import static java.time.LocalDateTime.ofInstant;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.util.TimeZone.getDefault;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.sql.Date;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import vk.kolosov.smokedigression.charts.CommonChart;
import vk.kolosov.smokedigression.db.SettingsRepository;
import vk.kolosov.smokedigression.db.SmokedCigarettesRepository;
import vk.kolosov.smokedigression.db.dao.SettingsDao;
import vk.kolosov.smokedigression.db.dao.SmokedCigarettesDao;
import vk.kolosov.smokedigression.db.entity.SettingsEntity;
import vk.kolosov.smokedigression.db.entity.SmokedCigarettesEntity;
import vk.kolosov.smokedigression.util.ScrollViewDates;

//TODO слева сделать кнопку INFO -> Описать, как работает приложение
//TODO разобраться с уведомлениями
//TODO делать ли настраиваемую частоту уведомлений?
//TODO запретить горизонтальное отображение


//TODO ВЫСТАВИТЬ НА PLAY MARKET ЗА 259 руб.
@RequiresApi(api = Build.VERSION_CODES.S)
public class MainActivity extends AppCompatActivity {
    //lateinit
    //https://stackoverflow.com/questions/42964397/kotlin-lateinit-correspondent-java
    private ActivityResultLauncher<Intent> activityResultLauncher; //вместо startActivityForResult

    private TextView setOnText;
    private ImageButton settingsButton;
    public static Integer setOn;

    private TextView lastCigarette;
    private TextView nonSmoke;

    @SuppressLint("StaticFieldLeak")
    private static TextView yourInterval;
    @SuppressLint("StaticFieldLeak")
    public static TextView dayEconomy;
    @SuppressLint("StaticFieldLeak")
    private static TextView monthEconomy;

    public static LineChart lineChart;
    private ImageButton left;
    private ImageButton right;
    private TextView inDay;
    private TextView inMonth;
    public static int monthPicked = 0;
    private Button smoked;
    private TextView donation;
    private TextView resetAll;
    private TextView i;

    public static LocalDateTime firstDate = LocalDateTime.now(); //calendar: first date
    public static LocalDateTime lastDate; //last time smoked cigarette
    public static LocalDate chosenDate = LocalDate.now(); //calendar
    public static LocalDate chosenMonth = LocalDate.now(); //calendar
    public static int currentCigarettesDay;
    public static int currentCigarettesMonth;

    public static ExecutorService executors = Executors.newCachedThreadPool();
    private SettingsDao settingsDao;
    private SmokedCigarettesDao smokedCigarettesDao;
    public static SettingsEntity settingsEntity;

    public static List<Entry> cachedAllSmokedCigarettesEntity;
    public static List<Entry> cachedMonthSmokedCigarettesEntity;

    private static String local_currency;

    @SuppressLint("ResourceType")
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        local_currency = getString(R.string.currency);

        yourInterval = findViewById(R.id.your_interval);
        dayEconomy = findViewById(R.id.day_economy);
        monthEconomy = findViewById(R.id.month_economy);

        i = findViewById(R.id.i);
        i.setOnClickListener(v -> {
            Uri uri = Uri.parse("https://smokedigression.ru/forum/");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        });

        left = findViewById(R.id.left);
        left.setOnClickListener(v -> {
            //сделать switch на month
            if (monthPicked == 0) {
                chosenDate = chosenDate.minusDays(1);
                CommonChart.setDayLineChart(this, smokedCigarettesDao, executors, lineChart, chosenDate);
                if (chosenDate.isEqual(firstDate.toLocalDate())) {
                    left.setClickable(false);
                }
                if (chosenDate.isBefore(LocalDate.now())) {
                    right.setClickable(true);
                }
            } else {
                chosenMonth = chosenMonth.minusMonths(1);
                CommonChart.setMonthLineChart(this, smokedCigarettesDao, executors, lineChart, chosenMonth);
                if (firstDate.getYear() <= LocalDate.now().getYear()
                        && chosenMonth.getMonth().getValue() <= firstDate.toLocalDate().getMonth().getValue()) {
                    left.setClickable(false);
                }
                if (chosenMonth.getYear() < LocalDate.now().getYear()
                        || chosenMonth.getMonth().getValue() < LocalDate.now().getMonth().getValue()) {
                    right.setClickable(true);
                }
            }
        });
        right = findViewById(R.id.right);
        right.setOnClickListener(v -> {
            if (monthPicked == 0) {
                chosenDate = chosenDate.plusDays(1);
                CommonChart.setDayLineChart(this, smokedCigarettesDao, executors, lineChart, chosenDate);
                if (chosenDate.isEqual(LocalDate.now())) {
                    right.setClickable(false);
                }
                if (chosenDate.isAfter(firstDate.toLocalDate())) {
                    left.setClickable(true);
                }
            } else {
                chosenMonth = chosenMonth.plusMonths(1);
                CommonChart.setMonthLineChart(this, smokedCigarettesDao, executors, lineChart, chosenMonth);
                if (chosenMonth.getYear() == LocalDate.now().getYear()
                        && chosenMonth.getMonth().getValue() == LocalDate.now().getMonth().getValue()) {
                    right.setClickable(false);
                }
                if (chosenMonth.getMonth().getValue() > firstDate.toLocalDate().getMonth().getValue()
                        && firstDate.getYear() <= LocalDate.now().getYear()) {
                    left.setClickable(true);
                }
            }
        });
        //инициализация
        if (chosenDate.isEqual(LocalDate.now()) || chosenMonth.getMonth().getValue() == LocalDate.now().getMonth().getValue()) {
            right.setClickable(false);
        }
        //здесь инициализируем репозитории
        SettingsRepository.initialize(this);
        settingsDao = SettingsRepository.get().settingsDao;
        SmokedCigarettesRepository.initialize(this);
        smokedCigarettesDao = SmokedCigarettesRepository.get().smokedCigarettesDao;

        lastCigarette = findViewById(R.id.last_cigarette_time);

        ListenableFuture<List<SmokedCigarettesEntity>> futureInDay = smokedCigarettesDao.getAll();
        Futures.addCallback(futureInDay, new FutureCallback<List<SmokedCigarettesEntity>>() {
            @RequiresApi(api = Build.VERSION_CODES.P)
            @Override
            public void onSuccess(List<SmokedCigarettesEntity> result) {
                //В БД УЖЕ ВСЕ УПОРЯДОЧЕНО
                if (!result.isEmpty()) {
                    firstDate = result.get(0).date;
                    updateClickableArrowsDay();
                    lastDate = result.get(result.size() - 1).date;
                    runOnUiThread(() -> {
                        lastCigarette.setText(secondsFormat(lastDate.getHour(), lastDate.getMinute(), lastDate.getSecond()));
                        if (chosenDate.isEqual(firstDate.toLocalDate())) { // не успевает сделать get
                            left.setClickable(false);
                        }
                    });
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d("SmokeDigression", "failed to get entity from db");
            }
        }, executors);

        setOnText = findViewById(R.id.text_set_stats);
        ListenableFuture<List<SettingsEntity>> future = settingsDao.getAll();
        Futures.addCallback(future, new FutureCallback<List<SettingsEntity>>() {
            @RequiresApi(api = Build.VERSION_CODES.P)
            @Override
            public void onSuccess(List<SettingsEntity> result) {
                if (!result.isEmpty()) {
                    settingsEntity = result.get(0);
                    setOn = settingsEntity.setOn;
                    if (setOn == 1) {
                        setOnText.setVisibility(View.INVISIBLE);
                        runOnUiThread(() -> updateObjectiveInterval(settingsEntity.cigarettes, settingsEntity.date, settingsEntity.sleep, settingsEntity.sleepW, settingsEntity.rate));
                    } else {
                        setOnText.setVisibility(View.VISIBLE);
                    }
                } else {
                    setOnText.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d("SmokeDigression", "failed to get entity from db");
            }
        }, executors);

        settingsButton = findViewById(R.id.settings_button);

        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        setOn = result.getData().getIntExtra("setOn", 0);
                        if (setOn == 1) {
                            setOnText.setVisibility(View.INVISIBLE);
                        } else {
                            setOnText.setVisibility(View.VISIBLE);
                        }
                    }
                }
        );
        settingsButton.setOnClickListener(view -> callSettingsActivity(activityResultLauncher));

        nonSmoke = findViewById(R.id.non_smoke);

        //TODO оставить это как пример инициализации
        //Создание lineChart
        lineChart = findViewById(R.id.common_chart);
        //Вынести все настройки в конструктор
        lineChart.getDescription().setEnabled(false);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);
        lineChart.setTouchEnabled(true);
        lineChart.setPinchZoom(true);

        //рабочее
        CommonChart.setDayLineChart(this, smokedCigarettesDao, executors, lineChart, LocalDate.now());
        //тест
        //CommonChart.setDayLineChart(this, smokedCigarettesDao, executors, lineChart, LocalDate.of(2024, 9, 14));
        //дефалт
        //CommonChart.setDefaultDayLineChart(this, lineChart);

        smoked = findViewById(R.id.smoked);
        if (setOn != null && setOn == 0) {
            smoked.setClickable(false);
        }

        smoked.setOnClickListener(view -> {
                    LocalDateTime now = ofInstant(now(), getDefault().toZoneId());
                    lastDate = now;
                    chosenDate = now.toLocalDate();
                    monthPicked = 0;
                    updateClickableArrowsDay();
                    updateLastSmokedCigaretteUI();
                    updateWidgets(this);
                    ListenableFuture<Void> insert = smokedCigarettesDao.insert(new SmokedCigarettesEntity(now, true));
                    Futures.addCallback(insert, new FutureCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            //UPDATE DAY UI
                            //TODO возможно надо сделать update arrows
                            //TODO сделать метод setOnDayArrows
                            CommonChart.setDayLineChart(getBaseContext(), smokedCigarettesDao, executors, lineChart, LocalDate.now());
                            if (setOn != null && setOn == 1) {
                                //UPDATE UI ДЛЯ OBJECTIVE INTERVAL
                                runOnUiThread(() -> updateObjectiveInterval(settingsEntity.cigarettes, settingsEntity.date, settingsEntity.sleep, settingsEntity.sleepW, settingsEntity.rate));
                            }
                        }

                        @Override
                        public void onFailure(Throwable t) {
                            Log.d("SmokeDigression", "failed to get entity from db");
                        }
                    }, executors);
                }
        );

        inDay = findViewById(R.id.cigarette_in_day);
        inDay.setOnClickListener(v -> {
            DatePickerDialog.OnDateSetListener onDateSetListener = (view, year, month, dayOfMonth) -> {
                //MONTH С НУЛЯ
                LocalDate localDate = LocalDate.of(year, month + 1, dayOfMonth);
                chosenDate = localDate;
                monthPicked = 0;
                CommonChart.setDayLineChart(this, smokedCigarettesDao, executors, lineChart, localDate);
                updateClickableArrowsDay();
            };
            LocalDate currentDate = LocalDate.now();
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    onDateSetListener,
                    currentDate.getYear(),
                    currentDate.getMonth().getValue(),
                    currentDate.getDayOfMonth());
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(Date.from(firstDate.atZone(ZoneId.systemDefault()).toInstant()));
            datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
            datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
            datePickerDialog.show();
        });

        inMonth = findViewById(R.id.cigarette_in_month);
        inMonth.setOnClickListener(v -> {

            LocalDate now = LocalDate.now();
            monthPicked = 1;

            List<String> months = ScrollViewDates.convertToScrollViewList(this);

            NumberPicker numberPicker = new NumberPicker(this);
            String[] displayedValues = months.toArray(new String[months.size()]);
            numberPicker.setMinValue(1);
            numberPicker.setMaxValue(months.size());
            numberPicker.setDisplayedValues(displayedValues);

            AlertDialog.Builder textDialog = new AlertDialog.Builder(this);
            textDialog.setTitle(getString(R.string.month));
            textDialog.setView(numberPicker);
            textDialog.setPositiveButton(getString(R.string.ok), (dialog, whichButton) -> {
                String chosenMonth = displayedValues[numberPicker.getValue() - 1];
                LocalDate chosenMonthDate = ScrollViewDates.parseLocalDateFromScrollView(this, chosenMonth);

                CommonChart.setMonthLineChart(this, smokedCigarettesDao, executors, lineChart, chosenMonthDate);
                updateClickableArrowsMonth(now);
            });
            textDialog.setNegativeButton(getString(R.string.cancel),
                    (dialog, which) -> {
                    });
            textDialog.show();
        });

        donation = findViewById(R.id.donate);
        donation.setOnClickListener(v -> {
            Uri uri = Uri.parse("https://www.donationalerts.com/r/smokedigression");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        });

        resetAll = findViewById(R.id.reset_all);
        resetAll.setOnClickListener(v -> {
            AlertDialog.Builder textDialog = new AlertDialog.Builder(this);
            textDialog.setTitle(getString(R.string.reset));
            textDialog.setMessage(getString(R.string.reset_msg));
            textDialog.setPositiveButton(getString(R.string.ok), (dialog, whichButton) -> {
                executors.execute(() -> smokedCigarettesDao.deleteAll());
                Toast.makeText(this, getString(R.string.erased), Toast.LENGTH_LONG).show();

                firstDate = LocalDateTime.now();
                lastDate = null;

                //TODO вот вроде и добавил
                updateLastSmokedCigaretteUI();
                updateCurrentlyNonSmokeUI();
                updateClickableArrowsDay();
                updateWidgets(this);

                //TODO ВОЗМОЖНО ХОТЕЛ ТУТ ЧТО-ТО ДОБАВИТЬ
                CommonChart.setDayLineChart(this, smokedCigarettesDao, executors, lineChart, LocalDate.now());

                inMonth.setOnClickListener(rv -> {

                    LocalDate now = LocalDate.now();
                    monthPicked = 1;

                    List<String> months = ScrollViewDates.convertToScrollViewList(this);

                    NumberPicker numberPicker = new NumberPicker(this);
                    String[] displayedValues = months.toArray(new String[months.size()]);
                    numberPicker.setMinValue(1);
                    numberPicker.setMaxValue(months.size());
                    numberPicker.setDisplayedValues(displayedValues);

                    AlertDialog.Builder textDialogReset = new AlertDialog.Builder(this);
                    textDialogReset.setTitle(getString(R.string.month));
                    textDialogReset.setView(numberPicker);
                    textDialogReset.setPositiveButton(getString(R.string.ok), (dialogReset, whichButtonReset) -> {
                        String chosenMonth = displayedValues[numberPicker.getValue() - 1];
                        LocalDate chosenMonthDate = ScrollViewDates.parseLocalDateFromScrollView(this, chosenMonth);

                        CommonChart.setMonthLineChart(this, smokedCigarettesDao, executors, lineChart, chosenMonthDate);
                        updateClickableArrowsMonth(now);
                    });
                    textDialogReset.setNegativeButton(getString(R.string.cancel),
                            (dialogReset, which) -> {
                            });
                    textDialogReset.show();
                });

                inDay.setOnClickListener(vDay -> {
                    DatePickerDialog.OnDateSetListener onDateSetListener = (view, year, month, dayOfMonth) -> {
                        //MONTH С НУЛЯ
                        LocalDate localDate = LocalDate.of(year, month + 1, dayOfMonth);
                        chosenDate = localDate;
                        monthPicked = 0;
                        CommonChart.setDayLineChart(this, smokedCigarettesDao, executors, lineChart, localDate);
                        updateClickableArrowsDay();
                    };
                    LocalDate currentDate = LocalDate.now();
                    DatePickerDialog datePickerDialog = new DatePickerDialog(
                            this,
                            onDateSetListener,
                            currentDate.getYear(),
                            currentDate.getMonth().getValue(),
                            currentDate.getDayOfMonth());
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(Date.from(firstDate.atZone(ZoneId.systemDefault()).toInstant()));
                    datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
                    datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
                    datePickerDialog.show();
                });

            });
            textDialog.setNegativeButton(getString(R.string.not_yet),
                    (dialog, which) -> {
                    });
            textDialog.show();
        });

        //TODO ПОЧЕМУ-ТО ОБНОВЛЯЕТСЯ ДВАЖДЫ
        /*
D/CommonChart: dayLineChartUpdated 2024-09-15T23:30:27.904577
D/CommonChart: dayLineChartUpdated 2024-09-15T23:30:45.125516
D/CommonChart: dayLineChartUpdated 2024-09-15T23:30:47.905299
D/CommonChart: dayLineChartUpdated 2024-09-15T23:31:05.151640
D/CommonChart: dayLineChartUpdated 2024-09-15T23:31:07.955191
D/CommonChart: dayLineChartUpdated 2024-09-15T23:31:25.148050
D/CommonChart: dayLineChartUpdated 2024-09-15T23:31:27.959274
         */
        Thread lineChartThread = new Thread() {
            @Override
            public void run() {
                try {
                    while (true) {
                        sleep(20000);
                        if (monthPicked == 0) {
                            updateDayLineChart();
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        lineChartThread.start();

        Thread uiTread = new Thread() {
            @RequiresApi(api = Build.VERSION_CODES.S)
            @Override
            public void run() {
                try {
                    while (true) {
                        updateCurrentlyNonSmokeUI();
                        sleep(100);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        uiTread.start();

    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    public void onResume() {
        super.onResume();
        updateSettingsEntity();
        if (setOn != null && setOn == 1) {
            updateSettingsEntity();
            updateLastSmokedCigarette();
            updateLastSmokedCigaretteUI();
            updateCurrentlyNonSmokeUI();
            updateClickableArrowsDay();
            CommonChart.setDayLineChart(this, smokedCigarettesDao, executors, lineChart, chosenDate);
        }
        local_currency = getString(R.string.currency);
    }

    private void updateSettingsEntity() {
        ListenableFuture<List<SettingsEntity>> future = settingsDao.getAll();
        Futures.addCallback(future, new FutureCallback<List<SettingsEntity>>() {
            @RequiresApi(api = Build.VERSION_CODES.P)
            @Override
            public void onSuccess(List<SettingsEntity> result) {
                if (!result.isEmpty()) {
                    settingsEntity = result.get(0);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d("SmokeDigression", "failed to get entity from db");
            }
        }, executors);
    }

    private void callSettingsActivity(ActivityResultLauncher<Intent> activityResultLauncher) {
        Intent intent = new Intent(MainActivity.this, Settings.class);
        activityResultLauncher.launch(intent);
    }

    public static String secondsFormat(int hours, int minutes, int seconds) {
        String secondsText = String.valueOf(seconds);
        String minutesText = String.valueOf(minutes);
        String hoursText = String.valueOf(hours);
        if (seconds < 10) {
            secondsText = "0" + seconds;
        }
        if (minutes < 10) {
            minutesText = "0" + minutes;
        }
        if (hours < 10) {
            hoursText = "0" + hours;
        }
        return hoursText + ":" + minutesText + ":" + secondsText;
    }

    //TODO СДЕЛАТЬ CACHE В ОТДЕЛЬНОМ ПОТОКЕ
    private void updateDayLineChart() {
        LocalDate now = LocalDate.now();
        if (chosenDate != null && chosenDate.isEqual(now)) {
            CommonChart.setDayLineChart(this, smokedCigarettesDao, executors, lineChart, now);
        }
    }

    private void updateLastSmokedCigaretteUI() {
        if (lastDate == null) {
            ListenableFuture<SmokedCigarettesEntity> last = smokedCigarettesDao.getLast();
            Futures.addCallback(last, new FutureCallback<SmokedCigarettesEntity>() {
                @Override
                public void onSuccess(SmokedCigarettesEntity result) {
                    if (result == null) {
                        runOnUiThread(() -> lastCigarette.setText("__:__"));
                    } else {
                        lastDate = result.date;
                        runOnUiThread(() -> lastCigarette.setText(secondsFormat(lastDate.getHour(), lastDate.getMinute(), lastDate.getSecond())));
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    Log.d("SmokeDigression", "failed to get entity from db");
                }
            }, executors);
        } else {
            lastCigarette.setText(secondsFormat(lastDate.getHour(), lastDate.getMinute(), lastDate.getSecond()));
        }
        Log.d("SmokeDigression", "smokedCigaretteUIUpdated");
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    private void updateCurrentlyNonSmokeUI() {
        if (lastDate == null) {
            runOnUiThread(() -> nonSmoke.setText("__:__"));
            return;
        }

        if (lastDate != null && !lastCigarette.getText().equals("__:__")) {

            Duration difference = Duration.between(lastDate, LocalDateTime.now());
            long seconds = difference.getSeconds();

            int hours = (int) (seconds / 3600);
            int minutes = (int) ((seconds % 3600) / 60);
            int secs = (int) (seconds % 60);
            runOnUiThread(() -> nonSmoke.setText(secondsFormat(hours, minutes, secs)));
        }
    }

    //привязано к графику
    public static void updateDaysEconomy(String startCigarettesNumber, String costOfPack, int currentCigarettes) {
        int startCigarettesNumberInt = Integer.parseInt(startCigarettesNumber);
        float costOfPackInt = Float.parseFloat(costOfPack.split(" ")[0]);

        float costOfOneCigarette = costOfPackInt / 20;
        float currentExpenses = currentCigarettes * costOfOneCigarette;
        float defaultExpenses = startCigarettesNumberInt * costOfOneCigarette;
        float result = defaultExpenses - currentExpenses;
        String text = String.format(Locale.ENGLISH, "%.2f", result) + " " + local_currency;

        dayEconomy.setText(text);
    }

    //привязано к графику
    public static void updateMonthEconomy(String startCigarettesNumber, String costOfPack, int currentCigarettes, LocalDate localDate) {
        int startCigarettesNumberInt = Integer.parseInt(startCigarettesNumber);
        float costOfPackInt = Float.parseFloat(costOfPack.split(" ")[0]);

        //YearMonth yearMonthObject = YearMonth.of(LocalDate.now().getYear(), LocalDate.now().getMonth());
        int daysInMonth = localDate.lengthOfMonth(); //yearMonthObject.lengthOfMonth();
        /*
        if (firstDate.getMonth() == LocalDate.now().getMonth()) {
            //+1, потому как отсчет с 0
            daysInMonth = yearMonthObject.lengthOfMonth() - firstDate.getDayOfMonth() + 1;
        }
        */
        float costOfOneCigarette = costOfPackInt / 20;
        float currentExpenses = currentCigarettes * costOfOneCigarette;
        float defaultExpenses = startCigarettesNumberInt * costOfOneCigarette;
        float result = defaultExpenses * daysInMonth - currentExpenses;
        String text = String.format(Locale.ENGLISH, "%.2f", result) + " " + local_currency;

        monthEconomy.setText(text);
    }

    //привязано к settings
    @SuppressLint("SetTextI18n")
    public static void updateObjectiveInterval(String cigarettesInDay, LocalDateTime updatedSettingsTime, String timeSleep, String timeSleepWeekend, String min) {
        if (setOn == null || setOn == 0) {
            //yourInterval.setTextColor(parseColor("#FF03DAC5"));
            yourInterval.setText("__:__");
            return;
        }

        int defaultIntervalMinutes = Integer.parseInt(timeSleep.split(" ")[0].split(":")[1]);
        int defaultIntervalHours = Integer.parseInt(timeSleep.split(" ")[0].split(":")[0]);
        int defaultIntervalWeekendMinutes = Integer.parseInt(timeSleepWeekend.split(" ")[0].split(":")[1]);
        int defaultIntervalWeekendHours = Integer.parseInt(timeSleepWeekend.split(" ")[0].split(":")[0]);

        int increment = Integer.parseInt(min.split(" ")[0]);
        long duration = DAYS.between(updatedSettingsTime.toLocalDate(), LocalDate.now());

        long defaultInterval = (60 * 24 - (defaultIntervalMinutes + defaultIntervalHours * 60L)) / Integer.parseInt(cigarettesInDay);
        long defaultIntervalWeekend = (60 * 24 - (defaultIntervalWeekendMinutes + defaultIntervalWeekendHours * 60L)) / Integer.parseInt(cigarettesInDay);
        Long result;

        if (LocalDate.now().getDayOfWeek().equals(DayOfWeek.SUNDAY)
                || LocalDate.now().getDayOfWeek().equals(DayOfWeek.SATURDAY)) {
            result = defaultIntervalWeekend + increment * (duration + 1);
        } else {
            result = defaultInterval + increment * (duration + 1);
        }
        int hours = (int) (result / 60);
        int minutes = (int) (result % 60);

        String hoursText;
        String minutesText;

        if (hours < 10) {
            hoursText = "0" + hours;
        } else {
            hoursText = "" + hours;
        }

        if (minutes < 10) {
            minutesText = "0" + minutes;
        } else {
            minutesText = "" + minutes;
        }

        //yourInterval.setTextColor(parseColor("#FF3700B3"));
        yourInterval.setText(hoursText + ":" + minutesText + ":" + "00");
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void updateWidgets(Context context) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
        views.setChronometer(R.id.timer, SystemClock.elapsedRealtime(), "%tH:%tM:%tS", true);

        ComponentName thisAppWidgetComponentName = new ComponentName("vk.kolosov.smokedigression", "vk.kolosov.smokedigression.widget.Widget");
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidgetComponentName);
        for (int appWidgetId : appWidgetIds) {
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    private void updateLastSmokedCigarette() {
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

    private void updateClickableArrowsDay() {
        if (monthPicked == 0) {
            if (chosenDate.isEqual(firstDate.toLocalDate())) {
                left.setClickable(false);
            }
            if (chosenDate.isBefore(LocalDate.now())) {
                right.setClickable(true);
            }
            if (chosenDate.isAfter(firstDate.toLocalDate())) {
                left.setClickable(true);
            }
        }
    }

    private void updateClickableArrowsMonth(LocalDate now) {
        if (chosenMonth.withDayOfMonth(1).isBefore(firstDate.toLocalDate().withDayOfMonth(2))) {
            left.setClickable(false);
        }
        if (chosenMonth.withDayOfMonth(2).isAfter(now.withDayOfMonth(1))) {
            right.setClickable(false);
        }
        if (chosenMonth.withDayOfMonth(2).isBefore(now.withDayOfMonth(1))) {
            right.setClickable(true);
        }
        if (chosenMonth.withDayOfMonth(1).isAfter(firstDate.toLocalDate().withDayOfMonth(2))) {
            left.setClickable(true);
        }
    }
}
