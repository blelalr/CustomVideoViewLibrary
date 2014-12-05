package com.jumplife.videoloader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.util.Log;
import android.util.SparseArray;

/*
 * 2014/02/27
 * 	
 */

public class QQProxyLoader {
	
	private final static int QUALITY_NORMAL = 1;
	
	public static SparseArray<String> Loader(String videoId) {
		
		SparseArray<String> VideoQuiltyLink = new SparseArray<String>(2);
		
		String vId = parseVId(videoId);
		String message = getMessageFromServer("GET", "http://vv.video.qq.com/geturl?vid=" + vId, null);
		
		if(message == null) 
			return null;
		
		StringReader sr = new StringReader(message);
		InputSource is = new InputSource(sr);
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		Document document = null;
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			document = builder.parse(is);
		} catch (SAXException e) {
			document = null;
			e.printStackTrace();
		} catch (IOException e) {
			document = null;
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			document = null;
			e.printStackTrace();
		}
		if (document != null) {
			NodeList nlId = document.getElementsByTagName("url");  
	        Element eleId = (Element)nlId.item(0);  
	        String videoLink = eleId.getChildNodes().item(0).getNodeValue();
	        Log.d(null, videoLink);
			VideoQuiltyLink.put(QUALITY_NORMAL, videoLink);
		}

		return VideoQuiltyLink;
	}
	
	private static String parseVId(String videoId) {
		String vId = null;
		String[] splitLink;
		
		if(videoId.contains("vid=")) {
			splitLink = videoId.split("vid=");
			String[] tmpLink = splitLink[1].split("&");
			vId = tmpLink[0];
		}
		
		return vId;
	}
	
	public static String getMessageFromServer(String requestMethod, String apiPath, JSONObject json) {
		URL url;
		try {
			Log.d(null, "apiPath : " + apiPath);
			url = new URL(apiPath);
			
			System.getProperties().setProperty("http.proxyHost", "proxy.uku.im");
			System.getProperties().setProperty("http.proxyPort", "80");
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
