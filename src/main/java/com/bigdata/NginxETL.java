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
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.time.DateUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bigdata.util.HashUtil;
import com.bigdata.util.IPAddressUtils;
import com.bigdata.util.MyUtils;

public class NginxETL {
	public static IPAddressUtils ipAddressUtils = new IPAddressUtils();
	//所有 省份、自治区、直辖市、特别行政区
    public static Map<String,String> hashMap = new HashMap<String,String>();
    //所有 自治区、直辖市、特别行政区
    public static Map<String,String> hashMap1 = new HashMap<String,String>();
    //所有 自治区
    public static Map<String,String> hashMap2 = new HashMap<String,String>();

	@SuppressWarnings("resource")
	public static void main(String[] args) throws Exception {
		//初始化ip库
		ipAddressUtils.init();
		//所有 省份、自治区、直辖市、特别行政区
        hashMap.put("上海","");
        hashMap.put("北京","");
        hashMap.put("天津","");
        hashMap.put("安徽","");
        hashMap.put("澳门","");
        hashMap.put("香港","");
        hashMap.put("福建","");
        hashMap.put("甘肃","");
        hashMap.put("广东","");
        hashMap.put("广西","");
        hashMap.put("贵州","");
        hashMap.put("海南","");
        hashMap.put("河北","");
        hashMap.put("河南","");
        hashMap.put("黑龙","");
        hashMap.put("湖北","");
        hashMap.put("湖南","");
        hashMap.put("吉林","");
        hashMap.put("江苏","");
        hashMap.put("江西","");
        hashMap.put("辽宁","");
        hashMap.put("内蒙","");
        hashMap.put("宁夏","");
        hashMap.put("青海","");
        hashMap.put("山东","");
        hashMap.put("山西","");
        hashMap.put("陕西","");
        hashMap.put("四川","");
        hashMap.put("西藏","");
        hashMap.put("新疆","");
        hashMap.put("云南","");
        hashMap.put("浙江","");
        hashMap.put("重庆","");
        hashMap.put("台湾","");

        /*
            4个直辖市：北京市、天津市、上海市、重庆市
            5个自治区：广西壮族自治区、内蒙古自治区、西藏自治区、宁夏回族自治区、新疆维吾尔自治区
    　　    2个特别行政区：香港特别行政区、澳门特别行政区
        */
        hashMap1.put("北京","北京市");
        hashMap1.put("天津","天津市");
        hashMap1.put("上海","上海市");
        hashMap1.put("重庆","重庆市");
        hashMap1.put("广西","广西壮族自治区");
        hashMap1.put("内蒙","内蒙古自治区");
        hashMap1.put("西藏","西藏自治区");
        hashMap1.put("宁夏","宁夏回族自治区");
        hashMap1.put("新疆","新疆维吾尔自治区");
        hashMap1.put("香港","香港特别行政区");
        hashMap1.put("澳门","澳门特别行政区");
        hashMap1.put("台湾","台湾省");

        //5个自治区：广西  壮族自治区、内蒙古 自治区、西藏 自治区、宁夏  回族自治区、新疆  维吾尔自治区
        hashMap2.put("广西","2");
        hashMap2.put("内蒙","3");
        hashMap2.put("西藏","2");
        hashMap2.put("宁夏","2");
        hashMap2.put("新疆","2");
		
		
		//IP地址库
		
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
				//解压
				line = decoder(line);
				//清洗
				line = processData(line, ip);
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
		//连读10次 读不了数据重新来读取   
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
				/*
                if(type.equals("track") || type.equals("track_signup")) {
                	JSONObject propertiesJsonObject = jsonObject.getJSONObject("properties"); 
                	String event = jsonObject.getString("event");
                	String distinct_id = jsonObject.getString("distinct_id");
                	String time = jsonObject.getString("time");
                	
                	//把 hash值 转换为 long值
                    Long user_id = HashUtil.userIdHash(distinct_id.toString());
                	
                	
                }*/
                //后面在弄上面的情况
                JSONObject propertiesJsonObject = jsonObject.getJSONObject("properties"); 
            	String event = jsonObject.getString("event");
            	String distinct_id = jsonObject.getString("distinct_id");
            	String time = jsonObject.getString("time");
            	
            	String province = ipAddressUtils.getIPLocation(ip).getCountry(); //获取到的区域信息 "$province":"",
                String city = ipAddressUtils.getIPLocation(ip).getCity(); //获取到的城市  "$city":"",
                String carrier = ipAddressUtils.getIPLocation(ip).getArea(); //运营商  "$carrier":""
                
                String substring = province.substring(0,2); //获取字符串的前两个字符
                int i1 = province.indexOf("省");
                int i2 = city.indexOf("市");
                
              //只要带有省份名 都用中国
                if (hashMap.get(substring) !=null)
                {
                	propertiesJsonObject.put("$country","中国");
                }
                //只要带有 省 或 市 都用中国
                else if(i1 != -1 || i2 != -1)//-1 表示找不到，不存在
                {
                	propertiesJsonObject.put("$country","中国");
                }
                else
                {
                	propertiesJsonObject.put("$country",province);
                }

//                                            int i1 = province.indexOf("省");
                if (i1 != -1) //-1 表示找不到，不存在
                {
                    province = province.substring(0,i1+1);
                    //如果遇到有 浙金省，则要替换为 浙江省
                    if("浙金省".equals(province))
                    {
                        province = "浙江省";
                    }
                    else if("广西省".equals(province))
                    {
                        province = "广西";
                    }
                    propertiesJsonObject.put("$province",province);
                }
                else //找不到"省"字符，代表有两种情况，一种是自治区/行政区/直辖市，另外一种是外国名
                {
                    /*
                        4个直辖市：北京市、天津市、上海市、重庆市
    　　                5个自治区：广西  壮族自治区、内蒙古  自治区、西藏  自治区、宁夏  回族自治区、新疆  维吾尔自治区
    　　                2个特别行政区：香港特别行政区、澳门特别行政区
                    */
                    String substring1 = province.substring(0, 2);
                    String s1 = hashMap1.get(substring1);
                    if(s1 == null)
                    {
                    	propertiesJsonObject.put("$province","");
                    }
                    else
                    {
                    	propertiesJsonObject.put("$province",s1);
                    }
                }

//                                           int i2 = city.indexOf("市");
                if (i2 != -1) //-1 表示找不到，不存在
                {
                    //5个自治区：广西  壮族自治区、内蒙古 自治区、西藏 自治区、宁夏  回族自治区、新疆  维吾尔自治区
                    String substring2 = city.substring(0, 2);
                    String s2 = hashMap2.get(substring2);
                    if(s2 != null)
                    {
                        int i3 = Integer.parseInt(s2);
                        city = city.substring(i3, i2+1);
                        propertiesJsonObject.put("$city",city);
                    }
                    else
                    {
                        city = city.substring(0,i2+1);
                        propertiesJsonObject.put("$city",city);
                    }
                }
//                                                   else //找不到"市"字符，代表有两种情况，一种是自治区/行政区/直辖市，另外一种是外国名
//                                                   {
//                                                       properties.put("$city",city);
//                                                    }

                propertiesJsonObject.put("$ip",ip);
                propertiesJsonObject.put("$carrier",carrier);
            	
            	
            	//把 hash值 转换为 long值
                Long user_id = HashUtil.userIdHash(distinct_id.toString());
                
                String jsonString = jsonObject.toJSONString();
                
                if (i==0) {
                	sb.append(jsonString);
				}else {
					sb.append("\n"+jsonString);
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
	
	/**
	 * 判断包含省份  后面弄
	 * @return
	 */
	public static void isIncludeProvince(String province) {
		//所有 省份、自治区、直辖市、特别行政区
	    Map<String,String> hashMap = new HashMap<String,String>();
	    //所有 自治区、直辖市、特别行政区
	    Map<String,String> hashMap1 = new HashMap<String,String>();
	    //所有 自治区
	    Map<String,String> hashMap2 = new HashMap<String,String>();
	    
		
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
