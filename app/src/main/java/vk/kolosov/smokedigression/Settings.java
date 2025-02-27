package vk.kolosov.smokedigression;

import static android.graphics.Color.parseColor;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import vk.kolosov.smokedigression.db.SettingsRepository;
import vk.kolosov.smokedigression.db.dao.SettingsDao;
import vk.kolosov.smokedigression.db.entity.SettingsEntity;
import vk.kolosov.smokedigression.widget.Widget;

public class Settings extends AppCompatActivity {
//TODO проверить 00:00 в интервалах и прочее (просто сделать toast на сет value)

    private TextView RESET;
    private int resetOn;
    private TextView cigarettes;
    private TextView costOfPack;
    private TextView workSleep;
    private TextView weekendSleep;
    private TextView resultRate;
    private Button DONE;
    private float workSleepActually; //ВОЗМОЖНО ПЕРЕДАВАТЬ В INTENT
    private float weekendSleepActually;
    public static ExecutorService executors = Executors.newCachedThreadPool();
    private final SettingsDao settingsDao = SettingsRepository.get().settingsDao;
    private static LocalDateTime updateDate;

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //TODO настроить полоску ввода текста
        cigarettes = findViewById(R.id.cigarette_in_day);
        cigarettes.setOnClickListener(v -> {
            EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_NORMAL);
            input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});

            AlertDialog.Builder textDialog = new AlertDialog.Builder(this);
            textDialog.setTitle(getString(R.string.enter));
            textDialog.setMessage(getString(R.string.cig_day_msg));
            textDialog.setView(input);
            textDialog.setPositiveButton(getString(R.string.ok), (dialog, whichButton) -> {
                if (input.getText().toString().isEmpty()) {
                    cigarettes.setText("____");
                } else {
                    cigarettes.setText(input.getText().toString());
                    updateDate = LocalDateTime.now();
                }
            });
            textDialog.setNegativeButton(getString(R.string.cancel),
                    (dialog, which) -> {
                        //cigarettes.setText("____");
                    });
            textDialog.show();
        });

        costOfPack = findViewById(R.id.cost_of_pack);
        costOfPack.setOnClickListener(v -> {
            EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});

            AlertDialog.Builder textDialog = new AlertDialog.Builder(this);
            textDialog.setTitle(getString(R.string.enter));
            textDialog.setMessage(getString(R.string.cost_pack_msg));
            textDialog.setView(input);
            textDialog.setPositiveButton(getString(R.string.ok), (dialog, whichButton) -> {
                if (input.getText().toString().isEmpty()) {
                    costOfPack.setText("____ " + getString(R.string.currency));
                } else {
                    costOfPack.setText(input.getText().toString() + " " + getString(R.string.currency));
                    updateDate = LocalDateTime.now();
                }
            });
            textDialog.setNegativeButton(getString(R.string.cancel),
                    (dialog, which) -> {
                        //costOfPack.setText("____ " + getString(R.string.currency));
                    });
            textDialog.show();
        });

        workSleep = findViewById(R.id.work_sleep);
        workSleep.setOnClickListener(v -> {
            TimePickerDialog.OnTimeSetListener myTimeListener = (view, hourOfDay, minute) -> {
                if (view.isShown()) {
                    workSleep.setText(hoursFormat(hourOfDay, minute, getString(R.string.h)));
                    updateDate = LocalDateTime.now();
                }
            };
            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    Settings.this,
                    android.R.style.Theme_Holo_Light_Dialog_NoActionBar,
                    myTimeListener,
                    0,
                    0,
                    true);
            timePickerDialog.setTitle(getString(R.string.select_time));
            timePickerDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            timePickerDialog.show();
        });

        weekendSleep = findViewById(R.id.weeknd_sleep);
        weekendSleep.setOnClickListener(v -> {
            TimePickerDialog.OnTimeSetListener myTimeListener = (view, hourOfDay, minute) -> {
                if (view.isShown()) {
                    weekendSleep.setText(hoursFormat(hourOfDay, minute, getString(R.string.h)));
                    updateDate = LocalDateTime.now();
                }
            };
            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    Settings.this,
                    android.R.style.Theme_Holo_Light_Dialog_NoActionBar,
                    myTimeListener,
                    0,
                    0,
                    true);
            timePickerDialog.setTitle(getString(R.string.select_time));
            timePickerDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            timePickerDialog.show();
        });

        resultRate = findViewById(R.id.temp);
        resultRate.setOnClickListener(v -> {
            EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_NORMAL);
            input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});

            AlertDialog.Builder textDialog = new AlertDialog.Builder(this);
            textDialog.setTitle(getString(R.string.enter));
            textDialog.setMessage(getString(R.string.your_obj_dig_rate));
            textDialog.setView(input);
            textDialog.setPositiveButton(getString(R.string.ok), (dialog, whichButton) -> {
                if (input.getText().toString().isEmpty()) {
                    resultRate.setText("____ " + getString(R.string.min));
                } else {
                    resultRate.setText(input.getText().toString() + " " + getString(R.string.min));
                    updateDate = LocalDateTime.now();
                }
            });
            textDialog.setNegativeButton(getString(R.string.cancel),
                    (dialog, which) -> {
                    });
            textDialog.show();
        });

        DONE = findViewById(R.id.button_done);
        ComponentName thisAppWidgetComponentName = new ComponentName("vk.kolosov.smokedigression", "vk.kolosov.smokedigression.widget.Widget");
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidgetComponentName);
        if (appWidgetIds.length > 0) {
            DONE.setClickable(false);
            DONE.setBackgroundColor(parseColor("#A09C9C"));
        }
        DONE.setOnClickListener(v -> {
            AlertDialog.Builder textDialog = new AlertDialog.Builder(this);
            textDialog.setTitle(getString(R.string.done));
            textDialog.setMessage(getString(R.string.done_msg));
            textDialog.setPositiveButton(getString(R.string.ok), (dialog, whichButton) -> {
                if (!cigarettes.getText().equals("____")
                        && !costOfPack.getText().equals("____ " + getString(R.string.currency))
                        && !workSleep.getText().equals("__:__ " + getString(R.string.h))
                        && !weekendSleep.getText().equals("__:__ " + getString(R.string.h))
                        && !resultRate.getText().equals("____ " + getString(R.string.min))) {
                    setOffClickable();
                    addWidget();
                    //Toast.makeText(this, "Widget added", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, getString(R.string.not_all_fields), Toast.LENGTH_LONG).show();
                }
            });
            textDialog.setNegativeButton(getString(R.string.not),
                    (dialog, which) -> {
                    });
            textDialog.show();
        });

        RESET = findViewById(R.id.reset);
        resetOn = 0;
        RESET.setOnClickListener(v -> {
            if (resetOn == 0) {
                setOnClickable();
                resetOn = 1;
            } else {
                setOffClickable();
                resetOn = 0;
            }
        });

        initTextViews(resultRate, cigarettes, costOfPack, workSleep, weekendSleep);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void addWidget() {
        AppWidgetManager mAppWidgetManager = getSystemService(AppWidgetManager.class);
        ComponentName myProvider = new ComponentName(this, Widget.class);
        Bundle bundle = new Bundle();
        if (mAppWidgetManager.isRequestPinAppWidgetSupported()) {
            Intent pinnedWidgetCallbackIntent = new Intent(this, Widget.class);
            PendingIntent successCallback = PendingIntent.getBroadcast(this, 0, pinnedWidgetCallbackIntent, PendingIntent.FLAG_IMMUTABLE);
            mAppWidgetManager.requestPinAppWidget(myProvider, bundle, successCallback);
        }
    }

    private void initTextViews(TextView resultRate, TextView cigarettes, TextView costOfPack, TextView workSleep, TextView weekendSleep) {
        ListenableFuture<List<SettingsEntity>> future = settingsDao.getAll();
        Futures.addCallback(future, new FutureCallback<List<SettingsEntity>>() {
            @SuppressLint("SetTextI18n")
            @RequiresApi(api = Build.VERSION_CODES.P)
            @Override
            public void onSuccess(List<SettingsEntity> result) {
                if (!result.isEmpty()) {
                    SettingsEntity settingsEntity = result.get(0);
                    resultRate.setText(settingsEntity.rate);
                    cigarettes.setText(settingsEntity.cigarettes);
                    costOfPack.setText(settingsEntity.cost);
                    workSleep.setText(settingsEntity.sleep);
                    weekendSleep.setText(settingsEntity.sleepW);
                    if (settingsEntity.rate.isEmpty()) {
                        resultRate.setText("5 " + getString(R.string.min));
                    } else {
                        resultRate.setText(settingsEntity.rate);
                    }
                    if (settingsEntity.date == null) {
                        updateDate = LocalDateTime.now();
                    } else {
                        updateDate = settingsEntity.date;
                    }
                    Integer setOn = 0;
                    if (!cigarettes.getText().equals("____")
                            && !costOfPack.getText().equals("____ " + getString(R.string.currency))
                            && !workSleep.getText().equals("__:__ " + getString(R.string.h))
                            && !weekendSleep.getText().equals("__:__ " + getString(R.string.h))
                            && !resultRate.getText().equals("____ " + getString(R.string.min))) {
                        setOn = 1;
                    }

                    if (setOn == 1) {
                        setOffClickable();
                    } else {
                        setOnClickable();
                    }
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d("SmokeDigression", "failed to get entity from db");
            }
        }, executors);
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    private void setOnClickable() {
        resultRate.setEnabled(true);
        cigarettes.setClickable(true);
        costOfPack.setClickable(true);
        workSleep.setClickable(true);
        weekendSleep.setClickable(true);
        DONE.setClickable(true);

        resultRate.setTextColor(parseColor("#FF03DAC5"));
        cigarettes.setTextColor(parseColor("#FF03DAC5"));
        costOfPack.setTextColor(parseColor("#FF03DAC5"));
        workSleep.setTextColor(parseColor("#FF03DAC5"));
        weekendSleep.setTextColor(parseColor("#FF03DAC5"));
        //DONE.setBackgroundColor(parseColor("#FF03DAC5"));
        RESET.setTextColor(Color.parseColor("#A09C9C"));
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    private void setOffClickable() {
        resultRate.setEnabled(false);
        cigarettes.setClickable(false);
        costOfPack.setClickable(false);
        workSleep.setClickable(false);
        weekendSleep.setClickable(false);
        ComponentName thisAppWidgetComponentName = new ComponentName("vk.kolosov.smokedigression", "vk.kolosov.smokedigression.widget.Widget");
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidgetComponentName);
        if (appWidgetIds.length > 0) {
            DONE.setClickable(false);
            DONE.setBackgroundColor(parseColor("#A09C9C"));
        }

        resultRate.setTextColor(parseColor("#A09C9C"));
        cigarettes.setTextColor(parseColor("#A09C9C"));
        costOfPack.setTextColor(parseColor("#A09C9C"));
        workSleep.setTextColor(parseColor("#A09C9C"));
        weekendSleep.setTextColor(parseColor("#A09C9C"));
        RESET.setTextColor(Color.parseColor("#008B8B")); //"#FF03DAC5"
    }

    public static String hoursFormat(int hours, int minutes, String h) {
        String minutesText = String.valueOf(minutes);
        String hoursText = String.valueOf(hours);
        if (minutes < 10) {
            minutesText = "0" + minutes;
        }
        if (hours < 10) {
            hoursText = "0" + hours;
        }
        return hoursText + ":" + minutesText + " " + h;
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    protected void onDestroy() {
        saveCurrentState();
        super.onDestroy();
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    protected void onStop() {
        saveCurrentState();
        super.onStop();
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    protected void onPause() {
        saveCurrentState();
        super.onPause();
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    public void onBackPressed() {
        saveCurrentState();
        super.onBackPressed();
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    private void saveCurrentState() {
        SettingsEntity settingsEntity = new SettingsEntity();
        settingsEntity.rate = resultRate.getText().toString();
        settingsEntity.cigarettes = cigarettes.getText().toString();
        settingsEntity.cost = costOfPack.getText().toString();
        settingsEntity.sleep = workSleep.getText().toString();
        settingsEntity.sleepW = weekendSleep.getText().toString();
        settingsEntity.date = updateDate;

        if (!cigarettes.getText().equals("____")
                && !costOfPack.getText().equals("____ " + getString(R.string.currency))
                && !workSleep.getText().equals("__:__ " + getString(R.string.h))
                && !weekendSleep.getText().equals("__:__ " + getString(R.string.h))
                && !resultRate.getText().equals("____ " + getString(R.string.min))) {
            settingsEntity.setOn = 1;
        } else {
            settingsEntity.setOn = 0;
        }
        settingsDao.insertAll(settingsEntity);
        MainActivity.settingsEntity = settingsEntity;
        MainActivity.updateObjectiveInterval(settingsEntity.cigarettes, settingsEntity.date, settingsEntity.sleep, settingsEntity.sleepW, settingsEntity.rate);
        saveIntentState(settingsEntity.setOn);
    }

    private void saveIntentState(int setOn) {
        Intent returnIntent = getIntent();
        returnIntent.putExtra("setOn", setOn);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }
}