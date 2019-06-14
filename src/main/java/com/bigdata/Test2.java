package com.bigdata;

import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.temporal.TemporalField;
import java.util.Date;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.DateUtils;

import javafx.util.converter.LocalDateTimeStringConverter;

public class Test2 {

	@SuppressWarnings("deprecation")
	public static void main(String[] args) throws Exception {
		String s = "1560355261207";
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = new Date(System.currentTimeMillis());
		
		//System.out.println(format.format(new Date(NginxETL.isAfterWeekBeTwoHour(15603552612075L))));
		
		
		Date date3 = DateUtils.addHours(date, 3); 
		System.out.println(format.format(date3));
		System.out.println(format.format(new Date(NginxETL.isAfterWeekBeTwoHour(date3.getTime()))));
		
		
		
		
		
		
		
		
		
		
		
		
		
		
	}

}
