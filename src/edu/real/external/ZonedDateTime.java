package edu.real.external;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class ZonedDateTime
{
	// There is no non-digit chars, so use US locale just to avoid the warning.
	static final SimpleDateFormat parse_format = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss", Locale.US);

	Date date;
	TimeZone zone;

	public ZonedDateTime(Date date, TimeZone zone)
	{
		if (date == null) {
			date = new Date();
		}
		if (zone == null) {
			zone = TimeZone.getDefault();
		}
		this.date = date;
		this.zone = zone;
	}

	public ZonedDateTime(Date date)
	{
		this(date, null);
	}

	public ZonedDateTime()
	{
		this(null, null);
	}

	public ZonedDateTime(long msec)
	{
		this(new Date(msec));
	}

	public static ZonedDateTime parse(String s) throws ParseException
	{
		String date = s.substring(0, 19);
		String zone = "GMT" + s.substring(19);
		return new ZonedDateTime(parse_format.parse(date),
				TimeZone.getTimeZone(zone));
	}

	public String toString()
	{
		// see comment for parse_format about Locale
		SimpleDateFormat format = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ssZ", Locale.US);
		format.setTimeZone(this.zone);
		return format.format(this.date);
	}
}
