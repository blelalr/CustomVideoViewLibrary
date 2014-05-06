package com.jumplife.videoloader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
import android.util.SparseArray;

/*
 * 2013/12/11
 * 	Reference Link :
 * 	http://hi.baidu.com/littzen/item/5ec504e36c36c0c1baf37d59
 * 	http://vxml.56.com/json/MTAxMDA3MjA5/?src=out
 */

public class FiveSixLoader {
	
	//private final static int QUALITY_VGA = 0;
    private final static int QUALITY_QVGA = 1;
    //private final static int QUALITY_QQVGA = 2;
    //private final static int QUALITY_MQVGA = 3;
	
	public static SparseArray<String> Loader(String videoId) {
		
		SparseArray<String> VideoQuiltyLink = new SparseArray<String>(2);
		
		String vId = parseVId(videoId);
		String message = getMessageFromServer("GET", "http://vxml.56.com/json/" + vId + "/?src=out", null);
		
		if(message == null) 
			return null;
		
		try {
			JSONObject jsonObject = new JSONObject(message.toString());
			JSONArray jsonArray = jsonObject.getJSONObject("info").getJSONArray("rfiles");
			for(int i=0; i<jsonArray.length(); i++) {
				String type = jsonArray.getJSONObject(i).getString("type");
				String streamLink = jsonArray.getJSONObject(i).getString("url");
				
				if(type.equals("normal"))
					VideoQuiltyLink.put(QUALITY_QVGA, streamLink);
				/*if(type.equals("clear"))
					VideoQuiltyLink.put(QUALITY_QQVGA, streamLink);
				if(type.equals("super"))
					VideoQuiltyLink.put(QUALITY_MQVGA, streamLink);*/
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		
		return VideoQuiltyLink;
	}
	
	private static String parseVId(String videoId) {
		String vId = null;
		String[] splitLink;
		
		if(videoId.contains("v_")) {
			splitLink = videoId.split("v_");
			String[] tmpLink = splitLink[1].split("\\.");
			vId = tmpLink[0];
		} else if(videoId.contains("vid=")) {
			splitLink = videoId.split("vid=");
			vId = splitLink[1].substring(0, 11);
		} else if(videoId.contains("_vid-")) {
			splitLink = videoId.split("_vid-");
			vId = splitLink[1].substring(0, 11);
		} else if(videoId.contains("iframe")) {
			splitLink = videoId.split("iframe");
			vId = splitLink[1].substring(0, 11);
		} else if(videoId.contains("id-")) {
			splitLink = videoId.split("id-");
			String[] tmpLink = splitLink[1].split("\\.");
			vId = tmpLink[0];
		} else if(videoId.contains("cpm_")) {
			splitLink = videoId.split("cpm_");
			vId = splitLink[1].substring(0, 11);
		}
		
		return vId;
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
