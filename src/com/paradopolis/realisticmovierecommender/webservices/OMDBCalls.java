package com.paradopolis.realisticmovierecommender.webservices;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Contains all the calls related to communicating with the OMDB Database.
 * @author Paradopolis
 *
 */
public class OMDBCalls {
	
	public static JSONObject getMovieOMDB(String name){
		String api = "http://www.omdbapi.com/";
		String titleKey = "?t=";
		name = name.replace(' ', '+');
		final String request = api + titleKey + name;
		//Log.i("Client", request);
		try {
			
			URL link = new URL(request);
			BufferedReader reader = new BufferedReader(new InputStreamReader(link.openStream()));
			String text = "";
			while (reader.ready()){
				text = text + reader.readLine();
			}
			JSONObject object;
			try {
				object = new JSONObject(text);
				return object;
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			reader.close();
		} catch (MalformedURLException e1) {;
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e){
			e.printStackTrace();
		}
		return null;
	}
	

}
