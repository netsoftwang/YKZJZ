package com.seeds.yk;

import java.io.File;

import org.junit.Test;



public class Simple {
	
	@Test
	public void testFile() {
		//退出检查:活跃时间差:61;文件大小:17292,当前大小:17548kb
		File file =new File("c:/Users/wzj/git/YKZJZ/violin/樂易第4冊1--前言.mp4");
		System.out.println(file.length());
		
	}
}
