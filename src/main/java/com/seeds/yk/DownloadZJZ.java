package com.seeds.yk;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteResultHandler;
import org.apache.commons.exec.ExecuteStreamHandler;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.util.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import com.alibaba.fastjson.JSONObject;



public class DownloadZJZ {
	
	@Test
	public void testDown() {
		String path = "c:/Users/wzj/git/YKZJZ/violin";
		String line = "you-get -o violin http://v.youku.com/v_show/id_XMzgyMjkwMTEzMg==.html?spm=a2hzp.8244740.0.0";
		new DownloadZJZ(line, path).downLoad();
	}
	public static void main(String[] args) {
		try {
			DB.db.start();
			String sql = " select * from tbZJZ limit 10 ";
			String path = "c:/Users/wzj/git/YKZJZ/violin";
			List<Map<String,Object>> mapList = DB.db.runner.query(sql, new MapListHandler());
			for(Map<String,Object> map : mapList){
				System.out.println(JSONObject.toJSONString(map,true));
				String line = "you-get -o violin http://v.youku.com"+map.get("STRLINKURL");
				if(line.contains("baidu")) {
					
				}else {
					new DownloadZJZ(line, path).downLoad();
				}

			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static void main111(String[] args) {
		String path = "c:/Users/wzj/git/YKZJZ/violin";
		String line = "you-get -o violin http://v.youku.com/v_show/id_XMzc0Nzg5NDMxNg==.html?spm=a2hzp.8244740.0.0&f=51836574";
		line = "you-get -o violin http://v.youku.com/v_show/id_XMzc2NTY0NzExMg==.html";
		new DownloadZJZ(line, path).downLoad();
	}
	public DownloadZJZ(String command,String filePath) {
		this.command = command;
		this.filePath = filePath;
	}
	
	public AtomicLong size = new AtomicLong(0);
	public volatile String filePath;
	public volatile String fileName;
	public AtomicBoolean isEnd = new AtomicBoolean(false);
	public AtomicBoolean youGetEnd = new AtomicBoolean(false);
	public AtomicBoolean isDebug = new AtomicBoolean(true);
	public AtomicBoolean isTimeOut = new AtomicBoolean(false);
	public AtomicLong currTime = new AtomicLong(0);
	public String command;
	public ExecuteWatchdog dog;
	public ExecuteResultHandler resultHandler;
	ExecuteStreamHandler streamHandler;
	
	public void downLoad() {
		this.streamHandler = getStreamHandler();
		this.dog = new ExecuteWatchdog(-1);
		this.resultHandler = new DefaultExecuteResultHandler();
		exec(this.command,this.dog,this.resultHandler,this.streamHandler);
		ScheduledThreadPoolExecutor sche = new ScheduledThreadPoolExecutor(1);
		sche.scheduleAtFixedRate(()->{
			StringBuilder sb =new StringBuilder();
			if(currTime.get() == 0 ) {
				return;
			}
			long diff = System.currentTimeMillis() - currTime.get();
			sb.append("活跃时间差:"+(diff/1000)+"s;");
			if(diff > 60 * 1000) {
				isEnd.set(true);
				isTimeOut.set(true);
			}
			if(StringUtils.isNoneBlank(filePath)) {
				File file = new File(filePath+File.separator+fileName);
				if(file.exists()) {
					long len = file.length();
					String msg = "文件大小:"+(size.get()/1024)+",当前大小:"+(len/1024)+"kb";
					sb.append(msg);
					if(size.get() > 0 && len > size.get() && youGetEnd.get()) {
						isEnd.set(true);
					}
				}
			}
			System.out.println("退出检查:"+sb.toString());
		}, 0, 5,TimeUnit.SECONDS);
		
		while(true) {
			try {
				if(isEnd.get()) {
					System.out.println(this.filePath+File.separator+this.fileName+"下载完成");
					this.dog.destroyProcess();
					Thread.currentThread().sleep(10*1000);
					break;
				}
				Thread.currentThread().sleep(5*1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		sche.shutdownNow();
		if(isTimeOut.get()) {
			System.out.println("===超时退出");
		}
		System.out.println("===线程退出====");
	}
	
	public void exec(String command,ExecuteWatchdog dog,ExecuteResultHandler resultHandler,ExecuteStreamHandler streamHandler) {
		CommandLine cline = CommandLine.parse(command);
		DefaultExecutor exe = new DefaultExecutor();
		exe.setExitValue(0);
		exe.setStreamHandler(streamHandler);
		exe.setWatchdog(dog);
		try {
			exe.execute(cline,resultHandler);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		return;
	}

	public ExecuteStreamHandler getStreamHandler() {
		return new ExecuteStreamHandler() {
			@Override
			public void stop() throws IOException {
				youGetEnd.set(true);
				System.out.println("====stop===");
			}
			
			@Override
			public void start() throws IOException {
				currTime.set(System.currentTimeMillis());
				System.out.println("start");
			}
			
			@Override
			public void setProcessOutputStream(InputStream is) throws IOException {
				try {
					currTime.set(System.currentTimeMillis());
					byte[] bArr = new byte[1024];
					int len = -1;
					while((len = is.read(bArr)) > 0) {
						String ss = new String(bArr,"UTF-8");
						if(isDebug.get()) {
							System.out.println("readLineVal:"+ss);
						}
						if(ss.contains("size") && ss.contains("bytes)") && ss.contains("(")) {
							String sSize = ss.split("bytes\\)")[0].split("MiB \\(")[1];
							size.set(Long.parseLong(sSize.trim()));
							if(size.get() == 0 ) {
								throw new RuntimeException("文件长度获取失败:"+ss);
							}
						}
						if(ss.contains("Downloading") && ss.contains("mp4")) {
							String fName = ss.split("Downloading")[1].trim().split(".mp4")[0]+".mp4";
							fileName = fName;
						}
						len = 0;
					}
				}catch(Exception e) {
					e.printStackTrace();
				}
			}
			@Override
			public void setProcessInputStream(OutputStream os) throws IOException {
			}
			@Override
			public void setProcessErrorStream(InputStream is) throws IOException {
			}
		};
	}

}
