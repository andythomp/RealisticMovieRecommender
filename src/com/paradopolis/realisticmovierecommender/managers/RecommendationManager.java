package com.paradopolis.realisticmovierecommender.managers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import android.content.Context;
import android.os.Environment;

import com.paradopolis.realisticmovierecommender.R;
import com.paradopolis.realisticmovierecommender.Facebook.FacebookManager;
import com.paradopolis.realisticmovierecommender.Facebook.Movie;


/**
 * Temporary class really. This is just a quick class thrown together to allow
 * the application to save recommendations to .csv files, so that they can be imported
 * into a spreadsheet program and have their data properly analyzed.
 * 
 * If this application is up for distribution this class can be removed.
 * @author Paradopolis
 *
 */
public class RecommendationManager {
	//JSON CONSTANTS
	
		//CONSTANTS
		protected static boolean initialized = false;
	
		
		/**
		 * Initializes the static movie configuration manager, loading the movies json file
		 * into memory. 
		 * @author Andrew Thompson
		 */
		public static boolean initialize(Context context){
			if (!preInitialize()){
				return false;
			}
			
			
			setInitialized();
			return true;
		}

		
	
		public static synchronized boolean saveMovies(Context context, ArrayList<Movie> movies){
			if (!isExternalWritable()){
				System.out.println(MovieManager.class.getSimpleName() + "Error: Unable to write to external storage.");
				return false;
			}
			try {
				int savedMovies = 0;
				File dir = new File(context.getExternalFilesDir(null), context.getString(R.string.file_dir));
				dir.mkdirs();
				System.out.println("WRITING TO: " + dir.getAbsolutePath());
				File file = new File(dir, FacebookManager.getFacebookManager().getUser().getId() + "_recommendation.csv");
				file.createNewFile();
				

				//Build the string
				String outputString = "Movie Name, Rating, Flagged, Haven't Seen it, Did not like it, Liked it\n";
				for (int i = 0; i < movies.size(); i++){
					Movie movie = MovieManager.getMovie(movies.get(i).getImdbId());
					if (movie == null){
						movie = movies.get(i);
			        }
					outputString = outputString + movie.getTitle() + "," + movie.getRating() + ",";
					boolean flagged = false;
					for (int j = 0; j < movie.getRecommenders().size(); j++){
			        	if (movie.getRecommenders().get(j).getPearsonCoefficient() > .5d){
			                flagged = true;
			        	}
					}
					outputString = outputString + flagged + ",,,\n";
				}
				
				
				
				System.out.println("Writing to file: " + outputString);
				
				//Write the string to a file
				FileOutputStream outStream = new FileOutputStream(file);
				outStream.write(outputString.getBytes());
				outStream.close();
				System.out.println("Number of movies saved: " + savedMovies);
			} catch (IOException e1) {
				e1.printStackTrace();
				return false;
			}
			return true;
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
