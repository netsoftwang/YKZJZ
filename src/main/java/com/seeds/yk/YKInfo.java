package com.seeds.yk;

import java.io.IOException;
import java.util.ArrayList;
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
import org.junit.Test;

import sun.security.action.GetIntegerAction;



public class YKInfo {
	
	public CloseableHttpClient  client;
	public BasicCookieStore cookieStore;
	
	@Test
	public void doDownLoad(){
		String url ="http://103.43.209.209/6975D468B92347A0724DD31A4/03000B01005B6C2C8B12040052CDD5BE3A19A4-F3DD-4D27-B69C-A02D83871135.mp4.ts?ccode=0502&duration=205&expire=18000&psid=9ee559933ddcf1f992c8e3656c5e7ba5&sp=&ups_client_netip=df1500e2&ups_ts=1537449786&ups_userid=&utid=ya%2FFEV1RfhkCAd8VHd%2F32sIw&vid=XMzc3MjA4ODUzNg%3D%3D&vkey=Bcfd1f046658e23f5e8fb56cdb81e01f8&ts_start=17.9&ts_end=29.9&ts_seg_no=3&ts_keyframe=1";
		url ="http://vali.cp31.ott.cibntv.net/youku/6773C3C042C4571BB20212AC2/03000B01005B90902028505003E880726E9BE0-5CB3-43A1-9F46-ED6AE628E984.mp4?sid=053744740561916195408_00_A29b83b27b6b9684a37dcb6683db280c1&sign=49faa51d54570b28b318d7cb513aec88&ctype=50&hd=2";
		url ="http://i.youku.com/i/UMzQ3MzA1MzM2/videos?spm=a2hzp.8244740.0.0";
		String html = doPost(url);
		getItermList(html);
		
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
	
	@Test
	public void testJsoup() {
		
	}
	public List<Map<String,String>> getItermList(String html) {
		Document doc = Jsoup.parse(html);
		List<Map<String,String>> mapList = new ArrayList<>();
		Iterator<Element> ite = doc.select("div.videos-list").select("div.items").select("div.v-link").iterator();
		while(ite.hasNext()) {
			Element ele = ite.next();
			System.out.println("===========================================================");
			//System.out.println(ele.toString());
			ele = ele.select("a").first();
			System.out.println(ele.attr("href"));
			System.out.println(ele.attr("title"));
		}
        return mapList;
	}
	
	public String doPost(String url){
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
