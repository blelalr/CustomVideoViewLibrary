package com.jumplife.videoloader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
import android.util.SparseArray;

/*
 * 2014/3/4
 * 	input :
 * 	http://www.bilibili.tv/video/av993434/index_1.html
 *  loader link : 
 * 	http://www.bilibili.tv/m/html5?aid=920966&page=3
 */

public class BiliLoader {
	
	private final static int QUALITY_QVGA = 1;
	
	public static SparseArray<String> Loader(String videoId) {
		
		SparseArray<String> VideoQuiltyLink = new SparseArray<String>(2);
		
		String vId = parseVId(videoId);
		String page = parsePage(videoId);
		
		String message = getMessageFromServer("GET", "http://www.bilibili.tv/m/html5?aid=" + vId + "&page=" + page, null);
		
		if(message == null) 
			return null;
		
		try {
			JSONObject jsonObject = new JSONObject(message.toString());
			String streamLink = jsonObject.getString("src");
			VideoQuiltyLink.put(QUALITY_QVGA, streamLink);
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		
		return VideoQuiltyLink;
	}
	
	private static String parseVId(String videoId) {
		String vId = null;
		String[] splitLink;
		
		if(videoId.contains("av")) {
			splitLink = videoId.split("av");
			String[] tmpLink = splitLink[1].split("/");
			vId = tmpLink[0];
		}
		
		return vId;
	}
	
	private static String parsePage(String videoId) {
		String page = null;
		String[] splitLink;
		
		if(videoId.contains("index_")) {
			splitLink = videoId.split("index_");
			String[] tmpLink = splitLink[1].split("\\.");
			page = tmpLink[0];
		}
		
		return page;
	}
	
	public static String getMessageFromServer(String requestMethod, String apiPath, JSONObject json) {
		URL url;
		try {
			Log.d(null, "apiPath : " + apiPath);
			url = new URL(apiPath);
				
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod(requestMethod);
			connection.connect();
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			StringBuilder lines = new StringBuilder();
			String tempStr;
			
			while ((tempStr = reader.readLine()) != null) {
	            lines = lines.append(tempStr);
	    		
	    		Log.d(null, tempStr);
	        }
			
			reader.close();
			connection.disconnect();
			
			return lines.toString();
		} 
		catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		} 
		catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}
