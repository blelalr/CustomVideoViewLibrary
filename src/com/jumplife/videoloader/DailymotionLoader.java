package com.jumplife.videoloader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

import android.util.Log;
import android.util.SparseArray;

public class DailymotionLoader {
	
	private final static int QUALITY_LD = 0;
    private final static int QUALITY_NORMAL = 1;
    private final static int QUALITY_HQ = 2;
    private final static int QUALITY_HD = 3;
	
	public static SparseArray<String> Loader(String videoId) {
		Log.d("DailyMotiondownloader", "parselinkUrl link is: " + videoId);
		SparseArray<String> qualitySparseArray = new SparseArray<String>(4);
	    
		String message = getMessageFromServer("GET", videoId, null);
		
		if(message == null)
			return qualitySparseArray;
	    
		Matcher mMatcher = Pattern.compile("var info = \\{(.+)\\}\\}").matcher(message);
	    String[] arrayOfString = null;
	    HashMap<String, String> qualityLinks = new HashMap<String, String>();
	    
	   if (mMatcher.find())
	    	arrayOfString = mMatcher.group().split(",");
	    
	    if(arrayOfString != null) {
		    for(String tmpString : arrayOfString) {
		    	Matcher mMatcherQuality = Pattern.compile("\"(.+)\":\"(.+)\"").matcher(tmpString);

		    	if(mMatcherQuality.find()) {
		    		if(mMatcherQuality.group(1).equalsIgnoreCase("stream_h264_hd_url")) {
			        	if(qualityLinks.containsKey(mMatcherQuality.group(1)))
				    		  qualityLinks.remove(mMatcherQuality.group(1));
			        	String qualityLink = mMatcherQuality.group(2).replace("\\/", "/");
					    qualityLinks.put(mMatcherQuality.group(1), qualityLink);
			        }			    		
			        if(mMatcherQuality.group(1).equalsIgnoreCase("stream_h264_hq_url")) {
			        	if(qualityLinks.containsKey(mMatcherQuality.group(1)))
				    		  qualityLinks.remove(mMatcherQuality.group(1));
			        	String qualityLink = mMatcherQuality.group(2).replace("\\/", "/");
					    qualityLinks.put(mMatcherQuality.group(1), qualityLink);
			        }
			        if(mMatcherQuality.group(1).equalsIgnoreCase("stream_h264_url")) {
			        	if(qualityLinks.containsKey(mMatcherQuality.group(1)))
				    		  qualityLinks.remove(mMatcherQuality.group(1));
			        	String qualityLink = mMatcherQuality.group(2).replace("\\/", "/");
					    qualityLinks.put(mMatcherQuality.group(1), qualityLink);
			        }
			        if(mMatcherQuality.group(1).equalsIgnoreCase("stream_h264_ld_url")) {
			        	if(qualityLinks.containsKey(mMatcherQuality.group(1)))
				    		  qualityLinks.remove(mMatcherQuality.group(1));
			        	String qualityLink = mMatcherQuality.group(2).replace("\\/", "/");
					    qualityLinks.put(mMatcherQuality.group(1), qualityLink);
			        }
		    	}
		    }
	    }

	    if(qualityLinks.containsKey("stream_h264_hd_url"))
	    	qualitySparseArray.put(QUALITY_HD, qualityLinks.get("stream_h264_hd_url"));
	    if(qualityLinks.containsKey("stream_h264_hq_url"))
	    	qualitySparseArray.put(QUALITY_HQ, qualityLinks.get("stream_h264_hq_url"));
	    if(qualityLinks.containsKey("stream_h264_url"))
	    	qualitySparseArray.put(QUALITY_NORMAL, qualityLinks.get("stream_h264_url"));
	    if(qualityLinks.containsKey("stream_h264_ld_url"))
	    	qualitySparseArray.put(QUALITY_LD, qualityLinks.get("stream_h264_ld_url"));
	    
	    for(int j = 0; j < qualitySparseArray.size(); j++)
	    	Log.d("DailyMotiondownloader", "qualityList link : " + qualitySparseArray.get(j));
	    
	    return qualitySparseArray;
	}
	
	public static String getMessageFromServer(String requestMethod, String apiPath, JSONObject json) {
		URL url;
		try {
			url = new URL(apiPath);
				
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod(requestMethod);
			connection.connect();
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			StringBuilder lines = new StringBuilder();
			String tempStr;
			
			while ((tempStr = reader.readLine()) != null) {
	            lines = lines.append(tempStr);
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
