package com.seeds.yk;

import java.io.File;

import org.junit.Test;



public class Simple {
	
	@Test
	public void testFile() {
		//�˳����:��Ծʱ���:61;�ļ���С:17292,��ǰ��С:17548kb
		File file =new File("c:/Users/wzj/git/YKZJZ/violin/���׵�4��1--ǰ��.mp4");
		System.out.println(file.length());
		
	}
}
