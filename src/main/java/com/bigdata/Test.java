package com.bigdata;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.IOUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class Test {

	@SuppressWarnings("resource")
	public static void main(String[] args) throws Exception {
		String str = IOUtils.toString(new FileInputStream("f:/test/a.txt"), "utf-8"); //把json读成一个string
		
		System.out.println(str);
		
		JSONObject jsonObject = JSON.parseObject(str);
		System.out.println(jsonObject.toJSONString());
		jsonObject.put("age", 23);
		System.out.println(jsonObject.toJSONString());
		
		Set<Entry<String,Object>> entrySet = jsonObject.entrySet();
		for(Entry<String,Object> entry : entrySet) {
			//System.out.println(entry.getKey() + ":" + entry.getValue());
		}
		
		
		
	}

}
