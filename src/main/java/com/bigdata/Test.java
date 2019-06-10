package com.bigdata;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

public class Test {

	@SuppressWarnings("resource")
	public static void main(String[] args) throws Exception {
		File file = new File("f:/test/a.txt");
		System.out.println(file.length());
		BufferedWriter write = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("f:/test/a.txt"))); 
		while(true) {
			write.write("hello");
			write.newLine();
			write.flush();
			System.out.println("写...");
			
			Thread.sleep(3000);
		}
	}

}
