package com.bigdata;

import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.temporal.TemporalField;
import java.util.Date;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.DateUtils;

import com.bigdata.util.IPAddressUtils;

import javafx.util.converter.LocalDateTimeStringConverter;

public class Test2 {

	@SuppressWarnings("deprecation")
	public static void main(String[] args) throws Exception {
		IPAddressUtils ipAddressUtils = new IPAddressUtils();
		ipAddressUtils.init();
		
		String ip = "223.88.210.129";
		String province = ipAddressUtils.getIPLocation(ip).getCountry(); //获取到的区域信息 "$province":"",
    	System.out.println("省份：-----------------"+province + "===========");
        String city = ipAddressUtils.getIPLocation(ip).getCity(); //获取到的城市  "$city":"",
        String carrier = ipAddressUtils.getIPLocation(ip).getArea(); //运营商  "$carrier":""
		
		
		
		
		
		
		
		
		
		
		
		
		
		
	}

}
