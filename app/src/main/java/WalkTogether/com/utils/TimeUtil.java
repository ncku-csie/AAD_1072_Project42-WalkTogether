package WalkTogether.com.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class TimeUtil {
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
    private static Calendar mCalendar = Calendar.getInstance();
    /**
     * 返回当前的时间
     * @return  今天 09:48
     */
    public static String getCurTime(){
        SimpleDateFormat dFormat = new SimpleDateFormat("HH:mm");
        String time = "今天 "+dFormat.format(System.currentTimeMillis());
        return time;
    }



    /**
     * 获取是几号
     *
     * @return dd
     */

    /**
     * 获取当前的日期
     *
     * @return yyyy年MM月dd日
     */
    public static String getCurrentDate() {
        String currentDateStr = dateFormat.format(mCalendar.getTime());
        return currentDateStr;
    }
}
