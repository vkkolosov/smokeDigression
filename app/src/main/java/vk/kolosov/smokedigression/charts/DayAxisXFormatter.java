package vk.kolosov.smokedigression.charts;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

public class DayAxisXFormatter implements IAxisValueFormatter {
    @Override
    public String getFormattedValue(float value, AxisBase axis) {
         switch ((int) value) {
            case(0):
            case (2400):
                return "00:00h";
            case (100):
                return "01:00h";
            case (200):
                return "02:00h";
            case (300):
                return "03:00h";
            case (400):
                return "04:00h";
            case (500):
                return "05:00h";
            case (600):
                return "06:00h";
            case (700):
                return "07:00h";
            case (800):
                return "08:00h";
            case (900):
                return "09:00h";
            case (1000):
                return "10:00h";
            case (1100):
                return "11:00h";
            case (1200):
                return "12:00h";
            case (1300):
                return "13:00h";
            case (1400):
                return "14:00h";
            case (1500):
                return "15:00h";
            case (1600):
                return "16:00h";
            case (1700):
                return "17:00h";
            case (1800):
                return "18:00h";
            case (1900):
                return "19:00h";
            case (2000):
                return "20:00h";
            case (2100):
                return "21:00h";
            case (2200):
                return "22:00h";
            case (2300):
                return "23:00h";
             default:
                 return "";
        }
    }
}
