package com.turfgame.json;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.turfgame.widget.CharStats;
import com.turfgame.widget.TurfWidget;

public class ParseJSON {
	private static final String URL_ADRESS = "http://api.turfgame.com/v4/users";
	private static final String DEBUG_TAG = "ParseJSON";

	public static CharStats parseJSON(String email) {
		if (TurfWidget.DEBUG) {
			Log.d(TurfWidget.DEBUG_STRING, "parseXML");
			// Simulating somewhat slow network.
			try {
				Log.d(TurfWidget.DEBUG_STRING, "sleeping..");
				Thread.sleep(3000);
				Log.d(TurfWidget.DEBUG_STRING, "..done");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		StringBuilder stringBuilder = new StringBuilder();
		HttpClient httpClient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(URL_ADRESS);
		JSONObject jsonPostObject = new JSONObject();

		try {
			jsonPostObject.accumulate("email", email);
			JSONArray jsonPostArray = new JSONArray();
			jsonPostArray.put(jsonPostObject);
			StringEntity se = new StringEntity(jsonPostArray.toString());
			httpPost.setEntity(se);
			httpPost.setHeader("Content-type", "application/json");
			HttpResponse response = httpClient.execute(httpPost);
			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();
			
			if (statusCode == 200) {
				HttpEntity entity = response.getEntity();
				InputStream inputStream = entity.getContent();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(inputStream));
				String line;
				while ((line = reader.readLine()) != null) {
					stringBuilder.append(line);
				}
				inputStream.close();
			} else {
				System.out.println(statusLine.toString());
			}

			JSONArray jsonArray = new JSONArray(stringBuilder.toString());

			//get first and only user from array
			JSONObject jsonObject = jsonArray.getJSONObject(0);

			CharStats charStats = new CharStats();
			CharStats.setPoints(Integer.parseInt(jsonObject.getString("points")));
			CharStats.setHour(Integer.parseInt(jsonObject.getString("pointsPerHour")));
			CharStats.setPlace(Integer.parseInt(jsonObject.getString("place")));
			// zones is an array, needs to be counted
			JSONArray zoneArray = jsonObject.getJSONArray("zones");
			CharStats.setZones(zoneArray.length());
			CharStats.setAccount(true);

			if (TurfWidget.DEBUG) {
				Log.d(DEBUG_TAG, "Success");
			}

			TurfWidget.setError(null);
			return charStats;
		} 
		 catch (ClientProtocolException e) {
			TurfWidget.setError("server");
			Log.e(DEBUG_TAG, "server", e);
		} catch (IOException e) {		
			TurfWidget.setError("internet");
			e.printStackTrace();
			Log.e(DEBUG_TAG, "internet", e);
		} catch (JSONException e) {
			TurfWidget.setError("json");
			Log.e(DEBUG_TAG, "JSONException", e);
		} catch (Exception e) {
			Log.e(DEBUG_TAG, "Error", e);
		}

		return new CharStats();
	}
}