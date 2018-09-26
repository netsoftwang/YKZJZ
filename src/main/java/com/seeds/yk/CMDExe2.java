package com.seeds.yk;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteStreamHandler;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.lang3.StringUtils;
import org.hsqldb.lib.StringUtil;
import org.junit.Test;



public class CMDExe2 {
	
	@Test
	public void doLoad11() throws Exception {
		String line = "you-get -o violin http://v.youku.com/v_show/id_XMzgyMjkwMTEzMg==.html?spm=a2hzp.8244740.0.0";
		ExecuteWatchdog dog = exec11(line);
		new ScheduledThreadPoolExecutor(1)
		.scheduleAtFixedRate(()->{
			System.out.println("schdu:"+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
			
		}, 0,3,TimeUnit.SECONDS);
		
		Thread.currentThread().sleep(10*60*1000);
		System.out.println("end");
	}
	

	public ExecuteWatchdog exec11(String command) {
		CommandLine cline = CommandLine.parse(command);
		DefaultExecutor exe = new DefaultExecutor();
		exe.setExitValue(0);
		setStreamHandler(exe);
		ExecuteWatchdog dog = new ExecuteWatchdog(-1);
		exe.setWatchdog(dog);
		DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
		try {
			exe.execute(cline,resultHandler);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return dog;
	}
	public void setStreamHandler(DefaultExecutor exe) {
		exe.setStreamHandler(new ExecuteStreamHandler() {
			@Override
			public void stop() throws IOException {
				System.out.println("stop");
			}
			
			@Override
			public void start() throws IOException {
				System.out.println("start");
				
			}
			
			@Override
			public void setProcessOutputStream(InputStream is) throws IOException {
				byte[] bArr = new byte[1024];
				int len = -1;
				while((len = is.read(bArr)) > 0) {
					String ss = new String(bArr);
					System.out.println("readLineVal:"+ss);
					len = 0;
				}
			}
			
			@Override
			public void setProcessInputStream(OutputStream os) throws IOException {
				
			}
			
			@Override
			public void setProcessErrorStream(InputStream is) throws IOException {
				BufferedReader reader = new BufferedReader(new InputStreamReader(is));
				new Thread(()->{
					try {
						String readLine = reader.readLine();
						while(StringUtils.isNotBlank(readLine)) {
							System.out.println("errorMsg:"+readLine);
							Thread.currentThread().sleep(1*1000);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}).start();
				
				
			}
		});
	}


	@Test
	public void doLoad() throws Exception {
		String line = "you-get -o violin http://v.youku.com/v_show/id_XMzgyOTIyMDg0OA==.html?spm=a2hzp.8244740.0.0 ";
		DefaultExecuteResultHandler handler = exec(line);
		new ScheduledThreadPoolExecutor(1)
		.scheduleAtFixedRate(()->{
			System.out.println("schdu:"+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
			Exception ex = handler.getException();
			if(ex != null) {
				System.out.println(ex.getMessage());
			}
			int exitVal = handler.getExitValue();
			if(1 == exitVal) {
				System.out.println("exitVal:"+exitVal);
			}
		}, 0,3,TimeUnit.SECONDS);
		
		Thread.currentThread().sleep(10*60*1000);
		System.out.println("end");
	}
	
	public DefaultExecuteResultHandler exec(String command) {
		CommandLine cline = CommandLine.parse(command);
		DefaultExecutor exe = new DefaultExecutor();
		exe.setExitValue(0);
		ExecuteWatchdog dog = new ExecuteWatchdog(-1);
		exe.setWatchdog(dog);
		DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
		CompletableFuture<Integer> complete = null;
		try {
			exe.execute(cline,resultHandler);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return resultHandler;
	}
}
