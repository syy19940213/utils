package hust.zeng.utils.staticm;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @title DateTimeUtil
 * @author zengzhihua
 */
public class DateTimeUtil {

    public final static String DATE_FORMAT_SECOND = "yyyy-MM-dd HH:mm:ss";
    public final static String DATE_FORMAT_MINUTE = "yyyy-MM-dd HH:mm";
    public final static String DATE_FORMAT_HOUR = "yyyy-MM-dd HH";
    public final static String DATE_FORMAT_DAY = "yyyy-MM-dd";
    private static SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_SECOND);

    /**
     * @title 将毫秒表示的时间转为"yyyy-MM-dd HH:mm:ss"
     * @param timeMillis
     * @return String
     */
    public static String formatSecond(long timeMillis) {
        return sdf.format(timeMillis);
    }

    /**
     * @title 将Date表示的时间转为"yyyy-MM-dd HH:mm:ss"
     * @param timeMillis
     * @return String
     */
    public static String formatDate(Date date) {
        return formatSecond(date.getTime());
    }

    /**
     * @title 将毫秒表示的时间转为"yyyy-MM-dd HH:mm"
     * @param timeMillis
     * @return String
     */
    public static String formatMinute(long timeMillis) {
        sdf.applyPattern(DATE_FORMAT_MINUTE);
        return sdf.format(timeMillis);
    }

    /**
     * @title将毫秒表示的时间转为"yyyy-MM-dd HH"
     * @param timeMillis
     * @return String
     */
    public static String formatHour(long timeMillis) {
        sdf.applyPattern(DATE_FORMAT_HOUR);
        return sdf.format(timeMillis);
    }

    /**
     * @title 将毫秒表示的时间转为"yyyy-MM-dd"
     * @param timeMillis
     * @return String
     */
    public static String formatDay(long timeMillis) {
        sdf.applyPattern(DATE_FORMAT_DAY);
        return sdf.format(timeMillis);
    }

    /**
     * @title "yyyy-MM-dd HH:mm:ss"表示的当前时间
     * @return String
     */
    public static String currentTime() {
        return formatSecond(System.currentTimeMillis());
    }

    /**
     * 将timeMillis表示为指定pattern的字符串
     * @param timeMillis
     * @param pattern
     * @return
     */
    public static String format(long timeMillis, String pattern) {
        sdf.applyPattern(pattern);
        return sdf.format(timeMillis);
    }

    /**
     * 将字符串解析成Date
     * @param text
     * @return
     * @throws ParseException
     */
    public static Date parse(String text) throws ParseException {
        return sdf.parse(text);
    }

    /**
     * 将 ?s 换算成 ?d?h?m?s
     * @param seconds
     * @return
     */
    public static String converseSeconds(long seconds) {
        String timeStr = seconds + "s";
        if (seconds > 60) {
            long second = seconds % 60;
            long min = seconds / 60;
            timeStr = min + "m" + second + "s";
            if (min > 60) {
                min = (seconds / 60) % 60;
                long hour = (seconds / 60) / 60;
                timeStr = hour + "h" + min + "m" + second + "s";
                if (hour > 24) {
                    hour = ((seconds / 60) / 60) % 24;
                    long day = (((seconds / 60) / 60) / 24);
                    timeStr = day + "d" + hour + "h" + min + "m" + second + "s";
                }
            }
        }
        return timeStr;
    }
}
