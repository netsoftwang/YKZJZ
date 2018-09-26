package com.seeds.yk;

import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Before;
import org.junit.Test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;



public class YKDownLoad {
	
	public CloseableHttpClient  client;
	public BasicCookieStore cookieStore;
	
	@Test
	public void doDownLoad(){
		String url ="http://103.43.209.209/6975D468B92347A0724DD31A4/03000B01005B6C2C8B12040052CDD5BE3A19A4-F3DD-4D27-B69C-A02D83871135.mp4.ts?ccode=0502&duration=205&expire=18000&psid=9ee559933ddcf1f992c8e3656c5e7ba5&sp=&ups_client_netip=df1500e2&ups_ts=1537449786&ups_userid=&utid=ya%2FFEV1RfhkCAd8VHd%2F32sIw&vid=XMzc3MjA4ODUzNg%3D%3D&vkey=Bcfd1f046658e23f5e8fb56cdb81e01f8&ts_start=17.9&ts_end=29.9&ts_seg_no=3&ts_keyframe=1";
		url ="http://vali.cp31.ott.cibntv.net/youku/6773C3C042C4571BB20212AC2/03000B01005B90902028505003E880726E9BE0-5CB3-43A1-9F46-ED6AE628E984.mp4?sid=053744740561916195408_00_A29b83b27b6b9684a37dcb6683db280c1&sign=49faa51d54570b28b318d7cb513aec88&ctype=50&hd=2";
		url ="http://i.youku.com/i/UMzQ3MzA1MzM2/videos?spm=a2hzp.8244740.0.0";
		String html = doPost(url);
		Document doc = Jsoup.parse(html);
		//getItermList(doc);
	}
	
	
	
	public List<Map<String,String>> getItermList(Document doc) {
		List<Map<String,String>> mapList = new ArrayList<>();
		Iterator<Element> ite = doc.select("div.videos-list").select("div.items").select("div.v-link").iterator();
		while(ite.hasNext()) {
			Element ele = ite.next();
			Map<String,String> map = new HashMap<String, String>();
			ele = ele.select("a").get(0);
			map.put("href", ele.attr("href"));
			map.put("title", ele.attr("title"));
			mapList.add(map);
			//System.out.println(JSONObject.toJSONString(map,true));
		}
        return mapList;
	}
	
	@Before 
	public void bef() {
		
	}
	@Test
	public void getPages(){
		DB.db.hsqlDB();
		DB.db.start();
		del();
		String url ="http://i.youku.com/i/UMzQ3MzA1MzM2/videos?spm=a2hzp.8244740.0.0";
		Map<String,String> map = new HashMap<>();
		map.put("1", url);
		getAllPage(url,map);
		String ss = JSON.toJSONString(map,true);
		System.out.println(ss);
		String base = "http://i.youku.com";
		for(Map.Entry<String, String> ent : map.entrySet()) {
			String pageUrl = ent.getValue();
			if(!pageUrl.startsWith("http")) {
				pageUrl =base+pageUrl;
			}
			System.out.println("pageUrl==="+pageUrl);
			Document doc = Jsoup.parse(doPost(pageUrl));
			for(Map<String,String> mapVal : getItermList(doc)) {
				System.out.println("title:"+mapVal.get("title")+";href:"+mapVal.get("href"));
				insertItem(mapVal.get("title"), mapVal.get("href"));
			}
		}
	}
	public void del() {
		String sql = " delete from tbZJZ where lId > 0 ";
		try {
			DB.db.runner.update(sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void insertItem(String title,String url) {
		//String sql =" insert into tbZJZ(nState,strLinkName,strLinkUrl,strMsg,strDateTime) values(1,'test','http://baidu.com','msg','2018-12-12 23:12:32')";
		String sql =" insert into tbZJZ(nState,strLinkName,strLinkUrl,strMsg,strDateTime) values(1,?,?,?,?)";
		try {
			DB.db.runner.update(sql,title,url,"",new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void getAllPage(String link,Map<String,String> map ) {
		String html = doPost(link);
		Document doc = Jsoup.parse(html);
		getPage(doc,map);
	}
	public void getPage(Document doc, Map<String,String> map ){
		Iterator<Element> ite = doc.select("ul.yk-pages").select("li").iterator();
		while(ite.hasNext()) {
			Element ele = ite.next();
			String text = ele.text().trim();
			if(text.equals("上一页") || text.equals("1")) {
				
			}else if(text.equals("下一页")) {
				//map.put(text.trim(), ele.attr("href"));
				if(!ele.select("a").isEmpty()) {
					getAllPage("http://i.youku.com"+ele.select("a").get(0).attr("href"), map);
				}
			}else {
				Integer pageNum = null;
					try{
						pageNum = Integer.parseInt(text);
					}catch(Exception e) {
						
					}
					if(pageNum != null) {
						if( !ele.select("a").isEmpty()) {
							map.put(String.valueOf(pageNum),ele.select("a").get(0).attr("href"));
						}
					}
			}
		}
		return ;
	}
	
	public CloseableHttpClient getClient(){
		if(client == null){
			this.cookieStore = new BasicCookieStore();
			client = HttpClients.custom()
					.setDefaultCookieStore(cookieStore)
					.build();
		}
		return client;
	}
	public String doPost(String url){
		System.out.println(url);
		HttpUriRequest req = RequestBuilder.post(url)
		.build();
		CloseableHttpResponse resp = null;
		HttpEntity ent = null;
		try{
			resp = getClient().execute(req);
		 	ent = resp.getEntity();
			StatusLine status = resp.getStatusLine();
			return EntityUtils.toString(ent);
			//EntityUtils.consume(ent);
		}catch(Exception e){
			throw new RuntimeException(e);
		}finally{	
			try {
				resp.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
}
