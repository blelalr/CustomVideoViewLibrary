package com.jumplife.videoloader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import org.json.JSONObject;
import android.util.SparseArray;

/*
 * 2013/12/16
 * 	Remove video/Webm Format :
 *  Some device is hard to support this format.
 *  This format will spend too much loading time.
 */

public class YoutubeLoader {
	
	static final String YOUTUBE_VIDEO_INFORMATION_URL = "http://www.youtube.com/get_video_info?video_id=";
	private final static int QUALITY_SMALL = 0;
    private final static int QUALITY_MEDIUM = 1;
    private final static int QUALITY_LARGE = 2;
    private final static int QUALITY_HD720 = 3;
    
	public static SparseArray<String> Loader(boolean pFallback, String pYouTubeVideoId) {
		/*
		 * Reference : http://stackoverflow.com/questions/16503166/getting-a-youtube-download-url-from-existing-url
		 */
	    
		//String message = getMessageFromServer("GET", YOUTUBE_VIDEO_INFORMATION_URL + pYouTubeVideoId + "&eurl=http://jumplives.pixnet.net/blog/", null);
	    String message = getMessageFromServer("GET", YOUTUBE_VIDEO_INFORMATION_URL + pYouTubeVideoId + "&el=detailpage&ps=default&eurl=&gl=US&hl=en", null);
		
		if(message == null) 
			return null;
	    
	    HashMap<String, String> qualityStreamMap = new HashMap<String, String>();
	    HashMap<String, String> tmp = parseStr(message);
	    String currentType = "";
	    String currentQuality = "";
	    
	    if(tmp.containsKey("url_encoded_fmt_stream_map")) {
		    String[] streamMapGroup = ((String)tmp.get("url_encoded_fmt_stream_map")).split(",");
		    for(String tmpString : streamMapGroup) {
		      HashMap<String, String> streamMap = parseStr(tmpString);
		      String type = (String) streamMap.get("type");
		      String quality = (String) streamMap.get("quality");
		      
		      if(type != null && quality != null) {
			      String[] typeTmp = type.split(";");
			      if (typeTmp.length > 1)
			    	  type = typeTmp[0];
			      if(((type.equals("video/mp4")) || (type.equals("video/3gpp"))) && 
			    		  ((quality.equals("hd720")) || (quality.equals("large")) || (quality.equals("medium")) || (quality.equals("small")))) {
		    		  if(quality.equals(currentQuality) && (currentType.equals("video/mp4") || currentType.equals("video/3gpp"))) {
		    			  
		    		  } else {
		    			  currentType = type;
		    			  currentQuality = quality;
			    		  String streamLink = (String)streamMap.get("url") + "&signature=" + (String)streamMap.get("sig");
				    	  if(qualityStreamMap.containsKey(quality))
				    		  qualityStreamMap.remove(quality);
					      qualityStreamMap.put(quality, streamLink);
					      
		    		  }
			      }
		      }
		    }
	    }
	    
	    SparseArray<String> qualitySparseArray = new SparseArray<String>(4);
	    if(qualityStreamMap.containsKey("hd720"))
	    	qualitySparseArray.put(QUALITY_HD720, qualityStreamMap.get("hd720"));
	    if(qualityStreamMap.containsKey("large"))
	    	qualitySparseArray.put(QUALITY_LARGE, qualityStreamMap.get("large"));
	    if(qualityStreamMap.containsKey("medium"))
	    	qualitySparseArray.put(QUALITY_MEDIUM, qualityStreamMap.get("medium"));
	    if(qualityStreamMap.containsKey("small"))
	    	qualitySparseArray.put(QUALITY_SMALL, qualityStreamMap.get("small"));
	    
	    /*for(int i = 0; i < qualitySparseArray.size(); i++)
	    	Log.d("YoutubeLoader", "qualityList link : " + qualitySparseArray.get(i));*/
	    	
	    return qualitySparseArray;
	}
	
	private static HashMap<String, String> parseStr(String paramString) {
		HashMap<String, String> parseStrMap = new HashMap<String, String>();
		if(paramString != null) {		
		    try {
		    	String[] arrayStringSplitAnd = paramString.split("&");
		    	for (int i=0; i<arrayStringSplitAnd.length ; i++) {
		    		String[] arrayOfString2 = arrayStringSplitAnd[i].split("=");
		    		if (arrayOfString2.length > 1) {
		    			parseStrMap.put(URLDecoder.decode(arrayOfString2[0], "utf-8"), URLDecoder.decode(arrayOfString2[1], "utf-8"));
		    		//Log.d(null, URLDecoder.decode(arrayOfString2[0], "utf-8") + " : " + URLDecoder.decode(arrayOfString2[1], "utf-8"));
		    		}
		    	}
		    } catch (UnsupportedEncodingException localUnsupportedEncodingException) {
		      localUnsupportedEncodingException.printStackTrace();
		    }
		}
		
	    return parseStrMap;
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
