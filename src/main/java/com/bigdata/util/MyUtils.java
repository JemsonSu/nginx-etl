package com.bigdata.util;

import java.io.Closeable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Wrapper;

public class MyUtils {
	/**
	 * 关闭所有io(实现Closeable接口),以及数据库连接相关的资源(实现Wrapper接口)
	 * 
	 * @param closeableObjs 所有带有close()方法的类
	 * 
	 */
	public static void close(Object... closeableObjs) {
		try {
			int len = 0;
			if (closeableObjs != null && (len = closeableObjs.length) > 0) {
				Object closeableObj = null;
				for (int i = 0; i < len; i++) { // 遍历每一个需要关闭的资源
					closeableObj = closeableObjs[i];
					if (closeableObj != null) {
						if (closeableObj instanceof Closeable) { // IO资源 Closeable
							((Closeable) closeableObj).close();
						} else if (closeableObj instanceof Wrapper) { // 数据库修改资源 Wrapper
							if (closeableObj instanceof Connection) {
								((Connection) closeableObj).close();
							} else if (closeableObj instanceof Statement) {
								((Statement) closeableObj).close();
							} else if (closeableObj instanceof ResultSet) {
								((ResultSet) closeableObj).close();
							}
						}
					}
					closeableObj = null; // 置空
				}
			}
		} catch (Exception e) {
			//log.warn("关闭" + closeableObjs + "异常", e);
			e.printStackTrace();
			System.err.println("关闭" + closeableObjs + "异常" + e); 
		}
	}
}
