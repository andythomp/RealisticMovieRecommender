/**
 * 
 */
package com.paradopolis.realisticmovierecommender.Facebook;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

/**
 * 
 * This is a movie object. It has all the information about a movie.
 * @author Andrew Thompson
 *
 */
public class Movie {

	/*
	 * OMDB CONSTANTS
	 */
	public static String TITLE = "Title";
	public static String YEAR = "Year";
	public static String PG_RATED = "Rated";
	public static String RUNTIME = "Runtime";
	public static String GENRES = "Genre";
	public static String POSTER = "Poster";
	public static String PLOT = "Plot";
	public static String IMDB_RATING = "imdbRating";
	public static String IMDB_VOTES = "imdbVotes";
	public static String IMDB_ID = "imdbID";
	public static String TYPE = "Type";
	public static String MOVIE_TYPE = "movie";
	
	/*
	 * FACEBOOK CONSTANTS
	 */

	public static String FACEBOOK_ID = "facebookID";
	
	/*
	 * VARIABLES
	 */
	private String 	title,
					year, 
					pgRating, 
					runtime, 
					plot, 
					imageURL, 
					rating, 
					imdbVotes,
					imdbID,
					facebookID;
	
	private ArrayList<String> genres;
	private ArrayList<FacebookUser> recommenders;
	private Integer likes;
	private Bitmap bitmap;
	
	public Movie(){
		genres = new ArrayList<String>();
		recommenders = new ArrayList<FacebookUser>();
		title = "Unknown";
		year = "0000";
		pgRating = "Unknown";
		runtime = "Unknown";
		plot = "Unknown";
		rating = "0.0";
		imdbVotes = "0";
	}
	
	/**
	 * Constructor that builds a movie from the JSON Library. Only pass in a movie object from the MovieManager
	 * after it loads the movie in from JSON File. 
	 * @param tempObject
	 * @author Andrew Thompson
	 */
	public Movie(JSONObject tempObject) {
		this();
		try {setTitle(tempObject.getString(TITLE));}
		catch (JSONException e) {e.printStackTrace();}
		
		try {setYear(tempObject.getString(YEAR));}
		catch (JSONException e) {e.printStackTrace();}
		
		try {setPgRating(tempObject.getString(PG_RATED));}
		catch (JSONException e) {e.printStackTrace();}
		
		try {setRuntime(tempObject.getString(RUNTIME));}
		catch (JSONException e) {e.printStackTrace();}
		
		try {
			String genreString = tempObject.getString(GENRES);
			setGenres(genreString);
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		
		try {setDescription(tempObject.getString(PLOT));}
		catch (JSONException e) {e.printStackTrace();}

		try {setIconUrl(tempObject.getString(POSTER));}
		catch (JSONException e) {e.printStackTrace();}
		
		try {setRating(tempObject.getString(IMDB_RATING));}
		catch (JSONException e) {e.printStackTrace();}
		
		try {setImdbVotes(tempObject.getString(IMDB_VOTES));}
		catch (JSONException e) {e.printStackTrace();}

		try {setImdbId(tempObject.getString(IMDB_ID));}
		catch (JSONException e) {e.printStackTrace();}
		
		try {setFacebookID(tempObject.getString(FACEBOOK_ID));}
		catch (JSONException e) {e.printStackTrace();}
	}


	/**
	 * Returns a URI constructed using a String representing a URL or URI. Compatible with
	 * android.
	 * @return - Uri constructed from the String URL
	 * @author Andrew Thompson
	 */
	public Uri getIconUri(){
		Uri uri = Uri.parse(imageURL);
		return uri;
	}
	
	
	public Bitmap getImageBitmap(String url) {
		if (url == null || url == ""){
			return null;
		}
        Bitmap bm = null;
        try {
            URL aURL = new URL(url);
            URLConnection conn = aURL.openConnection();
            conn.connect();
            InputStream is = conn.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            bm = BitmapFactory.decodeStream(bis);
            bis.close();
            is.close();
       } catch (Exception e){
    	   Log.e("Fragment", "Error getting bitmap", e);
       }
       return bm;
    } 
	
	/*
	 * GETTERS / SETTERS
	 */
	public String getImdbId() {
		if (imdbID == null)
			return null;
		else
			return  imdbID.replaceAll( "[^\\d]", "" );
	}
	
	public void setImdbId(String id) {
		if (id == null)
			return;
		else
			imdbID = id.replaceAll( "[^\\d]", "" );
	}
	
	public String getFacebookId() {return facebookID;}
	public void setFacebookID(String id) {this.facebookID = id;}

	public String getTitle() {return title;}
	public void setTitle(String name) {this.title = name;}

	
	public void setGenres(String genreString){
		genres = new ArrayList<String>();
		if (genreString == null){
			return;
		}
		if (genreString.isEmpty()){
			return;
		}
		String[] strings = genreString.split("[//\\., ]+");
		for (int i = 0; i < strings.length; i++){
			strings[i].trim();
			if (!strings[i].isEmpty() && !strings[i].equalsIgnoreCase(" ")){
				genres.add(strings[i]);
			}
		}
	}
	public ArrayList<String> genres() {return genres;}

	public String getDescription() {return plot;}
	public void setDescription(String description) {this.plot = description;}

	public String getIconUrl() {return imageURL;}
	public void setIconUrl(String iconUrl) {this.imageURL = iconUrl;}
	
	public Integer getLikes() {	return likes;}
	public void setLikes(Integer likes) {this.likes = likes;}

	public void setBitmap(Bitmap bitmap) {this.bitmap = bitmap;}
	public Bitmap getBitmap(){return bitmap;}
	
	public String getYear() {return year;}
	public void setYear(String year) {this.year = year;}

	public String getPgRating() {return pgRating;}
	public void setPgRating(String pgRating) {this.pgRating = pgRating;}

	public String getRuntime() {return runtime;}
	public void setRuntime(String runtime) {this.runtime = runtime;}

	public String getRating() {return rating;}
	public void setRating(String rating) {this.rating = rating;}


	public String getImdbVotes() {return imdbVotes;}
	public void setImdbVotes(String imdbVotes) {this.imdbVotes = imdbVotes;}

	
	public JSONObject toJson() throws JSONException{
		JSONObject temp = new JSONObject();
		String genreString = "";
		for (int i = 0; i < genres.size(); i++){
			if (i != 0){
				genreString = genreString + ",";
			}
			genreString = genreString + genres.get(i);
		}
		temp.put(TITLE, title);
		temp.put(YEAR, year);
		temp.put(PG_RATED, pgRating);
		temp.put(RUNTIME, runtime);
		temp.put(GENRES, genreString);
		temp.put(PLOT, plot);
		temp.put(POSTER, imageURL);
		temp.put(IMDB_RATING, rating);
		temp.put(IMDB_VOTES, imdbVotes);
		temp.put(IMDB_ID, imdbID);
		temp.put(FACEBOOK_ID, facebookID);
		
		
		return temp;
	}
	
	public String toString(){
		return "Title: " + title + "\n" +
				"Year: " + year + "\n" + 
				"Rated: " + pgRating + "\n" + 
				"Runtime: " + runtime + "\n" + 
				"Plot: " + plot + "\n" + 
				"IMDB Rating: " + rating + "\n" + 
				"IMDB Votes: " + imdbVotes + "\n" + 
				"IMDB ID: " + imdbID + "\n" + 
				"Facebook ID: " + facebookID + "\n" +
				"Genres: " + genres;
	}

	public ArrayList<FacebookUser> getRecommenders() {
		return recommenders;
	}

	public void addRecommender(FacebookUser recommender) {
		this.recommenders.add(recommender);
	}
	
}
