package vk.kolosov.smokedigression.util;

import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import vk.kolosov.smokedigression.MainActivity;
import vk.kolosov.smokedigression.R;

public class ScrollViewDates {

    @RequiresApi(api = Build.VERSION_CODES.S)
    public static List<String> convertToScrollViewList(Context context) {
        List<String> months = new ArrayList<>();
        LocalDate firstMonth = MainActivity.firstDate.toLocalDate().withDayOfMonth(1);
        LocalDate currentMonth = LocalDate.now().withDayOfMonth(1);
        while (!firstMonth.isAfter(currentMonth)) {
            switch (firstMonth.getMonth()) {
                case JANUARY:
                    months.add(context.getString(R.string.jan) + " " + firstMonth.getYear());
                    break;
                case FEBRUARY:
                    months.add(context.getString(R.string.feb) + " " + firstMonth.getYear());
                    break;
                case MARCH:
                    months.add(context.getString(R.string.mar) + " " + firstMonth.getYear());
                    break;
                case APRIL:
                    months.add(context.getString(R.string.apr) + " " + firstMonth.getYear());
                    break;
                case MAY:
                    months.add(context.getString(R.string.may) + " " + firstMonth.getYear());
                    break;
                case JUNE:
                    months.add(context.getString(R.string.jun) + " " + firstMonth.getYear());
                    break;
                case JULY:
                    months.add(context.getString(R.string.jul) + " " + firstMonth.getYear());
                    break;
                case AUGUST:
                    months.add(context.getString(R.string.aug) + " " + firstMonth.getYear());
                    break;
                case SEPTEMBER:
                    months.add(context.getString(R.string.sep) + " " + firstMonth.getYear());
                    break;
                case OCTOBER:
                    months.add(context.getString(R.string.oct) + " " + firstMonth.getYear());
                    break;
                case NOVEMBER:
                    months.add(context.getString(R.string.nov) + " " + firstMonth.getYear());
                    break;
                case DECEMBER:
                    months.add(context.getString(R.string.dec) + " " + firstMonth.getYear());
                    break;
            }
            firstMonth = firstMonth.plusMonths(1);
        }
        Collections.reverse(months);
        return months;
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public static LocalDate parseLocalDateFromScrollView(Context context, String localDate) {
        int year = Integer.parseInt(localDate.split(" ")[1]);
        int month = 0;
        String monthName = localDate.split(" ")[0];

        if (monthName.equals(context.getString(R.string.jan))) {
            month = 1;
        } else if (monthName.equals(context.getString(R.string.feb))) {
            month = 2;
        } else if (monthName.equals(context.getString(R.string.mar))) {
            month = 3;
        } else if (monthName.equals(context.getString(R.string.apr))) {
            month = 4;
        } else if (monthName.equals(context.getString(R.string.may))) {
            month = 5;
        } else if (monthName.equals(context.getString(R.string.jun))) {
            month = 6;
        } else if (monthName.equals(context.getString(R.string.jul))) {
            month = 7;
        } else if (monthName.equals(context.getString(R.string.aug))) {
            month = 8;
        } else if (monthName.equals(context.getString(R.string.sep))) {
            month = 9;
        } else if (monthName.equals(context.getString(R.string.oct))) {
            month = 10;
        } else if (monthName.equals(context.getString(R.string.nov))) {
            month = 11;
        } else if (monthName.equals(context.getString(R.string.dec))) {
            month = 12;
        }

        return LocalDate.of(year, month, 1);
    }

}
