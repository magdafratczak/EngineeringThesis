package com.pp.trenerpol.util;

import org.apache.commons.lang3.time.DateUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Locale;

public enum DateTimeFormatUtil {
	;

	public static Date parseStringAsDate(String stringDate) throws ParseException {
		return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(stringDate);
	}

	public static LocalDate getLocalDateFromDate(Date date) {
		return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
	}

	public static Date getDateFromLocalDate(LocalDate localDate) {
		return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
	}

	public static LocalDateTime getLocalDateTimeNow() {
		return LocalDateTime.now(ZoneId.systemDefault());
	}

	public static Date getDateFromLocalDateTime(LocalDateTime localDateTime) {
		ZonedDateTime zdtSource = ZonedDateTime.now(ZoneId.systemDefault());
		return Date.from(localDateTime.toInstant(zdtSource.getOffset()));
	}

	public static Date calculateEndDate(Date startDate, int duration) {
		return DateUtils.addMinutes(startDate, duration);
	}
}
