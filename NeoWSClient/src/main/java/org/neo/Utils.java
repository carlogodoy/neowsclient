package org.neo;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class Utils {


	/*
	 * Method: getNowDate
	 *       -returns today's date with required format: year-month-day
	 */
	public static String getNowDateFormatted() {
		Calendar cal = new GregorianCalendar();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		dateFormat.setTimeZone(cal.getTimeZone());
		return dateFormat.format(cal.getTime());		
	}

	/*
	 * Method: getNowPlusN
	 *       -returns today's date PLUS N days with required format: year-month-day
	 */
	public static String getNowPlusNDateFormatted(int N) {
		if(N>7) {
			System.out.println("Max allowed days for NASA API is 7 days");
			return null;
		}
		Calendar cal = new GregorianCalendar();
		cal.add(Calendar.DAY_OF_MONTH, N);
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		dateFormat.setTimeZone(cal.getTimeZone());
		return dateFormat.format(cal.getTime());		
	}

}

