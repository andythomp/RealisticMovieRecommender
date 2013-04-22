package com.paradopolis.realisticmovierecommender.managers;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.paradopolis.realisticmovierecommender.R;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Environment;

/**
 * Settings manager is responsible for organizing all the settings from the properties file.
 * Other aspects of the application can request settings from the settings manager.
 * @author Paradopolis
 *
 */
public class SettingsManager{
		//CONSTANTS
		private static String FILE_NAME = "settings.prop";
		public static final String MIN_PEARSON = "min_pearson";
		public static final String MIN_RATING = "min_rating";
		public static final String USE_SOCIAL = "use_social";
		
		//Configuration Variables
		private static boolean initialized = false;
		private static Properties settings;
		
		
		
		/**
		 * Initializes the Settings Manager with the current application context.
		 * @param context - Application Context (Necessary for file io on android).
		 * @author Andrew Thompson
		 */
		public static boolean initialize(Context context){
			if (!preInitialize()){
				return false;
			}
			
			File dir = new File(context.getExternalFilesDir(null), context.getString(R.string.file_dir));
			File file = new File(dir, FILE_NAME);
		    settings = new Properties();
			//Try and read in settings from a previously saved file.
			try {
				settings.load(new FileReader(file));
				if (settings != null){
					//If we managed to read in settings from a previously saved file, then exit successfully
					setInitialized();
					return initialized;
				}
			} 
			catch (IOException e) {
				//Do nothing, if init fails here we need to go forward anyway.
			} 
			
			
			
			Resources resources = context.getResources();
			AssetManager assetManager = resources.getAssets();
			// Read from the /assets directory
			try {
			    InputStream inputStream = assetManager.open(FILE_NAME);
			    settings.load(inputStream);
			} catch (IOException e) {
			    e.printStackTrace();
			    return false;
			}
			setInitialized();
			return initialized;
		
		}
		
		/**
		 * Returns the settings object to the user.
		 * @author Andrew Thompson
		 */
		public static synchronized Properties getSettings(){
			return settings;
		}
		
				
		/**
		 * Saves all the properties to a property file in external storage.
		 * 
		 */
		public static synchronized boolean saveProperties(Context context, Properties newSettings){
			settings = newSettings;
			if (!isExternalWritable()){
				System.out.println(MovieManager.class.getSimpleName() + "Error: Unable to write to external storage.");
				return false;
			}
			try {
				File dir = new File(context.getExternalFilesDir(null), context.getString(R.string.file_dir));
				dir.mkdirs();
				File file = new File(dir, FILE_NAME);
				file.createNewFile();
				settings.store(new FileWriter(file), "");
				return true;
			} catch (IOException e1) {
				e1.printStackTrace();
				return false;
			}
		}
			
		/**
		 * Checks if the settings have been intialized
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
		 * Checks to make sure that the settings manager is initialized, and that external files are readable.
		 * @return
		 */
		protected static boolean preInitialize(){
			if (initialized){
				System.out.println(SettingsManager.class.getSimpleName() + ": Already initialized.");
				return false;
			}
			
			if (!isExternalReadable()){
				System.out.println(SettingsManager.class.getSimpleName() + "Error: Unable to read from external storage.");
				return false;
			}
			return true;
		}

		public static String getSetting(String settingName) {
			return settings.getProperty(settingName);
		}
		
		
}
