package com.paradopolis.realisticmovierecommender.Facebook;

import java.util.ArrayList;
import java.util.HashMap;

import android.util.Log;

/**
 * Movie Maps are used as a wrapper around two seperate hashmaps.
 * This way movies can be retrieved based on either one of two keys, but are still organized
 * into their respective maps.
 * @author Paradopolis
 *
 */
public class MovieMap {
	
	public static final String FACEBOOK = "facebook";
	public static final String IMDB = "imdb";
	
	private HashMap<String, Movie> imdbMap;
	private HashMap<String, Movie> facebookMap;
	
	public MovieMap(){
		imdbMap = new HashMap<String, Movie>();
		facebookMap = new HashMap<String, Movie>();
	}
	
	/**
	 * Adds a movie to the movie map.
	 * @param imdbKey - The IMDB Key of the movie being added to the movie map
	 * @param facebookKey - The facebook key of the movie being added to the movie map
	 * @param movie - Movie to be added
	 * @return True is the movie is added successfuly, false if not.
	 */
	public boolean addMovie(String imdbKey, String facebookKey, Movie movie){
		//Check that hte imdb key is valid
		try{
			if (imdbKey.isEmpty()){
				Log.e(this.getClass().getSimpleName(), "IMDB Key empty.");
				return false;
			}
			//Check that the facebook key is valid
			if (facebookKey.isEmpty()){
				Log.e(this.getClass().getSimpleName(), "Facebook Key empty.");
				return false;
			}
			if (facebookKey.equals(imdbKey)){
				Log.e(this.getClass().getSimpleName(), "Attempted to add a movie with two identical keys");
				return false;
			}
		}
		catch(Exception e){
			Log.e(this.getClass().getSimpleName(), "Facebook Key or IMDB Key was null.");
			return false;
		}
		if (imdbMap.containsKey(imdbKey)){
			//Log.i("Fragment", "Movie alread in IMDB Map: " + movie.getTitle());
			return false;
		}
		if (facebookMap.containsKey(facebookKey)){
			//Log.i("Fragment", "Movie alread in FACEBOOK Map: " + movie.getTitle());
			return false;
		}
		imdbMap.put(imdbKey, movie);
		facebookMap.put(facebookKey, movie);
		return true;
	}
	
	public int size(){
		return Math.max(facebookMap.size(), imdbMap.size());
	}
	
	public boolean containsKey(String key){
		if (imdbMap.containsKey(key) || facebookMap.containsKey(key)){
			return true;
		}
		else{
			return false;
		}
	}
	
	/**
	 * Given a key, returns a movie if found, otherwise returns null
	 * @param key - Key of the movie to be found. 
	 * @return - The movie if found, null if it is not found.
	 */
	public Movie get(String key){
		if (imdbMap.containsKey(key)){
			return imdbMap.get(key);
		}
		else if (facebookMap.containsKey(key)){
			return facebookMap.get(key);
		}
		else{
			return null;
		}
	}

	public ArrayList<Movie> values() {
		return new ArrayList<Movie>(facebookMap.values());
	}

}
