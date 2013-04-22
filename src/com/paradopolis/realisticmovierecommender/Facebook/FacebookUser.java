/**
 * 
 */
package com.paradopolis.realisticmovierecommender.Facebook;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

/**
 * Represents a Facebook User's personal information.
 * @author Andrew Thompson
 *
 */
public class FacebookUser implements Comparable<FacebookUser>{


	private String id;
	private String name;
	private ArrayList<Movie> movies;
	private double pearsonCoefficient;
	private Bitmap picture;
	private String pictureURL;
	
	public FacebookUser(String id, String name){
		this.name = name;
		this.id = id;
		movies = new ArrayList<Movie>();
	}
	
	public void addMovie(Movie movie){
		movies.add(movie);
	}
	
	public ArrayList<Movie> movies(){
		return movies;
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public HashMap<String, Float> getGenreMap(){
		 HashMap<String, Float> genreMap = new HashMap<String, Float>();
		 for (int i = 0; i < movies.size(); i++){
			 Movie movie = movies.get(i);
			 for (int k = 0; k < movie.genres().size(); k++){
				 if (genreMap.containsKey(movie.genres().get(k))){
					 genreMap.put(movie.genres().get(k),genreMap.get(movie.genres().get(k)) + 1f);
				 }
				 else{
					 genreMap.put(movie.genres().get(k),1f);
				 }
			 }
		 }
		 return genreMap;
	}

	public double getPearsonCoefficient() {
		return pearsonCoefficient;
	}

	public void setPearsonCoefficient(double pearsonCoefficient) {
		this.pearsonCoefficient = pearsonCoefficient;
	}
	
	public String toString(){
		return "Name: " + name + "\n" + 
			   "Pearson Co.: " + pearsonCoefficient + "\n";
	}

	@Override
	public int compareTo(FacebookUser another) {
		if (name == null || another == null){
			return 0;
		}
		if (another.getName() == null)
			return 0;
		return name.compareTo(((FacebookUser) another).getName());
		
	}

	public Bitmap getPicture() {
		return picture;
	}

	public void setPicture(Bitmap picture) {
		this.picture = picture;
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

	public String getPictureURL() {
		return pictureURL;
	}

	public void setPictureURL(String pictureURL) {
		this.pictureURL = pictureURL;
	} 
	
	
}
