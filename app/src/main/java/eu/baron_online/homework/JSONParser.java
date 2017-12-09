package eu.baron_online.homework;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONException;
import org.json.JSONObject;

public class JSONParser {
	
	public static JSONObject makeHttpRequest(String url, String type, HashMap<String, String> params) {
		String paramString = "", fullUrl = "";
		
		if(type.equals("GET")) {
			Iterator<Entry<String, String>> it = params.entrySet().iterator();
			while(it.hasNext()) {
				Map.Entry pair = (Map.Entry) it.next();
				paramString += pair.getKey() + "=" + pair.getValue() + "&";
			}
			if(paramString.length() > 0) {
				paramString = paramString.substring(0, paramString.length() - 1);
				fullUrl = url + "?" + paramString;
			}
			
			String result = sendRequestGET(url, paramString);

			try {
				return new JSONObject(result);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} else if(type.equals("POST")) {
			String result = sendRequestPOST(url, params);
		}
		
		return null;
	}
	
	private static String sendRequestGET(String urlString, String params) {
		try {
			URL url = new URL(urlString + "?" + params);
			
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			con.setDoOutput(true);
			
			InputStream in = con.getInputStream();

			String response = convertStreamToString(in);
			return response;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return "";
	}
	
	private static String sendRequestPOST(String urlString, HashMap<String, String> params) {
		try {
			String paramString = "";
			URL url = new URL(urlString);
			
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("POST");
			con.setDoOutput(true);
			
			
			Iterator<Entry<String, String>> it = params.entrySet().iterator();
			while(it.hasNext()) {
				Map.Entry pair = (Map.Entry) it.next();
				paramString += pair.getKey() + "=" + pair.getValue() + "&";
			}
			
			if(paramString.length() > 0) {
				paramString = paramString.substring(0, paramString.length() - 1);
			}
			
			
			InputStream in = con.getInputStream();
			
			String response = convertStreamToString(in);
			return response;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return "";
	}
	
	private static String convertStreamToString(java.io.InputStream is) {
	    java.util.Scanner s = new java.util.Scanner(is);
	    s.useDelimiter("\\A");
	    
	    String result = s.hasNext() ? s.next() : "";
	    s.close();
	    return result;
	}
}
