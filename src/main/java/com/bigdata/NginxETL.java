package com.bigdata;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URLDecoder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bigdata.util.HashUtil;
import com.bigdata.util.MyUtils;

public class NginxETL {

	@SuppressWarnings("resource")
	public static void main(String[] args) throws Exception {
		
		String sOldDir = "";
		String sOldFile = "";

		while (true) {
			BufferedWriter hourWiter = null;
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("/home/data/nginx/logs/nginx.log"), "utf-8"));
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("/home/data/nginx/logs/Jemson_etl/post-data.txt"), "utf-8"));
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH"); // 先用分钟代替，后面再改为小时

			


			//读nginx.log文件每一行
			String line = null;
			while ((line = myReadLine(reader)) != null ) { 
				String ip = getIP(line); 
				//System.out.println(line); 
				line = decoder(line);
				//System.out.println("最终数据为:|"+line+"|");
				writer.write(line);
				writer.newLine();
				writer.flush();
				
				
				LocalDateTime dateTime = LocalDateTime.now();
				String sDateTime = formatter.format(dateTime);
				String[] dt = sDateTime.split(" ");
				String sDate = dt[0]; // yyyy-MM-dd
				String sTime = dt[1]; // HH

				// 当前目录 到时修改为全路径
				String sDir = "/home/data/nginx/logs/Jemson_etl/" + sDate;
				// 当前文件
				String sFile = sDir + "/post-data.txt_" + sTime;
				
				
				if (!sDir.equals(sOldDir)) {
					new File(sDir).mkdirs();
					sOldDir = sDir;
				}
				
				if (!sFile.equals(sOldFile) || hourWiter==null) {
					MyUtils.close(hourWiter); 
					hourWiter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(sFile,true), "utf-8"));
					sOldFile = sFile;
				}
				
				hourWiter.write(line);
				hourWiter.newLine();
				hourWiter.flush();
				   
				
				//Thread.sleep(10*1000);
			}
			System.out.println("读完nginx/logs/nginx.log文件!");

			// 读完一个文件
			MyUtils.close(hourWiter ,writer ,reader); 

		
			
			
			Thread.sleep(3*1000); //预留时间给nginx切换文件

		}

	
		
		
		
		
		

	}
	
	
	/**
	 * 读取一行，并判断是当天时间内
	 * 
	 */
	public static String myReadLine(BufferedReader reader) {
		String line = null;
		try {
			while(true) {
				line = reader.readLine();
				//System.out.println("line = " + line); 
				if(line!=null ) {
					break;
				} 
				if (isBetweenTime()) {
					System.out.println("到达第二天,跳出");
					line=null; //第二天跳出故而置空
					Thread.sleep(60 * 1000);
					break;
				} 
				
				Thread.sleep(1000);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return line;
	}
	
	//判断在nginx.log滚动时间之间
	public static boolean isBetweenTime() {
		boolean flag = false;
		LocalTime now = LocalTime.now();
		
		//在这个时间休息一分钟
		LocalTime lt4 = LocalTime.parse("00:01:00");
		LocalTime lt5 = LocalTime.parse("00:01:59");
		
		
		flag = now.isAfter(lt4) && now.isBefore(lt5);
		
		
		
		return flag;
		
	}
	
	
	
	
	/**
	 * 数据处理  即数据清洗
	 * @param line
	 * @return
	 */
	public static String processData(String line, String ip) {
		StringBuilder sb = new StringBuilder();
		try {
			JSONArray jsonArray = JSON.parseArray(line); //默认当数组处理
			for(int i=0; i<jsonArray.size(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				String type = jsonObject.getString("type"); //获取类型
				//事件逻辑
                if(type.equals("track") || type.equals("track_signup")) {
                	JSONObject propertiesJsonObject = jsonObject.getJSONObject("properties"); 
                	String event = jsonObject.getString("event");
                	String distinct_id = jsonObject.getString("distinct_id");
                	String time = jsonObject.getString("time");
                	
                	//把 hash值 转换为 long值
                    Long user_id = HashUtil.userIdHash(distinct_id.toString());
                	
                	
                }
			}
			
			
			
		} catch (Exception e) {
			System.out.println("解析异常数据:"+line);
			e.printStackTrace();
		}
		return sb.toString();
	}
	
	/**
	 * 从未解压的数据中，获取访问ip
	 * @param line
	 * @return
	 */
	public static String getIP(String line) {
		String ip = "";
		try {
			ip = line.split("-")[1].trim();
		} catch (Exception e) {
			System.err.println("获取ip失败,原数据为："  + line);
			e.printStackTrace();
		}
		
		return ip;
	}
	
	
	public static long isAfterWeekBeTwoHour(long time) {
		try {
			Date date = new Date(time); //获取时间戳
			Date addDays = DateUtils.addDays(new Date(), -7); //一周前
			Date addHours = DateUtils.addHours(new Date(), 2); //2小时后
			if( date.before(addDays) || date.after(addHours) ) { 
				time = System.currentTimeMillis();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return time;
	}
	
	
	public static String decoder(String line) throws Exception {
        /*
        1.对发送的数据进行 gzip压缩 、Base64.encode编码、URLEncoder.encode编码，最后再进行http传输数据源数据
                --> gzip压缩  --> Base64.encode编码 --> URLEncoder.encode编码 --> http传输数据
        2.对http接收到的数据进行 URLDecoder.decode解码、Base64.decode解码、ungzip解压缩 得到最终的原始数据
                http传输的数据 --> URLDecoder.decode解码 --> Base64.decode解码 --> ungzip解压缩
        */
		
		String data = "";
		try {
			data = line.split("data_list=")[1];
//		System.out.println("|"+data+"|");
			data = data.replaceAll("%25", "%");
			data = URLDecoder.decode(data, "UTF-8");
//        System.out.println("|"+data+"|");
			byte[] baseByte = Base64Coder.decode(data);
//        System.out.println("|"+ new String(baseByte)+"|"); 
			data = GZIPUtils.uncompressToString(baseByte);
//        System.out.println("|"+data+"|");
		} catch (Exception e) {
			System.err.println(LocalDateTime.now() +" 异常数据为：" + line);
			e.printStackTrace();
		}
      
		return data;
	}

}
