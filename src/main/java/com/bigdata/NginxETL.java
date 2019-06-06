package com.bigdata;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.time.LocalDateTime;
import java.util.zip.GZIPInputStream;

import javax.swing.InputMap;

public class NginxETL {

	@SuppressWarnings("resource")
	public static void main(String[] args) throws Exception {
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("f:/test/nginx.log"), "utf-8"));
		String line = null;
		if((line = reader.readLine()) != null) {
			System.out.println(line); 
			String data = decoder(line);
			System.out.println("最终数据为:|"+data+"|");
			
		}
		
		LocalDateTime now = LocalDateTime.now();
		System.out.println(now);
		
		
		
		
		

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
		
		String data = line.split("data_list=")[1];
//		System.out.println("|"+data+"|");
		data = data.replaceAll("%25", "%");
        data = URLDecoder.decode(data, "UTF-8");
//        System.out.println("|"+data+"|");
        byte[] baseByte = Base64Coder.decode(data);
//        System.out.println("|"+ new String(baseByte)+"|"); 
        data = GZIPUtils.uncompressToString(baseByte);
//        System.out.println("|"+data+"|");
      
		return data;
	}

}
