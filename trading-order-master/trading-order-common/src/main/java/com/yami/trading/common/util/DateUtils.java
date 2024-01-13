package com.yami.trading.common.util;

import com.aliyuncs.utils.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 *
 */
public abstract class DateUtils {

	public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";

	public static final String DEFAULT_TIME_ZONE = "GMT+5:30";

	public static final String DEFAULT_TIME_ZONES = "GMT+8";

	public static final String NORMAL_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

	public static Date toDate(String string) {
		return toDate(string, DEFAULT_DATE_FORMAT);
	}

	public static Date toDates(String string, String pattern) {
		return toDate(string, pattern, TimeZone.getTimeZone(DEFAULT_TIME_ZONES));
	}

	public static Date toDate(String string, String pattern) {
		return toDate(string, pattern, TimeZone.getTimeZone(DEFAULT_TIME_ZONE));
	}

	public static Date toDate(String string, String pattern, TimeZone timeZone) {
		try {
			SimpleDateFormat sdf = (SimpleDateFormat) createDateFormat(pattern, timeZone);
			return sdf.parse(string);
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}

	public static String format(Date date, String pattern, TimeZone timeZone) {
		DateFormat df = createDateFormat(pattern, timeZone);
		return df.format(date);
	}


	/*public static String formats(Date date, String pattern) {
		if (date == null) {
			return null;
		}
		DateFormat df = createDateFormats(pattern);
		return df.format(date);
	}

	public static DateFormat createDateFormats(String pattern) {
		return createDateFormat(pattern, TimeZone.getTimeZone(DEFAULT_TIME_ZONES));
	}*/
	/**
	 * Description: 根据指定的pattern格式化date
	
	 */
	public static String format(Date date, String pattern) {
		if (date == null) {
			return null;
		}
		DateFormat df = createDateFormat(pattern);
		return df.format(date);
	}

	public static DateFormat createDateFormat(String pattern) {
		return createDateFormat(pattern, TimeZone.getTimeZone(DEFAULT_TIME_ZONE));
	}

	public static DateFormat createDateFormat(String pattern, TimeZone timeZone) {
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		TimeZone gmt = timeZone;
		sdf.setTimeZone(gmt);
		sdf.setLenient(true);
		return sdf;
	}

	public static int getYear(Date date) {
		Calendar calendar = (Calendar) Calendar.getInstance().clone();
		calendar.setTime(date);
		return calendar.get(Calendar.YEAR);
	}

	public static String getShortYear(Date date) {
		String year = getYear(date) + "";
		int length = year.length();
		return year.substring(length - 2, length);
	}

	public static int getMonth(Date date) {
		Calendar calendar = (Calendar) Calendar.getInstance().clone();
		calendar.setTime(date);
		return calendar.get(Calendar.MONTH) + 1;
	}

	public static int getDay(Date date) {
		Calendar calendar = (Calendar) Calendar.getInstance().clone();
		calendar.setTime(date);
		return calendar.get(Calendar.DAY_OF_MONTH);
	}

	public static int getHour(Date date) {
		Calendar calendar = (Calendar) Calendar.getInstance().clone();
		calendar.setTime(date);
		return calendar.get(Calendar.HOUR_OF_DAY);
	}

	public static int getMinute(Date date) {
		Calendar calendar = (Calendar) Calendar.getInstance().clone();
		calendar.setTime(date);
		return calendar.get(Calendar.MINUTE);
	}

	public static int getSecond(Date date) {
		Calendar calendar = (Calendar) Calendar.getInstance().clone();
		calendar.setTime(date);
		return calendar.get(Calendar.SECOND);
	}

	public static Date addMilliSecond(Date oldDate, int addMilliSecond) {
		Calendar calendar = (Calendar) Calendar.getInstance().clone();
		calendar.setTime(oldDate);
		calendar.add(Calendar.MILLISECOND, addMilliSecond);
		return calendar.getTime();
	}

	public static Date addSecond(Date oldDate, int addSecond) {
		Calendar calendar = (Calendar) Calendar.getInstance().clone();
		calendar.setTime(oldDate);
		calendar.add(Calendar.SECOND, addSecond);
		return calendar.getTime();
	}

	public static Date addMinute(Date oldDate, int addMinutes) {
		Calendar calendar = (Calendar) Calendar.getInstance().clone();
		calendar.setTime(oldDate);
		calendar.add(Calendar.MINUTE, addMinutes);
		return calendar.getTime();
	}

	public static Date addHour(Date oldDate, int addHours) {
		Calendar calendar = (Calendar) Calendar.getInstance().clone();
		calendar.setTime(oldDate);
		calendar.add(Calendar.HOUR, addHours);
		return calendar.getTime();
	}

	public static Date addDay(Date oldDate, int addDays) {
		Calendar calendar = (Calendar) Calendar.getInstance().clone();
		calendar.setTime(oldDate);
		calendar.add(Calendar.DATE, addDays);
		return calendar.getTime();
	}

	public static Date addMonth(Date oldDate, int addMonths) {
		Calendar calendar = (Calendar) Calendar.getInstance().clone();
		calendar.setTime(oldDate);
		calendar.add(Calendar.MONTH, addMonths);
		return calendar.getTime();
	}

	public static Date addYear(Date oldDate, int addYears) {
		Calendar calendar = (Calendar) Calendar.getInstance().clone();
		calendar.setTime(oldDate);
		calendar.add(Calendar.YEAR, addYears);
		return calendar.getTime();
	}

	public static long calcTimeBetween(String unitType, Date startDate, Long endDate) {
		Assert.hasText(unitType);
		Assert.notNull(startDate);
		Assert.notNull(endDate);
		long between = endDate - startDate.getTime();
		if (unitType.equals("ms")) {
			return between;
		} else if (unitType.equals("s")) {
			return between / 1000;// 返回秒
		} else if (unitType.equals("m")) {
			return between / 60000;// 返回分钟
		} else if (unitType.equals("h")) {
			return between / 3600000;// 返回小时
		} else if (unitType.equals("d")) {
			return between / 86400000;// 返回天数
		} else {
			throw new IllegalArgumentException("the unitType is unknown");
		}
	}

	public static long calcTimeBetween(String unitType, Date startDate, Date endDate) {
		Assert.hasText(unitType);
		Assert.notNull(startDate);
		Assert.notNull(endDate);
		long between = endDate.getTime() - startDate.getTime();
		if (unitType.equals("ms")) {
			return between;
		} else if (unitType.equals("s")) {
			return between / 1000;// 返回秒
		} else if (unitType.equals("m")) {
			return between / 60000;// 返回分钟
		} else if (unitType.equals("h")) {
			return between / 3600000;// 返回小时
		} else if (unitType.equals("d")) {
			return between / 86400000;// 返回天数
		} else {
			throw new IllegalArgumentException("the unitType is unknown");
		}
	}

	public static long calcTimeBetweenInMillis(Date startDate, Date endDate) {
		return calcTimeBetween("ms", startDate, endDate);
	}

	public static long calcTimeBetweenInSecond(Date startDate, Date endDate) {
		return calcTimeBetween("s", startDate, endDate);
	}

	public static long calcTimeBetweenInMinute(Date startDate, Date endDate) {
		return calcTimeBetween("m", startDate, endDate);
	}

	public static long calcTimeBetweenInHour(Date startDate, Date endDate) {
		return calcTimeBetween("h", startDate, endDate);
	}

	public static long calcTimeBetweenInDay(Date startDate, Date endDate) {
		return calcTimeBetween("d", startDate, endDate);
	}

	public static Date roundYear(Date date) {
		return org.apache.commons.lang3.time.DateUtils.round(date, Calendar.YEAR);
	}

	public static Date roundMonth(Date date) {
		return org.apache.commons.lang3.time.DateUtils.round(date, Calendar.MONTH);
	}

	public static Date roundDay(Date date) {
		return org.apache.commons.lang3.time.DateUtils.round(date, Calendar.DATE);
	}

	public static Date roundHour(Date date) {
		return org.apache.commons.lang3.time.DateUtils.round(date, Calendar.HOUR);
	}

	public static Date roundMinute(Date date) {
		return org.apache.commons.lang3.time.DateUtils.round(date, Calendar.MINUTE);
	}

	public static Date roundSecond(Date date) {
		return org.apache.commons.lang3.time.DateUtils.round(date, Calendar.SECOND);
	}

	public static Date truncateYear(Date date) {
		return org.apache.commons.lang3.time.DateUtils.truncate(date, Calendar.YEAR);
	}

	public static Date truncateMonth(Date date) {
		return org.apache.commons.lang3.time.DateUtils.truncate(date, Calendar.MONTH);
	}

	public static Date truncateDay(Date date) {
		return org.apache.commons.lang3.time.DateUtils.truncate(date, Calendar.DATE);
	}

	public static Date truncateHour(Date date) {
		return org.apache.commons.lang3.time.DateUtils.truncate(date, Calendar.HOUR);

	}

	public static Date truncateMinute(Date date) {
		return org.apache.commons.lang3.time.DateUtils.truncate(date, Calendar.MINUTE);
	}

	public static Date truncateSecond(Date date) {
		return org.apache.commons.lang3.time.DateUtils.truncate(date, Calendar.SECOND);
	}

	public static Date setHour(Date oldDate, int newHour) {
		Calendar calendar = (Calendar) Calendar.getInstance().clone();
		calendar.setTime(oldDate);
		calendar.set(Calendar.HOUR, newHour);
		return calendar.getTime();
	}

	public static Date setMinute(Date oldDate, int newMinute) {
		Calendar calendar = (Calendar) Calendar.getInstance().clone();
		calendar.setTime(oldDate);
		calendar.set(Calendar.MINUTE, newMinute);
		return calendar.getTime();
	}

	public static Date setSecond(Date oldDate, int newSecond) {
		Calendar calendar = (Calendar) Calendar.getInstance().clone();
		calendar.setTime(oldDate);
		calendar.set(Calendar.SECOND, newSecond);
		return calendar.getTime();
	}

	/**
	 *
	 * @param dt Date
	 * @return boolean
	 */
	@SuppressWarnings("deprecation")
	public static boolean isRYear(Date dt) {
		return (isRYear(1900 + dt.getYear()));
	}

	/**
	 *
	 * @param y int
	 * @return boolean
	 */
	public static boolean isRYear(int y) {
		return (y % 400 == 0 || (y % 4 == 0 && y % 100 != 0));
	}

	/**
	 * 获取一个日期的时间字符串
	 *
	 * @param dt Date
	 * @return String
	 */
	public static String getTimeStr(Date dt) {
		return new SimpleDateFormat("HH:mm:ss").format(dt);
	}

	/**
	 * 获取一个日期值的日期字符串
	 *
	 * @param dt Date
	 * @return String
	 */
	public static String getDateStr(Date dt) {
		return new SimpleDateFormat("yyyy-MM-dd").format(dt);
	}

	/**
	 * 获取一个日期值的带时间日期字符串
	 *
	 * @param dt Date
	 * @return String
	 */
	public static String getLongDate(Date dt) {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(dt);
	}

	/**
	 *
	 * @param dt Date
	 * @return String
	 */
	public static String toString(Date dt) {
		return format(dt, "yyyy-MM-dd HH:mm:ss");
	}

	/**
	 *
	 * @param date Date
	 * @return Timestamp added by jiayc
	 */
	public static java.sql.Timestamp dateToTimeStamp(Date date) {
		if (date == null) {
			return null;
		} else {
			return new java.sql.Timestamp(date.getTime());
		}
	}

	public static final String DFS_MMdd = "MMdd";

	public static final String DFS_yyyyMMdd = "yyyyMMdd";

	public static final String DFS_yyMMdd = "yyMMdd";

	public static final String DF_MMdd = "MM-dd";

	public static final String DF_HHmm = "HH:mm";

	public static final String DF_MMddHH = "MM-dd HH";

	public static final String DF_yyyyMM = "yyyy-MM";

	public static final String DF_yyyyMMdd = "yyyy-MM-dd";

	public static final String DF_yyyyMMddHH = "yyyy-MM-dd HH";

	public static final String DF_yyyyMMddHHmm = "yyyy-MM-dd HH:mm";

	public static final String DF_yyyyMMddHHmmss = "yyyy-MM-dd HH:mm:ss";

	public static final String DF_yyyyMMddHHmmssS = "yyyy-MM-dd HH:mm:ss.SSS";
	public static final String DF_MMddyyyy = "MM/dd/yyyy";

	private static String[] availableDF = { DFS_MMdd, DFS_yyyyMMdd, DFS_yyMMdd, DF_MMdd, DF_MMddHH, DF_HHmm,
			DF_yyyyMMdd, DF_yyyyMM, DF_yyyyMMddHH, DF_yyyyMMddHHmm, DF_yyyyMMddHHmmss, DF_yyyyMMddHHmmssS,
			DF_MMddyyyy };

	static Log logger = LogFactory.getLog(DateUtils.class.getName());

	protected DateUtils() {
	}

	/**
	 * �����String��Date��ת����Ŀǰ����ܹ涨��ʽ��DF_xxxx��DFS_xxxx�ƶ��ĸ�ʽ��
	 * 
	 * @param strDate ת��ǰ�����ڣ��ַ��ʾ
	 * @return Date ת���������
	 * @throws ParseException
	 */
	public static Date strToDate(String strDate) throws ParseException {
		if (StringUtils.isEmpty(strDate)) {
			return null;
		}
		int _L = strDate.trim().length();
		String format = "";
		switch (_L) {
		case 4:
			format = DFS_MMdd;
			break;
		case 5:
			if (strDate.indexOf("-") != -1) {
				format = DF_MMdd;
			} else if (strDate.indexOf(":") != -1) {
				format = DF_HHmm;
			}
			break;
		case 6:
			format = DFS_yyMMdd;
			break;
		case 7:
			format = DF_yyyyMM;
			break;
		case 8:
			if (strDate.indexOf("-") != -1) {
				format = DF_MMddHH;
			} else {
				format = DFS_yyyyMMdd;
			}
			break;
		case 10:
			format = DF_yyyyMMdd;
			break;
		case 13:
			format = DF_yyyyMMddHH;
			break;
		case 16:
			format = DF_yyyyMMddHHmm;
			break;
		case 19:
			format = DF_yyyyMMddHHmmss;
			break;
		case 21:
			format = DF_yyyyMMddHHmmssS;
			break;
		default:
			throw new ParseException("", 0);
		}
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		sdf.setLenient(false);
		return sdf.parse(strDate);
	}

	/**
	 * �����Date��String��ת����formatͨ��DateUtil.DF_xxx��4ָ��
	 * 
	 * @param date ת��ǰ������
	 * @return String ת���������
	 */
	public static String dateToStr(Date date, String format) {
		if (date == null) {
			return "";
		}
		for (int i = 0; i < availableDF.length; i++) {
			if (availableDF[i].equals(format)) {
				SimpleDateFormat sdf = new SimpleDateFormat(format);
				sdf.setLenient(false);
				return sdf.format(date);
			}
		}
		return "";
	}

	/**
	 * ��ȡdate�ĵ�����ʼʱ��
	 * 
	 * @param date
	 * @return Date
	 */
	public static Date getDayStart(Date date) {
		if (date == null) {
			return null;
		}
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}

	/**
	 * ��ȡdate�ĵ������ʱ��
	 * 
	 * @param date
	 * 
	 * @return Date
	 */
	public static Date getDayEnd(Date date) {
		if (date == null) {
			return null;
		}
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);
		calendar.set(Calendar.MILLISECOND, calendar.getActualMaximum(Calendar.MILLISECOND) - 1);
		return calendar.getTime();
	}

	/**
	 * ��ȡdate�ĵ�����ʼʱ��
	 * 
	 * @param date
	 * @return Date
	 */
	public static Date getMonthStart(Date date) {
		if (date == null) {
			return null;
		}
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}

	/**
	 * ��ȡdate�ĵ��µ����ʱ��
	 * 
	 * @param date
	 * @return Date
	 */
	public static Date getMonthEnd(Date date) {
		if (date == null) {
			return null;
		}
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);
		calendar.set(Calendar.MILLISECOND, calendar.getActualMaximum(Calendar.MILLISECOND) - 1);
		return calendar.getTime();
	}

	/**
	 * ��ȡdate�����Сʱ����ʼʱ��
	 * 
	 * @param date
	 * @return Date
	 */
	public static Date getHourStart(Date date) {
		if (date == null) {
			return null;
		}
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}

	/**
	 * ��ȡDate���ϻ��ȥ���պ��Date
	 * 
	 * @param date Ҫ�����������
	 * @param day  Ҫ�ӵ�����,����Ϊ����,Ϊ������ȡ�������
	 * @return Date ����������
	 */
	public static Date addDate(Date date, int day) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.DAY_OF_YEAR, day);
		return calendar.getTime();
	}

	/**
	 * ��long�ͳ��ȵ�ʱ��ת��Ϊ"��ʱ�w֡���"�ĸ�ʽ
	 */
	public static String formatTime(long time) {
		long hour = time / 3600;
		long tempTime = time % 3600;
		long min = tempTime / 60;
		long sec = tempTime % 60;
		return hour + "ʱ" + min + "��" + sec + "��";
	}

	public static Date strToDate(String strDate, String formatStr, String zoneStr) {
		Locale locale = new Locale(zoneStr);
		SimpleDateFormat formatter = new SimpleDateFormat(formatStr, locale);
		Date strtodate = null;
		try {
			strtodate = formatter.parse(strDate);
		} catch (ParseException e) {
			logger.error(e);

		}
		return strtodate;
	}

	public static String getLogTime() {
		return dateToStr(new Date(), DateUtils.DF_yyyyMMddHHmmss);
	}

	public static int getIntervalDaysByTwoDate(Date formDate, Date toDate) {
		int recoupDays = 0;
		if (formDate != null && toDate != null) {
			Calendar calendarFrom = Calendar.getInstance();
			calendarFrom.setTime(formDate);
			calendarFrom.set(Calendar.HOUR_OF_DAY, 0);
			calendarFrom.set(Calendar.MINUTE, 0);
			calendarFrom.set(Calendar.SECOND, 0);
			calendarFrom.set(Calendar.MILLISECOND, 0);

			Calendar calendarTo = Calendar.getInstance();
			calendarTo.setTime(toDate);
			calendarTo.set(Calendar.HOUR_OF_DAY, 0);
			calendarTo.set(Calendar.MINUTE, 0);
			calendarTo.set(Calendar.SECOND, 0);
			calendarTo.set(Calendar.MILLISECOND, 0);
			long time = calendarFrom.getTime().getTime() - calendarTo.getTime().getTime();
			recoupDays = (int) (time / (24 * 3600 * 1000));
		}
		return recoupDays;
	}

	public static String timeStamp2Date(String seconds, String format) {
		if (seconds == null || seconds.isEmpty() || seconds.equals("null")) {
			return "";
		}
		if (format == null || format.isEmpty()) {
			format = "yyyy-MM-dd HH:mm:ss";
		}
		TimeZone timeZone = TimeZone.getTimeZone("GMT+5:30");
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		sdf.setTimeZone(timeZone);
		// 类库使用时候，也保证有个正确结果
		if(seconds.length() == 10){
			seconds = seconds + "000";
		}
		return sdf.format(new Date(Long.valueOf(seconds)));
	}

	public static String timeStamp2Date(String millisecond) {
		if (millisecond == null || millisecond.isEmpty() || millisecond.equals("null")) {
			return "";
		}
		TimeZone timeZone = TimeZone.getTimeZone("GMT+5:30");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		sdf.setTimeZone(timeZone);
		return sdf.format(new Date(Long.valueOf(millisecond)));
	}

	public static void main(String[] args) throws ParseException {
		System.out.println(DateUtils.strToDate("13:20"));
	}

}
