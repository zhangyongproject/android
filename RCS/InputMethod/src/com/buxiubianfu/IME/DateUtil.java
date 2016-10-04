package com.buxiubianfu.IME;

import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Context;

/**
 * @ClassName: DateUtil
 * @Description:
 * @author Comsys-linbinghuang
 * @date 2014-11-3 下午5:34:40
 *
 */
public class DateUtil {

	/**
	 * string转成Date类型
	 * 
	 * @param dateString
	 * @param format
	 * @return
	 */
	public static Date StrToDate(String dateString, String format) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(format);
			Date date = sdf.parse(dateString);
			return date;
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

	}

	/**
	 * date转成string类型
	 * 
	 * @param date
	 * @param format
	 * @return
	 */
	public static String DateToStr(Date date, String format) {

		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(date);

	}

	/**
	 * js时间更是转换
	 * 
	 * @param time
	 * @param format
	 * @return
	 */
	public static String getMilliToDateForTime(String time, String format) {
		time = time.substring(6, time.length() - 2);
		Date date = new Date(Long.valueOf(time));
		SimpleDateFormat formatter = new SimpleDateFormat(format);
		return formatter.format(date);
	}

	/**
	 * string 转成毫秒
	 * 
	 * @param date
	 * @param format
	 * @return
	 */
	public static long stringToLong(String date, String format) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		Date dt = null;
		try {
			dt = sdf.parse(date);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return dt.getTime();
	}

	public static final String SHORT_DATE_FORMAT_2 = "yyyy-MM-dd";

	public static final String LONG_DATE_FORMAT_1 = "yyyyMMddHHmmss";

	/**
	 * 24小时
	 */
	public static final String C_TIME_PATTON_24HHMM = "HH:mm";

	/**
	 * 12小时
	 */
	public static final String C_TIME_PATTON_12HHMM = "hh:mm";

	/**
	 * 中文月份数组
	 */

	/**
	 * 英文月份数组
	 */
	public static final String[] monthsEn = { "Jan", "Feb", "Mar", "Apr",
			"May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };

	/**
	 * 获取现在时间
	 * 
	 * @return 返回时间类型 yyyy-MM-dd HH:mm:ss
	 */
	public static Date getNowDate() {
		Date currentTime = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dateString = formatter.format(currentTime);
		ParsePosition pos = new ParsePosition(8);
		Date currentTime_2 = formatter.parse(dateString, pos);
		return currentTime_2;
	}

	/**
	 * 获取现在时间
	 * 
	 * @param 时间字符串输出
	 * @return 返回对应格式的字符串
	 */
	public static String getStringDateByFormat(String format) {
		Date currentTime = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat(format);
		String dateString = formatter.format(currentTime);
		return dateString;
	}

	/**
	 * 获取现在时间
	 * 
	 * @return返回字符串格�?yyyy-MM-dd HH:mm:ss
	 */
	public static String getStringDate() {
		Date currentTime = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dateString = formatter.format(currentTime);
		return dateString;
	}

	/**
	 * 获取现在时间
	 * 
	 * @return 返回短时间字符串格式yyyy-MM-dd
	 */
	public static String getStringDateShort() {
		Date currentTime = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		String dateString = formatter.format(currentTime);
		return dateString;
	}

	/**
	 * 获取时间
	 * 
	 * @return 返回短时间字符串格式yyyy-MM-dd
	 */
	public static String getStringDate(long time) {
		Date currentTime = new Date(time);
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		String dateString = formatter.format(currentTime);
		return dateString;
	}

	/**
	 * 获取时间
	 * 
	 * @return 返回短时间字符串格式yyyy-MM-dd
	 */
	public static String getStringDate(long time, SimpleDateFormat format) {
		Date currentTime = new Date(time);
		String dateString = format.format(currentTime);
		return dateString;
	}

	/**
	 * 获取时间 小时:�?�?HH:mm:ss
	 * 
	 * @return
	 */
	public static String getTimeShort() {
		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
		Date currentTime = new Date();
		String dateString = formatter.format(currentTime);
		return dateString;
	}

	/**
	 * 将长时间格式字符串转换为时间 yyyy-MM-dd
	 * 
	 * @param strDate
	 * @return
	 */
	public static Date strToDateShort(String strDate) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		ParsePosition pos = new ParsePosition(0);
		Date strtodate = formatter.parse(strDate, pos);
		return strtodate;
	}

	/**
	 * 将长时间格式字符串转换为时间 yyyy-MM-dd HH:mm:ss
	 * 
	 * @param strDate
	 * @return
	 */
	public static Date strToDateLong(String strDate) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		ParsePosition pos = new ParsePosition(0);
		Date strtodate = formatter.parse(strDate, pos);
		return strtodate;
	}

	/**
	 * 将长时间格式时间转换为字符串 yyyy-MM-dd HH:mm:ss
	 * 
	 * @param dateDate
	 * @return
	 */
	public static String dateToStrLong(java.util.Date dateDate) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dateString = formatter.format(dateDate);
		return dateString;
	}

	/**
	 * 将短时间格式时间转换为字符串 yyyyMM
	 * 
	 * @param dateDate
	 * @param k
	 * @return
	 */
	public static String dateToStr(java.util.Date dateDate) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMM");
		String dateString = formatter.format(dateDate);
		return dateString;
	}

	/**
	 * 将短时间格式字符串转换为时间 yyyyMM
	 * 
	 * @param strDate
	 * @return
	 */
	public static Date strToDate(String strDate) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMM");
		ParsePosition pos = new ParsePosition(0);
		Date strtodate = formatter.parse(strDate, pos);
		return strtodate;
	}

	/**
	 * 得到现在时间
	 * 
	 * @return
	 */
	public static Date getNow() {
		Date currentTime = new Date();
		return currentTime;
	}

	/**
	 * 提取�?��月中的最后一�?
	 * 
	 * @param day
	 * @return
	 */
	public static Date getLastDate(long day) {
		Date date = new Date();
		long date_3_hm = date.getTime() - 3600000 * 34 * day;
		Date date_3_hm_date = new Date(date_3_hm);
		return date_3_hm_date;
	}

	/**
	 * 得到现在时间
	 * 
	 * @return 字符�?yyyyMMdd HHmmss
	 */
	public static String getStringToday() {
		Date currentTime = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd HHmmss");
		String dateString = formatter.format(currentTime);
		return dateString;
	}

	/**
	 * 得到现在小时
	 */
	public static String getHour() {
		Date currentTime = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dateString = formatter.format(currentTime);
		String hour;
		hour = dateString.substring(11, 13);
		return hour;
	}

	/**
	 * 得到现在分钟
	 * 
	 * @return
	 */
	public static String getTime() {
		Date currentTime = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dateString = formatter.format(currentTime);
		String min;
		min = dateString.substring(14, 16);
		return min;
	}

	/**
	 * 根据用户传入的时间表示格式，返回当前时间的格�?如果是yyyyMMdd，注意字母y不能大写�?
	 * 
	 * @param sformat
	 *            yyyyMMddhhmmss
	 * @return
	 */
	public static String getUserDate(String sformat) {
		Date currentTime = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat(sformat);
		String dateString = formatter.format(currentTime);
		return dateString;
	}

	/**
	 * 是否在两个日期之�?
	 * 
	 * @param dateStr1
	 * @param dateStr2
	 * @return
	 */
	public static boolean isBetweenDate(String dateStr1, String dateStr2) {
		try {
			long date1 = strToDateShort(dateStr1).getTime();
			long date2 = strToDateShort(dateStr2).getTime();
			long now = new Date().getTime();
			if (date1 < now && now < date2) {
				return true;
			}
		} catch (Exception e) {
			return false;
		}
		return false;
	}

	/**
	 * 年月信息
	 * 
	 * @author linbinghuang
	 *
	 */
	public static class YearMonthInfo {
		/**
		 * 年月标识，例201108,199912
		 */
		public String tag;
		/**
		 * 年月显示标签，中�?3月；英文：Mar
		 */
		public String label;
	};

	/**
	 * 获取今天日期的毫秒数
	 * 
	 * @return
	 */
	public static long getTodayTime() {
		try {
			Date date = new Date();
			SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd");
			String dateStr = dateformat.format(date);
			return dateformat.parse(dateStr).getTime();
		} catch (Exception e) {

			return 0;
		}
	}

	/**
	 * 日期字符串转�?pattern可以从R.string中获取，datetime_pattern_yyyymmddhhmmss,
	 * datetime_pattern_yyyy_mm_dd_hhmmss
	 * �?date_pattern_chinese,datetime_pattern_chinese�?
	 * 
	 * @param originalPattern
	 *            初始日期格式
	 * @param targetPattern
	 *            目标日期格式
	 * @param datetime
	 *            日期字符�?
	 * @return
	 */
	public static String formatDateTime(Context context,
			int originalPatternStrId, int targetPatternStrId, String datetime) {
		SimpleDateFormat sdf = new SimpleDateFormat(
				context.getString(originalPatternStrId), Locale.getDefault());
		Date date = null;
		try {
			date = sdf.parse(datetime);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		if (date != null) {
			sdf = new SimpleDateFormat(context.getString(targetPatternStrId),
					Locale.getDefault());
			String target = sdf.format(date);
			return target;
		}
		return null;

	}

	/**
	 * 从公元元年算�?
	 * 
	 * @param type
	 * @param time
	 * @return
	 */
	public static String getTimeByJavaToADZero(String type, String time) {

		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat(type);
			Date data = dateFormat.parse(time);
			return data.getTime()
					+ (long) (1970 * 365 + (25 * 20 - 8) - 19 + 5) * 24 * 60
					* 60 * 1000 + "0000";

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
