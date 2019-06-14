package com.bigdata;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Iterator;

import org.apache.commons.io.IOUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class Test {

	@SuppressWarnings("resource")
	public static void main(String[] args) throws Exception {
		String str = IOUtils.toString(new FileInputStream("f:/test/a.txt"), "utf-8"); //把json读成一个string
		
		System.out.println(str);
		
		JSONArray jsonArray = JSON.parseArray(str);
		for(int i=0; i<jsonArray.size(); i++) {
			JSONObject jsonObject = jsonArray.getJSONObject(i);
			String type = jsonObject.getString("type"); //获取类型
			//事件逻辑
            if(type.equals("track") || type.equals("track_signup")) {
            	JSONObject propertiesJsonObject = jsonObject.getJSONObject("properties"); 
            	String event = jsonObject.getString("event");
            	String distinct_id = jsonObject.getString("distinct_id");
            	String time = jsonObject.getString("time");
            	
            	
            }
            
           
		}
		
		
	}

}
