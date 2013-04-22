/**
 * 
 */
package com.paradopolis.realisticmovierecommender.webservices;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.paradopolis.realisticmovierecommender.MainActivity;
import com.paradopolis.realisticmovierecommender.Facebook.Movie;
import com.paradopolis.realisticmovierecommender.managers.WebServiceManager;

/**
 * Contains all the calls related to communicating with the Rotten Tomatoes data base.
 * @author Andrew Thompson
 *
 */
public class RottenTomatoesCalls {

	
	public static final String ROTTEN_KEY = "mev5zgajg4kprzkjxafeszcb";
	
	/**
	 * Creates and executes a movie alias request on Rotten Tomatoes API. Takes the ID of the movie to 
	 * run. All requests are executed on the WebServiceManager's threadpool.
	 * @param id - ID of the movie
	 * @author Andrew Thompson
	 */
	public static void getMovieAlias(String id){
		String request = "http://api.rottentomatoes.com/api/public/v1.0/movie_alias.json?id=%s&type=imdb&apikey=%s";
		final String formattedRequest = String.format(request, id, ROTTEN_KEY);
		
		
		WebServiceManager.handleWebserviceRequest(new Runnable(){

			@Override
			public void run() {
				//Log.i("Client", request);
				try {
					
					URL link = new URL(formattedRequest);
					BufferedReader reader = new BufferedReader(new InputStreamReader(link.openStream()));
					String text = "";
					while (reader.ready()){
						text = text + reader.readLine();
					}
					//Log.i("Client", text);
					JSONObject object;
					try {
						object = new JSONObject(text);
						getSimilarMovies(object.getString("id"), 5);
					} catch (JSONException e) {
						//postMessage("JSON Exception");
						e.printStackTrace();
					}
					
					reader.close();
				} catch (MalformedURLException e1) {;
					e1.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
		});

	}
	
	/**
	 * Gets similiar movies to a given movie id. The similiar movies are obtained using rotten tomatoes.
	 * This function is used to create an initial recommendation based on movies you already like, through
	 * the Rotten Tomatoes API.
	 * @param id - ID of the movie
	 * @param limit - Limit of movies to be added
	 * @author Andrew Thompson
	 */
	public static void getSimilarMovies(String id, int limit){
		String request = "http://api.rottentomatoes.com/api/public/v1.0/movies/%s/similar.json?limit=%s&apikey=%s";
		final String formattedRequest = String.format(request, id, Integer.toString(limit), ROTTEN_KEY);
		
		
		WebServiceManager.handleWebserviceRequest(new Runnable(){

			@Override
			public void run() {
				//Log.i("Client", request);
				try {
					
					URL link = new URL(formattedRequest);
					BufferedReader reader = new BufferedReader(new InputStreamReader(link.openStream()));
					String text = "";
					while (reader.ready()){
						text = text + reader.readLine();
					}
					//Log.i("Client", text);
					JSONObject object;
					ArrayList<Movie> tempMovies = new ArrayList<Movie>();
					try {
						object = new JSONObject(text);
						JSONArray array = object.getJSONArray("movies");
						for (int i = 0; i < array.length(); i++){
							JSONObject tempMovie = array.getJSONObject(i);
							Movie temp = new Movie();
							temp.setTitle(tempMovie.getString("title"));
							temp.setYear(tempMovie.getString("year"));
							temp.setIconUrl(tempMovie.getJSONObject("posters").getString("thumbnail"));
							try{
								temp.setImdbId("tt" + tempMovie.getJSONObject("alternate_ids").getString("imdb"));
							}
							catch(Exception e){
								Log.i("Recommenders", "Didn't get an imdb id back.");
							}
							temp.setRating(tempMovie.getJSONObject("ratings").getString("critics_score"));
							tempMovies.add(temp);
						}
						
						MainActivity.getMainActivity().processRecommendations(tempMovies);
					} catch (JSONException e) {
						//postMessage("JSON Exception");
						e.printStackTrace();
					}
					
					reader.close();
				} catch (MalformedURLException e1) {;
					e1.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
		});
		

	}
	
}
