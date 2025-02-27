package vk.kolosov.smokedigression.charts;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

public class DayAxisYFormatter implements IAxisValueFormatter {
    @Override
    public String getFormattedValue(float value, AxisBase axis) {
         switch ((int) value) {
            case(0):
                return "0";
            case (100):
                return "1mg";
            case (200):
                return "2mg";
            case (300):
                return "3mg";
            case (400):
                return "4mg";
            case (500):
                return "5mg";
            case (600):
                return "6mg";
            case (700):
                return "7mg";
            case (800):
                return "8mg";
            case (900):
                return "9mg";
            case (1000):
                return "10mg";
             default:
                 return "";
        }
    }
}
