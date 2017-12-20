package common.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;

public class DateUtil {

	// 默认显示日期时间的格式
	public static final String TIME_FORMATE = "yyyy-MM-dd HH:mm:ss";
	public static final String DAY_FORMATE = "yyyy-MM-dd";
	public static final String DAY_FORMATE2 = "yyyy年MM月dd日";
	private static DateFormat timeFormat = new SimpleDateFormat(TIME_FORMATE);
	private static DateFormat dayFormat = new SimpleDateFormat(DAY_FORMATE);
	private static DateFormat dayFormat2 = new SimpleDateFormat(DAY_FORMATE2);

	private static Logger LOGGER = Logger.getLogger(DateUtil.class);

	/**
	 * 返回yyyy-MM-dd HH:mm:ss格式字符串
	 * 
	 * @param inputDat
	 * @return
	 */
	public static String getTimeStr(Date inputDate) {
		return timeFormat.format(inputDate);
	}
	
	public static String stringToDateTime(String stringTime) throws ParseException{
		Date date = timeFormat.parse(stringTime);
		return getTimeStr(date);
		
	}

	/**
	 * 返回yyyy-MM-dd格式字符串
	 * 
	 * @param inputDat
	 * @return
	 */
	public static String getDayStr(Date inputDate) {
		return dayFormat.format(inputDate);
	}
	
	/**
	 * 返回yyyy年MM月dd日格式字符串
	 * 
	 * @param inputDat
	 * @return
	 */
	public static String DateToString(Date inputDate) {
		return dayFormat2.format(inputDate);
	}

	/**
	 * 返回给定字符串所指的日期
	 * 
	 * @param day
	 * @return
	 */
	public static Date getDateByDay(String day) {
		Date date = new Date();
		try {
			date = (Date) dayFormat.parse(day);
		} catch (Exception e) {
		}
		return date;
	}
	
	/**
     * 返回给定字符串所指的日期
     * 
     * @param day
     * @return
     */
    public static Date getDateTimeByDay(String day) {
        Date date = new Date();
        try {
            date = (Date) timeFormat.parse(day);
        } catch (Exception e) {
        }
        return date;
    }

	/**
	 * 判断当前日期是星期几<br>
	 * 
	 * @param pTime
	 *            修要判断的时间<br>
	 * @return dayForWeek 判断结果<br>
	 * @throws Exception
	 */
	public static int getIndexWeek(Date inputDate) {
		Calendar c = Calendar.getInstance();
		int dayForWeek = 0;
		try {
			c.setTime(inputDate);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (c.get(Calendar.DAY_OF_WEEK) == 1) {
			dayForWeek = 7;
		} else {
			dayForWeek = c.get(Calendar.DAY_OF_WEEK) - 1;
		}
		return dayForWeek;
	}

	/**
	 * 判断当前日期是星期几<br>
	 * 
	 * @param pTime
	 *            修要判断的时间<br>
	 * @return dayForWeek 判断结果<br>
	 * @throws Exception
	 */
	public static String getChineseWeek(Date inputDate) {
		String dayNames[] = { "星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六" };
		Calendar c = Calendar.getInstance();
		int dayForWeek = 0;
		try {
			c.setTime(inputDate);
			dayForWeek = c.get(Calendar.DAY_OF_WEEK);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dayNames[dayForWeek - 1];
	}

	/**
	 * 根据输入的日期 和 提前天数 ， 获得 指定日期提前几天的日期对象
	 * 
	 * @param inputDate
	 *            日期对象
	 * @param after
	 *            是向后推不
	 * @param beforeDays
	 *            倒推的天数
	 * @return 指定日期倒推指定天数后的日期对象
	 * @throws ParseException
	 */
	public static Date getAppointDate(Date inputDate, boolean after, int beforeDays) {

		Calendar cal = Calendar.getInstance();
		cal.setTime(inputDate);
		int inputDayOfYear = cal.get(Calendar.DAY_OF_YEAR);
		int resultDayInt = 0;
		if (after) {
			resultDayInt = inputDayOfYear + beforeDays;
		} else {
			resultDayInt = inputDayOfYear - beforeDays;
		}
		cal.set(Calendar.DAY_OF_YEAR, resultDayInt);

		return cal.getTime();
	}

	/**
	 * 得到几天前的时间
	 * 
	 * @param d
	 * @param day
	 * @return
	 */
	public static Date getDateBefore(Date d, int day) {
		Calendar now = Calendar.getInstance();
		now.setTime(d);
		now.set(Calendar.DATE, now.get(Calendar.DATE) - day);
		return now.getTime();
	}

	/**
	 * 得到几天前的时间
	 * 
	 * @param date 时间,默认是当前系统时间
	 * @param day 天数
	 * @param format 时间格式,默认是"yyyy-MM-dd"
	 * @return
	 */
	public static String getDateBefore(String date, int day, String format) {
		SimpleDateFormat dft = null;
		if (null != format && !"".equals(format)) {
			dft = new SimpleDateFormat(format);
		} else {
			dft = (SimpleDateFormat) dayFormat;
		}
		Date beginDate = null;
		try {
			if (null != date && !"".equals(date)) {
				beginDate = dft.parse(date);
			} else {
				beginDate = new Date();
			}
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Calendar d = Calendar.getInstance();
		d.setTime(beginDate);
		d.set(Calendar.DATE, d.get(Calendar.DATE) - day);
		return dft.format(d.getTime());
	}

	/**
	 * 得到几天后的时间
	 * 
	 * @param d
	 * @param day
	 * @return
	 */
	public static Date getDateAfter(Date d, int day) {
		Calendar now = Calendar.getInstance();
		now.setTime(d);
		now.set(Calendar.DATE, now.get(Calendar.DATE) + day);
		return now.getTime();
	}

	/**
	 * 两个日期之间的间隔
	 * 
	 * @param start
	 * @param end
	 * @return interval
	 */
	public static int getDateInterval(Date start, Date end) {

		Calendar startCal = Calendar.getInstance();
		startCal.setTime(start);

		Calendar endCal = Calendar.getInstance();
		endCal.setTime(end);

		int startInt = startCal.get(Calendar.DAY_OF_YEAR);
		int endInt = endCal.get(Calendar.DAY_OF_YEAR);

		int interval = startInt > endInt ? startInt - endInt : endInt - startInt;
		return interval;
	}

	/**
	 * 计算2个日期之间的工作日
	 * 
	 * @param start
	 * @param end
	 * @return
	 */
	public static int getWorkingDay(Date start, Date end) {
		int result = 0;
		int interval = getDateInterval(start, end);
		int startIndex = getIndexWeek(start);
		int endIndex = getIndexWeek(end);
		int beforeCount = 0;
		int beforeWorking = 0;
		if (startIndex == 6) {// 周六
			beforeCount = 2;
		} else if (startIndex == 7) {// 周日
			beforeCount = 1;
		} else {// 工作日
			beforeCount = 7 - startIndex + 1;
			beforeWorking = 5 - startIndex;
		}
		result = ((interval - beforeCount) / 7) * 5 + beforeWorking + endIndex;
		return result;
	}

	public static void main(String[] args) {
		Date startDate = DateUtil.getDateByDay("2012-12-07");
		Date endDate = DateUtil.getDateByDay("2012-12-17");
		LOGGER.info("工作日:" + DateUtil.getWorkingDay(startDate, endDate));

	}

}
