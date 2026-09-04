package vk.kolosov.smokedigression.charts;

import static android.os.Looper.getMainLooper;
import static vk.kolosov.smokedigression.MainActivity.*;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.DefaultValueFormatter;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import vk.kolosov.smokedigression.R;
import vk.kolosov.smokedigression.db.dao.SmokedCigarettesDao;
import vk.kolosov.smokedigression.db.entity.SmokedCigarettesEntity;

public class CommonChart {

    public static void setDefaultDayLineChart(Context context, LineChart lineChart) {

        XAxis xAxis;
        {   // // X-Axis Style // //
            xAxis = lineChart.getXAxis();

            // vertical grid lines
            xAxis.enableGridDashedLine(10f, 10f, 0f);

            // axis range
            xAxis.setAxisMaximum(2400f);
            xAxis.setAxisMinimum(-0f);
        }
        YAxis yAxis;
        {   // // Y-Axis Style // //
            yAxis = lineChart.getAxisLeft();

            // disable dual axis (only use LEFT axis)
            lineChart.getAxisRight().setEnabled(false);

            // horizontal grid lines
            yAxis.enableGridDashedLine(10f, 10f, 0f);

            // axis range
            yAxis.setAxisMaximum(400f);
            yAxis.setAxisMinimum(-0f);
        }
        List<Entry> values = new ArrayList<>();
        //y = время
        //x = отметка сигареты
        Entry sampleEntry = new Entry(500, 140);
        sampleEntry.setIcon(ContextCompat.getDrawable(context, R.drawable.smoked));
        //х = время
        //TODO y = уровень в крови никотина
        values.add(new Entry(100, 30));
        values.add(new Entry(300, 25));
        values.add(new Entry(400, 20));
        values.add(sampleEntry); //100/80
        values.add(new Entry(600, 120));
        Entry sampleEntry2 = new Entry(700, 220);
        sampleEntry2.setIcon(ContextCompat.getDrawable(context, R.drawable.smoked));
        values.add(sampleEntry2);

        LineDataSet lineDataSet = new LineDataSet(values, "Sample");
        LineData lineData = new LineData(lineDataSet);

        lineChart.setData(lineData);
    }

    public static void setDayLineChart(Context context, SmokedCigarettesDao smokedCigarettesDao, ExecutorService executors, LineChart lineChart, LocalDate localDate) {

        XAxis xAxis;
        {   // // X-Axis Style // //
            xAxis = lineChart.getXAxis();

            // vertical grid lines
            xAxis.enableGridDashedLine(10f, 10f, 0f);

            // axis range
            xAxis.setAxisMaximum(2400f);
            xAxis.setAxisMinimum(-0f);
            xAxis.setValueFormatter(new DayAxisXFormatter());
        }
        YAxis yAxis;
        {   // // Y-Axis Style // //
            yAxis = lineChart.getAxisLeft();

            // disable dual axis (only use LEFT axis)
            lineChart.getAxisRight().setEnabled(false);

            // horizontal grid lines
            yAxis.enableGridDashedLine(10f, 10f, 0f);

            // axis range
            yAxis.setAxisMaximum(1000f);
            yAxis.setAxisMinimum(-0f);
            yAxis.setValueFormatter(new DayAxisYFormatter());
        }

        ListenableFuture<List<SmokedCigarettesEntity>> future = smokedCigarettesDao.getAll();
        Futures.addCallback(future, new FutureCallback<List<SmokedCigarettesEntity>>() {
            @RequiresApi(api = Build.VERSION_CODES.S)
            @Override
            public void onSuccess(List<SmokedCigarettesEntity> result) {
                // Сортировка полученного списка из БД для корректной хронологии
                List<SmokedCigarettesEntity> sortedResult = result.stream()
                        .sorted(Comparator.comparing(smokedCigarettesEntity -> smokedCigarettesEntity.date))
                        .collect(Collectors.toList());

                if (localDate != LocalDate.now() && sortedResult.isEmpty()) {
                    lineChart.clear();
                }
                List<Entry> entries = getDayGraphEntries(context, sortedResult, localDate);
                new Handler(Looper.getMainLooper()).post(() -> {
                            if (!entries.isEmpty()) {
                                entries.sort(Comparator.comparingDouble(Entry::getX));

                                LineDataSet lineDataSet = new LineDataSet(entries, localDate + " " + context.getString(R.string.day_graph));
                                lineDataSet.setLineWidth(2f);
                                lineDataSet.setDrawCircles(true);
                                lineDataSet.setCircleRadius(3f);

                                LineData lineData = new LineData(lineDataSet);
                                lineChart.clear();
                                lineChart.setData(lineData);
                                lineChart.notifyDataSetChanged();
                                lineChart.invalidate();
                            } else {
                                lineChart.clear();
                                lineChart.invalidate();
                            }
                        });
                Set<SmokedCigarettesEntity> currentDates = sortedResult.stream()
                        .filter(date -> date.date.toLocalDate().isEqual(localDate))
                        .collect(Collectors.toSet());
                currentCigarettesDay = currentDates.size();
                Set<SmokedCigarettesEntity> monthDates = sortedResult.stream()
                        .filter(date -> (date.date.getMonth().getValue() == localDate.getMonth().getValue())
                                && (date.date.getYear() == localDate.getYear()))
                        .collect(Collectors.toSet());
                currentCigarettesMonth = monthDates.size();

                new Handler(getMainLooper()).post(() -> {
                    if (setOn != null && setOn == 1) {
                        updateDaysEconomy(settingsEntity.cigarettes, settingsEntity.cost, currentCigarettesDay);
                        updateMonthEconomy(settingsEntity.cigarettes, settingsEntity.cost, currentCigarettesMonth, localDate);
                    }
                });

                Log.d("CommonChart", "dayLineChartUpdated " + LocalDateTime.now());
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d("SmokeDigression", "failed to get entity from db");
            }
        }, executors);
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    private static List<Entry> getDayGraphEntries(Context context, List<SmokedCigarettesEntity> result, LocalDate localDate) {
        Map<String, LocalDate> lastDateMap = new HashMap<>();
        List<Entry> graph = new ArrayList<>();
        List<Entry> entries = new ArrayList<>();
        Entry zeroEntry = getZeroEntry(result, localDate, lastDateMap);
        //TODO сделать javadoc

        LocalDate lastDate = lastDateMap.get("lastDate");
        if (lastDate != null) {
            // Исправление съезда при переходе года: абсолютный расчёт разницы дней
            long daysBetween = ChronoUnit.DAYS.between(lastDate, localDate);
            zeroEntry.setX((float) (zeroEntry.getX() - 2400 * daysBetween));
        }

        Entry firstEcho = zeroEntry;
        List<SmokedCigarettesEntity> dayEntities = result.stream()
                .filter(smokedCigarettesEntity -> smokedCigarettesEntity.date.toLocalDate().equals(localDate))
                //в бд упорядочено
                .sorted(Comparator.comparing(smokedCigarettesEntity -> smokedCigarettesEntity.date))
                .collect(Collectors.toList());

        currentCigarettesDay = dayEntities.size();
        if (dayEntities.isEmpty()) {
            List<Entry> intermediateEntries = getLastIntermediateEntries(zeroEntry, localDate);
            return getMiddleEntryZero(intermediateEntries);
        }

        for (int i = 0; i < dayEntities.size(); i++) {
            Entry second = getFollowEntry(context, firstEcho, dayEntities.get(i));
            entries.add(second);
            firstEcho = second;
        }

        for (int i = 0; i < entries.size(); i++) {
            if (i == 0) {
                //НАДО СОЗДАТЬ ENTRY ДЛЯ 00:00
                Entry first = entries.get(i);
                List<Entry> intermediateEntries = getZeroIntermediateEntries(zeroEntry, first);
                graph.add(zeroEntry);
                List<Entry> middleEntries = getMiddleEntryZero(intermediateEntries);
                graph.addAll(middleEntries);
                if (entries.size() == 1) {
                    graph.add(first);
                    intermediateEntries = getLastIntermediateEntries(first, localDate); //ВЗЯТЬ LAST ENTRY + ENTRY С Y=0
                    middleEntries = getMiddleEntry(intermediateEntries);
                    graph.addAll(middleEntries);
                }
                continue;
            }

            if (i == entries.size() - 1) {
                Entry before = entries.get(i - 1);
                Entry last = entries.get(i);
                List<Entry> intermediateEntries = getInnerIntermediateEntries(before, last);
                List<Entry> middleEntries = getMiddleEntry(intermediateEntries);
                graph.add(before);
                graph.addAll(middleEntries);
                graph.add(last);

                intermediateEntries = getLastIntermediateEntries(last, localDate);
                middleEntries = getMiddleEntry(intermediateEntries);
                graph.addAll(middleEntries);
                continue;
            }

            Entry first = entries.get(i - 1);
            Entry second = entries.get(i);
            List<Entry> intermediateEntries = getInnerIntermediateEntries(first, second);
            graph.add(first);
            List<Entry> middleEntries = getMiddleEntry(intermediateEntries);
            graph.addAll(middleEntries);
        }

        return graph;
    }

    //НАХОДИТСЯ ПОСЛЕДНЯЯ ТОЧКА ДО ТЕКУЩЕЙ ДАТЫ ПУТЕМ ПОСТРОЕНИЯ ГРАФИКА
    //первостепенно расчитывается Y, после выполнения метода корректируется X
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static Entry getZeroEntry(List<SmokedCigarettesEntity> result, LocalDate localDate, Map<String, LocalDate> lastDateMap) {
        List<SmokedCigarettesEntity> sortedEntities = result.stream()
                .filter(smokedCigarettesEntity -> smokedCigarettesEntity.date.toLocalDate().isBefore(localDate))
                //в бд упорядочено
                .sorted(Comparator.comparing(smokedCigarettesEntity -> smokedCigarettesEntity.date))
                .collect(Collectors.toList());
        //СДЕЛАЮ ИНКРЕМЕНТ Y НА 120
        Entry first = new Entry(0, 0);
        Entry second;
        LocalDateTime firstDate;
        LocalDate previousDate = null;

        for (int i = 0; i < sortedEntities.size(); i++) {
            LocalDateTime currentDate = sortedEntities.get(i).date;
            if (i == 0) {
                firstDate = currentDate;
                previousDate = firstDate.toLocalDate();
                int hours = firstDate.getHour();
                int minutes = firstDate.getMinute();
                int x1 = hours * 100 + minutes * 100 / 60;
                first = new Entry(x1, 120);
                continue;
            }
            int x1 = (int) first.getX();
            int y1 = (int) first.getY();

            LocalDate currentLocalDate = currentDate.toLocalDate();
            int hours = currentDate.getHour();
            int minutes = currentDate.getMinute();

            int x2 = hours * 100 + minutes * 100 / 60;

            // Расчет разницы дней через ChronoUnit.DAYS без сбоя при смене года
            long days = ChronoUnit.DAYS.between(previousDate, currentLocalDate);

            //Переход от дня к дню (промежуточные)
            if (days >= 1) { //уже если больше дня
                x1 = (int) (x1 - 2400 - 2400 * (days - 1)); //плюс расстояние по дню
            }
            // Экспоненциальный распад никотина с периодом полураспада 2 часа (200 X-units)
            int deltaX = x2 - x1;
            int y2 = (int) (y1 * Math.pow(0.5, deltaX / 200.0));

            // Порог отсечения: если осталось меньше 1% никотина, опускаем до нуля
            if (y2 < 1) {
                y2 = 0;
            }

            y2 = y2 + 120;

            previousDate = currentLocalDate;
            second = new Entry(x2, y2);
            first = second;

            //сохраняем последнюю дату
            if (i == sortedEntities.size() - 1) {
                lastDateMap.put("lastDate", previousDate); //тут уже смотрим последний день, когда посчитан y
            }
        }

        return first;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private static List<Entry> getMiddleEntryZero(List<Entry> entries) {
        List<Entry> middleEntries = new ArrayList<>();

        if (entries.isEmpty()) {
            return Collections.emptyList();
        }
        Entry x0 = entries.stream().filter(entry -> entry.getX() >= 0)
                //TODO берет ~00:00:05, a last настроен на 23:59:00 ровно
                .findFirst()
                .orElse(null); //могут быть проблемы
        if (x0 != null) {
            middleEntries.add(x0);
        }

        if (entries.size() == 1) {
            return middleEntries;
        }

        Entry y0 = entries.stream().filter(entry -> entry.getX() >= 0 && entry.getY() == 0)
                .findFirst()
                .orElse(null);
        if (y0 != null) {
            middleEntries.add(y0);
        }

        Entry last = entries.get(entries.size() - 1);
        if (y0 == null || last.getX() != y0.getX()) {
            middleEntries.add(last);
        }

        return middleEntries;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private static List<Entry> getMiddleEntry(List<Entry> entries) {
        if (entries.isEmpty()) {
            return Collections.emptyList();
        }
        Entry last = entries.get(entries.size() - 1);
        Entry y0 = entries.stream().filter(entry -> entry.getY() == 0)
                .findFirst()
                .orElse(null);
        List<Entry> middleEntries = new ArrayList<>();
        if (y0 != null) {
            middleEntries.add(y0);
        }
        middleEntries.add(last);
        return middleEntries;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private static Entry getFollowEntry(Context context, Entry first, SmokedCigarettesEntity smokedCigarettesEntity) {
        int x1 = (int) first.getX();
        int y1 = (int) first.getY();
        int hours = smokedCigarettesEntity.date.getHour();
        int minutes = smokedCigarettesEntity.date.getMinute();
        int x2 = hours * 100 + minutes * 100 / 60;
        if (x2 < x1) {
            x1 = x1 - 2400;
        }

        // Экспоненциальный распад никотина
        int deltaX = x2 - x1;
        int y2 = (int) (y1 * Math.pow(0.5, deltaX / 200.0));

        if (y2 < 1) {
            y2 = 0;
        }

        y2 = y2 + 120;

        Entry follow = new Entry(x2, y2);
        //follow.setIcon(ContextCompat.getDrawable(context, R.drawable.smoked));
        return follow;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private static List<Entry> getZeroIntermediateEntries(Entry zero, Entry second) {
        List<Entry> intermediateEntries = new ArrayList<>();

        int currentX = (int) second.getX();
        int numberOfEntries = (int) (currentX - (zero.getX() - 2400)) / 15;
        if (numberOfEntries <= 0) return intermediateEntries;

        float increment = (currentX - zero.getX()) / numberOfEntries;
        for (int i = 0; i < numberOfEntries; i++) {

            int x = (int) (zero.getX() + increment * (i + 1));
            float deltaX = increment * (i + 1);
            int y = (int) (zero.getY() * Math.pow(0.5, deltaX / 200.0));

            if (y < 1) {
                y = 0;
            }

            intermediateEntries.add(new Entry(x, y));
        }

        return intermediateEntries;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private static List<Entry> getInnerIntermediateEntries(Entry first, Entry second) {
        List<Entry> intermediateEntries = new ArrayList<>();

        int currentX = (int) second.getX();
        int numberOfEntries = (int) (currentX - first.getX()) / 15;
        if (numberOfEntries <= 0) return intermediateEntries;

        float increment = (currentX - first.getX()) / numberOfEntries;
        for (int i = 0; i < numberOfEntries; i++) {

            int x = (int) (first.getX() + increment * (i + 1));
            float deltaX = increment * (i + 1);
            int y = (int) (first.getY() * Math.pow(0.5, deltaX / 200.0));

            if (y < 1) {
                y = 0;
            }

            intermediateEntries.add(new Entry(x, y));
        }

        return intermediateEntries;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private static List<Entry> getLastIntermediateEntries(Entry smokedEntry, LocalDate localDate) {
        List<Entry> intermediateEntries = new ArrayList<>();
        LocalDateTime currentTime = LocalDateTime.now(); //00:00 все ломает
        int currentHour;
        int currentMinutes;

        if (localDate.isAfter(currentTime.toLocalDate())) {
            return Collections.emptyList();
        }
        if (currentTime.toLocalDate().isEqual(localDate)) {
            currentHour = LocalDateTime.now().getHour(); //при выборе текущей даты
            currentMinutes = LocalDateTime.now().getMinute(); //00:01 не ломает
        } else {
            //TODO getMiddleEntryZero
            currentHour = 23;
            currentMinutes = 59;
        }

        int currentX = currentHour * 100 + currentMinutes * 100 / 60;
        //TODO Может это будет фиксом
        if (currentX == 0) {
            currentX = 1;
        }
        int numberOfEntries = (int) (currentX - smokedEntry.getX()) / 5; //TODO ВЫНЕСТИ ДЕЛИМЕТЕР В СИГНАТУРУ И ИНКРЕМЕНТИРОВАТЬ ЕГО ВМЕСТЕ С ТАЙМЕРОМ
        if (numberOfEntries <= 0) return intermediateEntries;

        float increment = (currentX - smokedEntry.getX()) / numberOfEntries;
        for (int i = 0; i < numberOfEntries; i++) {
            //y	    x
            //20	0
            //15	100
            //10	200 - через 2 часа сокращение в 2 раза
            //Y = Y1 * (0.5 ^ (deltaX / 200)) - КОРРЕКТНЫЙ ЭКСПОНЕНЦИАЛЬНЫЙ ГРАФИК
            int x = (int) (smokedEntry.getX() + increment * (i + 1));
            float deltaX = increment * (i + 1);
            int y = (int) (smokedEntry.getY() * Math.pow(0.5, deltaX / 200.0));

            if (y < 1) {
                y = 0;
            }

            intermediateEntries.add(new Entry(x, y));
        }

        return intermediateEntries;
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    public static void setMonthLineChart(Context context, SmokedCigarettesDao smokedCigarettesDao, ExecutorService executors, LineChart lineChart, LocalDate month) {

        chosenMonth = month;
        YearMonth yearMonthObject = YearMonth.of(month.getYear(), month.getMonth());
        int daysInMonth = yearMonthObject.lengthOfMonth();

        XAxis xAxis;
        {   // // X-Axis Style // //
            xAxis = lineChart.getXAxis();

            // vertical grid lines
            xAxis.enableGridDashedLine(10f, 10f, 1f);

            // axis range
            xAxis.setAxisMaximum(daysInMonth);
            xAxis.setAxisMinimum(1f);
            xAxis.setValueFormatter(null);
        }
        YAxis yAxis;
        {   // // Y-Axis Style // //
            yAxis = lineChart.getAxisLeft();

            // disable dual axis (only use LEFT axis)
            lineChart.getAxisRight().setEnabled(false);

            // horizontal grid lines
            yAxis.enableGridDashedLine(10f, 10f, 0f);

            // axis range
            yAxis.setAxisMaximum(50f);
            yAxis.setAxisMinimum(-0f);
            yAxis.setValueFormatter(null);
        }

        String query = month.toString().substring(0, 7);
        ListenableFuture<List<SmokedCigarettesEntity>> future = smokedCigarettesDao.getByMonth(query);
        Futures.addCallback(future, new FutureCallback<List<SmokedCigarettesEntity>>() {
            @RequiresApi(api = Build.VERSION_CODES.S)
            @Override
            public void onSuccess(List<SmokedCigarettesEntity> result) {
                // Сортировка записей за месяц по дате
                List<SmokedCigarettesEntity> sortedResult = result.stream()
                        .sorted(Comparator.comparing(smokedCigarettesEntity -> smokedCigarettesEntity.date))
                        .collect(Collectors.toList());

                if (sortedResult.isEmpty()) {
                    LocalDate now = LocalDate.now();
                    int lengthOfMonth;
                    if (month.withDayOfMonth(2).isAfter(now.withDayOfMonth(1))) {
                        lengthOfMonth = now.getDayOfMonth();
                    } else {
                        lengthOfMonth = month.lengthOfMonth();
                    }
                    List<Entry> entries = getZeroMonthEntries(lengthOfMonth);
                    LineDataSet lineDataSet = new LineDataSet(entries, month.getMonth() + "-" + month.getYear() + " " + context.getString(R.string.month_graph));
                    LineData lineData = new LineData(lineDataSet);
                    lineData.setValueFormatter(new DefaultValueFormatter(1));
                    lineChart.clear();
                    lineChart.setData(lineData);
                } else {
                    //TODO Добавить zero по currentDay
                    List<Entry> entries = getMonthGraphEntries(sortedResult, month);
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (!entries.isEmpty()) {
                            entries.sort(Comparator.comparingDouble(Entry::getX));

                            LineDataSet lineDataSet = new LineDataSet(entries, month.getMonth() + "-" + month.getYear() + " " + context.getString(R.string.month_graph));
                            lineDataSet.setLineWidth(2f);

                            LineData lineData = new LineData(lineDataSet);
                            lineData.setValueFormatter(new DefaultValueFormatter(1));
                            lineChart.clear();
                            lineChart.setData(lineData);
                            lineChart.notifyDataSetChanged();
                            lineChart.invalidate();
                        } else {
                            lineChart.clear();
                            lineChart.invalidate();
                        }
                    });
                }
                currentCigarettesMonth = sortedResult.size();

                ((Activity) context).runOnUiThread(() -> {
                    if (setOn != null && setOn == 1) {
                        dayEconomy.setText("____.__ " + "RUB");
                        updateMonthEconomy(settingsEntity.cigarettes, settingsEntity.cost, currentCigarettesMonth, month);
                    }
                });
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d("SmokeDigression", "failed to get entity from db");
            }
        }, executors);
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    private static List<Entry> getMonthGraphEntries(List<SmokedCigarettesEntity> result, LocalDate month) {
        List<Entry> graph = new ArrayList<>();
        /*
        List<SmokedCigarettesEntity> monthEntities = result.stream()
                .filter(smokedCigarettesEntity -> smokedCigarettesEntity.date.getYear() == LocalDate.now().getYear()
                        && smokedCigarettesEntity.date.getMonth().getValue() == month)
                //в бд все упорядочено
                //.sorted(Comparator.comparing(smokedCigarettesEntity -> smokedCigarettesEntity.date))
                .collect(Collectors.toList());
         */

        currentCigarettesDay = result.size();

        Entry entry = new Entry(0, 0);
        int counter = 0;
        for (int i = 0; i < result.size(); i++) {
            SmokedCigarettesEntity entity = result.get(i);
            int currentDay = entity.date.getDayOfMonth();
            entry.setX(currentDay);
            entry.setY(entry.getY() + 1);
            if (i == 0) {
                insertFirstNullEntries(entity, graph, month);
            }
            if (i == result.size() - 1) {
                graph.add(entry);
                insertLastNullEntries(entity, graph, month);
                continue;
            }
            int nextDay = result.get(i + 1).date.getDayOfMonth();
            if (currentDay != nextDay) {
                //Добавление линии от первого месяца
                //    if (counter == 0) {
                //        graph.add(new Entry(entry.getX(), 0));
                //        counter++;
                //    }
                graph.add(entry);
                entry = new Entry(0, 0);
                insertBetweenNullEntries(currentDay, nextDay, graph);
            }
        }
        return graph;
    }

    private static void insertBetweenNullEntries(int currentDay, int nextDay, List<Entry> graph) {
        if (currentDay < nextDay) {
            for (int i = currentDay + 1; i < nextDay; i++) {
                graph.add(new Entry(i, 0));
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    private static void insertFirstNullEntries(SmokedCigarettesEntity firstEntity, List<Entry> graph, LocalDate month) {
        if (firstEntity.date.isAfter(firstDate)) {
            int nextDay = firstEntity.date.getDayOfMonth();
            insertBetweenNullEntries(0, nextDay, graph);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private static void insertLastNullEntries(SmokedCigarettesEntity lastEntity, List<Entry> graph, LocalDate month) {
        LocalDate now = LocalDate.now();
        if (lastEntity.date.getDayOfMonth() < month.lengthOfMonth()) {
            int nextDay;
            if (now.isBefore(month.withDayOfMonth(month.lengthOfMonth()))) {
                nextDay = now.getDayOfMonth();
            } else {
                nextDay = month.lengthOfMonth();
            }
            insertBetweenNullEntries(lastEntity.date.getDayOfMonth(), nextDay + 1, graph);
        } else {
            int nextDay = lastEntity.date.getDayOfMonth();
            insertBetweenNullEntries(lastEntity.date.getDayOfMonth(), nextDay + 1, graph);
        }
    }

    //IF RESULT IS EMPTY
    private static List<Entry> getZeroMonthEntries(int currentDay) {
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i <= currentDay; i++) {
            Entry entry = new Entry(i, 0);
            entries.add(entry);
        }
        return entries;
    }
}