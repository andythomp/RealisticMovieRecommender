/**
 * Group Identities:
 * Andrew Thompson, SN: 100745521
 * Roger Cheung, SN: 100741823
 * Chopel Tsering SN:100649290
 * 
 */
package com.paradopolis.realisticmovierecommender.managers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Environment;

import com.paradopolis.realisticmovierecommender.R;
import com.paradopolis.realisticmovierecommender.Facebook.Movie;
import com.paradopolis.realisticmovierecommender.Facebook.MovieMap;


/**
 * Static class that is responsible for storing all the information
 * about various movies that the system knows about.
 * @author Andrew Thompson
 *
 */
public class MovieManager{

	//JSON CONSTANTS
	
	//CONSTANTS
	protected static boolean initialized = false;
	private static String FILE_NAME = "movies.json";
	//VARIABLES
	private static MovieMap movies;
	
	/**
	 * Returns a movie from the list of movies when given the movies name.
	 * @param key - name of the movie
	 * @return - a movie if found, null if not
	 * @author Andrew Thompson
	 */
	public static Movie getMovie(String key){
		return movies.get(key);
	}
	
	
	
	
	
	
	/**
	 * Initializes the static movie configuration manager, loading the movies json file
	 * into memory. 
	 * @author Andrew Thompson
	 */
	public static boolean initialize(Context context){
		if (!preInitialize()){
			return false;
		}
		
		movies = new MovieMap();
		File dir = new File(context.getExternalFilesDir(null), context.getString(R.string.file_dir));
		File file = new File(dir, FILE_NAME);
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			StringBuilder sb = new StringBuilder();
			while (reader.ready()){
				sb.append(reader.readLine());
			}
			
			JSONArray tempArray = new JSONArray(sb.toString());
			for (int i = 0; i < tempArray.length(); i++){
				JSONObject tempObject = tempArray.getJSONObject(i);
				Movie tempMovie = new Movie(tempObject);
				movies.addMovie(tempMovie.getFacebookId(), tempMovie.getImdbId(), tempMovie);
			}
			reader.close();
		} 
		catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		System.out.println(MovieManager.class.getSimpleName() + ":Initialization complete.");
		System.out.println(MovieManager.class.getSimpleName() + ":Loaded " + movies.size() + " movies into memory.");
		setInitialized();
		
		
		
		return true;
	}

	public static boolean contains(String key){
		if (movies.containsKey(key)){
			return true;
		}
		else{
			return false;
		}
	}
	
	public static synchronized void addMovie(Movie movie){
		if (movie == null){
			return;
		}
		if (!movies.containsKey(movie.getFacebookId()))
			movies.addMovie(movie.getFacebookId(), movie.getImdbId(), movie);
	}

	public static synchronized boolean saveMovies(Context context){
		if (!isExternalWritable()){
			System.out.println(MovieManager.class.getSimpleName() + "Error: Unable to write to external storage.");
			return false;
		}
		try {
			int savedMovies = 0;
			File dir = new File(context.getExternalFilesDir(null), context.getString(R.string.file_dir));
			dir.mkdirs();
			System.out.println("WRITING TO: " + dir.getAbsolutePath());
			File file = new File(dir, FILE_NAME);
			file.createNewFile();
			
			//Build the json file in a string.
			StringBuilder sb = new StringBuilder();
			sb.append("[");
			for (int i = 0; i < movies.size(); i++){
				try {
					if (i != 0){
						sb.append(",");
					}
					sb.append(((Movie) movies.values().toArray()[i]).toJson().toString());
					savedMovies++;
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			sb.append("]");
			System.out.println("Writing to file: " + sb.toString());
			
			//Write the string to a file
			FileOutputStream outStream = new FileOutputStream(file);
			outStream.write(sb.toString().getBytes());
			outStream.close();
			System.out.println("Number of movies saved: " + savedMovies);
		} catch (IOException e1) {
			e1.printStackTrace();
			return false;
		}
		return true;
	}


	/**
	 * @return
	 * @author Andrew Thompson
	 */
	public static ArrayList<Movie> getMovieArray() {
		
		return new ArrayList<Movie>(movies.values());
	}
	
	/**
	 * We use an initialization flag to confirm that the configuration manager is configured.
	 * This may be necessary in concurrent programming, and can also be used to check that
	 * initialization was at least run.
	 */
	public static boolean isInitialized() {
		return initialized;
	}

	/**
	 * Internal function to set initialized to true. Once initialized, can not uninitialize.
	 */
	protected static void setInitialized() {
		initialized = true;
	}
	
	/**
	 * Checked to see if the Android Device is able to read from external storage.
	 * @return 	- True if External Reading is available
	 * 			- False if External Reading is not available
	 */
	public static boolean isExternalReadable(){
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
		    // We can read and write the media
		    return true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
		    // We can only read the media
		    return true;
		} else {
		    // Something else is wrong. It may be one of many other states, but all we need
		    // to know is we can neither read nor write
		   	return false;
		}
	}
	
	/**
	 * Checked to see if the Android Device is able to write to external storage.
	 * @return 	- True if External Writing is available
	 * 			- False if External Writing is not available
	 */
	public static boolean isExternalWritable(){
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
		    // We can read and write the media
		    return true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
		    // We can only read the media
		    return false;
		} else {
		    // Something else is wrong. It may be one of many other states, but all we need
		    // to know is we can neither read nor write
		   	return false;
		}
	}
	
	/**
	 * Recommended to be run at the start of the initialize function.
	 * @return
	 */
	protected static boolean preInitialize(){
		if (initialized){
			System.out.println(MovieManager.class.getSimpleName() + ": Already initialized.");
			return false;
		}
		
		if (!isExternalReadable()){
			System.out.println(MovieManager.class.getSimpleName() + "Error: Unable to read from external storage.");
			return false;
		}
		return true;
	}
}
