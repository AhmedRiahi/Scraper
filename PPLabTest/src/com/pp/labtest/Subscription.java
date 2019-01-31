package com.pp.labtest;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Subscription {

	public static void main(String[] args) throws ParseException {
		DateFormat format = new SimpleDateFormat("yyyy-MM-dddd HH:mm:ss", Locale.ENGLISH);
		Date date = format.parse("2017-08-28 14:18:26");
		System.out.println(date);
	}
}
