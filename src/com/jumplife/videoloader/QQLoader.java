package com.jumplife.videoloader;

import android.util.SparseArray;

/*
 * 2013/12/11
 * 	Reference Link :
 * 	https://github.com/kerwin/my.video.scripts/blob/master/youku-lixian/qq.py
 * 	http://v.qq.com/iframe/player.html?vid=l0013f7dcj4&tiny=0&auto=0
 */

public class QQLoader {
	
	private final static int QUALITY_NORMAL = 1;
	
	public static SparseArray<String> Loader(String videoId) {
		
		SparseArray<String> VideoQuiltyLink = new SparseArray<String>(2);
		
		String vId = parseVId(videoId);
		String streamLink = "http://vsrc.store.qq.com/" + vId + ".flv";
		VideoQuiltyLink.put(QUALITY_NORMAL, streamLink);
		
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
}
