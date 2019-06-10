package com.bigdata;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URLDecoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.bigdata.util.MyUtils;

public class NginxETL {

	@SuppressWarnings("resource")
	public static void main(String[] args) throws Exception {
		
		String sOldDir = "";
		String sOldFile = "";

		while (true) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("/home/data/nginx/logs/nginx.log"), "utf-8"));
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("/home/data/nginx/logs/Jemson_etl/post.log"), "utf-8"));
			BufferedWriter hourWiter = null;
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH"); // 先用分钟代替，后面再改为小时

			


			String line = null;
			while ((line = reader.readLine()) != null) {
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
				String sFile = sDir + "/post.log_" + sTime;
				
				
				if (!sDir.equals(sOldDir)) {
					new File(sDir).mkdirs();
					sOldDir = sDir;
				}
				
				if (!sFile.equals(sOldFile)) {
					if(hourWiter!=null) {
						//hourWiter.close();
						MyUtils.close(hourWiter); 
					}
					hourWiter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(sFile), "utf-8"));
					sOldFile = sFile;
					
				}
				
				hourWiter.write(line);
				hourWiter.newLine();
				hourWiter.flush();
				
				
				//Thread.sleep(10*1000);
			}
			System.out.println("读完nginx/logs/nginx.log文件!");

			// 读完一个文件
			//hourWiter.close();
			//reader.close();
			//writer.close();
			MyUtils.close(hourWiter,reader,writer); 

		
			
			
			Thread.sleep(3*1000); //预留时间给nginx切换文件

		}

	
		
		
		
		
		

	}
	
	/**
	 * 数据处理  即数据清洗
	 * @param line
	 * @return
	 */
	public static String processData(String line) {
		line += line+"kk";
		return line;
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
