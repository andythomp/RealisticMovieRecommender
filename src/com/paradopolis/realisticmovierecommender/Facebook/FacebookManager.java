/**
 * 
 */
package com.paradopolis.realisticmovierecommender.Facebook;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.util.Log;

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionDefaultAudience;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;
import com.paradopolis.realisticmovierecommender.MainActivity;
import com.paradopolis.realisticmovierecommender.managers.MovieManager;
import com.paradopolis.realisticmovierecommender.managers.SettingsManager;
import com.paradopolis.realisticmovierecommender.webservices.OMDBCalls;
import com.paradopolis.realisticmovierecommender.webservices.RottenTomatoesCalls;

/**
 * Facebook Manager, manages all the information related to facebook, and does a large amount of communication with other managers.
 * Since the application is so tied to facebook, this manager will communicate with other services using the information about the user,
 * andthe user's friends.
 * @author Andrew Thompson
 * 
 */
public class FacebookManager{

	private static final List<String> PERMISSIONS = Arrays.asList(
			"friends_likes", "user_likes");
	
	public static final int REAUTH_ACTIVITY_CODE = 100;

	public static final int PICTURE_SIZE = 50;
	public static final String ME = "me";
	public static final String FRIENDS = "me/friends";
	public static final String ID = "id";
	public static final String FIELDS = "fields";
	public static final String MOVIES = "movies";
	public static final String GENRE = "genre";
	public static final String PICTURE_PATH = "picture.width(" + PICTURE_SIZE + ").height("+ PICTURE_SIZE +")";
	public static final String LIKES = "likes";
	public static final String NAME = "name";
	public static final String DESCRIPTION = "description";
	public static final String PICTURE = "picture";
	public static final String URL = "url";
	public static final String DATA = "data";
	
	private static boolean retrievedMyMovies = false;
	private static boolean retrievedFriendsMovies = false;
	
	private static MainActivity controller;
	private static FacebookManager singleton;
	private Session.StatusCallback callback; 
	
	private FacebookUser user;
	private HashMap<String, FacebookUser> friends;
	private HashMap<String, Movie> facebookMovies;
	
	
	/**
	 * Gets the required permissions for Facebook to have full functionality.
	 * @return - List of permissions in string form.
	 */
	public static final List<String> getPermissions(){
		return PERMISSIONS;
	}
	
	/**
	 * Gets the primary user who logged in.
	 * @return - Active user.
	 */
	public FacebookUser getUser() {
		return user;
	}

	/**
	 * Gets the user's friend list.
	 * @return - Array list of Facebook Users (User's friends)
	 */
	public ArrayList<FacebookUser> getFriends() {
		return new ArrayList<FacebookUser>(friends.values());
	}

	/**
	 * Implements a singleton to ensure there is only ever one FacebookManager
	 * @return
	 */
	public static FacebookManager getFacebookManager(){
		if (singleton == null){
			singleton = new FacebookManager();
			return singleton;
		}
		else{
			return singleton;
		}
	}
	
	/**
	 * Updates the callback with a new controller.
	 * @param callBack - new Main Activity
	 */
	public static void updateUICallback(MainActivity callBack){
		controller = callBack;
	}
	
	/**
	 * Constructor for facebook manager.
	 */
	public FacebookManager() {
		friends = new HashMap<String, FacebookUser>();
		facebookMovies = new HashMap<String, Movie>();
		callback = new Session.StatusCallback() {
		    @Override
		    public void call(final Session session, final SessionState state, final Exception exception) {
		        onSessionStateChange(session, state, exception);
		    }
		};
		
	}

	/**
	 * When a change occurs to the state of the session, this is the callback function for that event.
	 * @param session - Handle to the session that was changed.
	 * @param state - the updated session state;
	 * @param exception - Exception that occured in state change.
	 * 
	 * @author Andrew Thompson
	 */
	private void onSessionStateChange(final Session session, SessionState state, Exception exception) {
	    if (session != null && session.isOpened()) {
	    	if (state.equals(SessionState.OPENED_TOKEN_UPDATED)) {
	    		Log.i("Client", "OPENED TOKEN UPDATED");
            } else {
            	Log.i("Client", "SOMETHING ELSE");
                makeMeRequest();
            }
	    }
	}
	

	/**
	 * Gets the facebook callback.
	 * @return - callback
	 */
    public Session.StatusCallback getCallback(){
    	return callback;
    }
    
    /**
     * Makes a request to the session to open.
     */
    public void makeOpenRequest(){
    	Session.openActiveSession(controller, false, null);
    }
    
    /**
     * Makes a "me" request on facebook. Gets back the primary user profile.
     * User is defined as the one who logged in.
     */
	public void makeMeRequest() {
	    // Make an API call to get user data and define a 
	    // new callback to handle the response.
		controller.pauseUserInput();
		final Session session = Session.getActiveSession();
       	if (session.getAccessToken() == null){
       		requestReadPermissions();
       		return;
       	}
	    Request request = Request.newMeRequest(session, 
	            new Request.GraphUserCallback() {
	        @Override
	        public void onCompleted(GraphUser graphUser, Response response) {
	            // If the response is successful
	            if (session == Session.getActiveSession()) {
	                if (graphUser != null) {
	                	user = new FacebookUser(graphUser.getId(), graphUser.getName());
	                	controller.processPersonalInfo(graphUser.getId());
	                }
	            }
	            if (response.getError() != null) {
	               	Log.i("Client", response.getError().getErrorMessage());
	            }
	        }
	    });
	    request.executeAsync();
	} 
	
	/**
	 * Requests permissions from Facebook as outlined in the PERMISSIONS constant. This should not need to be used,
	 * except in rare circumstances. Permissions are set upon logging in, and should never change.
	 * 
	 * @author Andrew Thompson
	 */
	private void requestReadPermissions() {
		Session session = Session.getActiveSession();
		if (session != null) {
			Session.NewPermissionsRequest newPermissionsRequest = new Session.NewPermissionsRequest(controller,PERMISSIONS)
				.setDefaultAudience(SessionDefaultAudience.FRIENDS)
				.setRequestCode(REAUTH_ACTIVITY_CODE);
			session.requestNewReadPermissions(newPermissionsRequest);
		}
	}
	
	
	 
	 /**
	  * Gets the current user's favourite movies. Equivalent to getting favourite movies for a user,
	  * with a user key of "me". Done through the Facebook API.
	  * @author Andrew Thompson
	  */
	 public boolean getUsersFavouriteMovies(){
		 // Make an API call to get user data and define a 
		 // new callback to handle the response.
		 String userID = ME;
		 Session session = Session.getActiveSession();
		 //Check to make sure the session is not null, and is in an open state.
		if (session == null || !session.isOpened()) {
			return false;
		}
		//Get the permissions and make sure that they are set.
		//If they are not, request the permissions.
		List<String> permissions = session.getPermissions();
		if (!permissions.containsAll(PERMISSIONS)) {
			requestReadPermissions();
			return false;
		}
		 
		 
		 String graphPath = userID +"/" +  MOVIES + "?";
		 Bundle params = new Bundle();
		 
		 params.putString("fields", DESCRIPTION + "," + GENRE + "," + LIKES + "," + PICTURE_PATH + "," + NAME);
		 Request request = new Request(Session.getActiveSession(), graphPath, params, HttpMethod.GET, new Request.Callback() {
			 @Override
			 public void onCompleted(Response response) {
				try {
					//If facebook does not return us a graph object, handle the error.
					if (response.getGraphObject() == null){
						Log.i("Client", "Response was null.");
						return;
					}
					//Get an array of movies from the user object.
					JSONArray array = response.getGraphObject().getInnerJSONObject().getJSONArray(DATA);
					for (int i = 0; i < array.length(); i++){
						JSONObject jsonMovie = array.getJSONObject(i);
						Movie tempMovie = new Movie();
						//Try and grab as much information as we can from each movie.
						try{
							tempMovie.setDescription(jsonMovie.getString(DESCRIPTION));
						}
						catch (Exception e){}
						try{
							tempMovie.setFacebookID(jsonMovie.getString(ID));
						}
						catch (Exception e){}
						try{
							tempMovie.setTitle(jsonMovie.getString(NAME));
						}
						catch (Exception e){}
						try{
							tempMovie.setIconUrl(jsonMovie.getJSONObject(PICTURE).getJSONObject(DATA).getString(URL));
						}
						catch (Exception e){}
						try{
							tempMovie.setLikes(jsonMovie.getInt(LIKES));	
						}
						catch (Exception e){}
						try{
							tempMovie.setGenres(jsonMovie.getString(GENRE));
						}
						catch (Exception e){}
						//If the movie manager doesn't already contain this movie, then add the temp movie.
						if (MovieManager.getMovie(tempMovie.getFacebookId()) == null){
							user.addMovie(tempMovie);
							//If we haven't already found this particular movie, add it to the facebookMovie array
							if (!facebookMovies.containsKey(tempMovie.getFacebookId())){
								facebookMovies.put(tempMovie.getFacebookId(), tempMovie);
							}
						}
						//Otherwise, add the existing movie
						else{
							user.addMovie(MovieManager.getMovie(tempMovie.getFacebookId()));
						}
					}
					retrievedMyMovies = true;
					processAllMovies();
				} catch (JSONException e) {
					e.printStackTrace();
				} 
			 }
		 });
		 request.executeAsync();
		 return true;
	 }
	 
	 /**
	  * Gets the current user's favourite movies. Equivalent to getting favourite movies for a user,
	  * with a user key of "me". Done through the Facebook API.
	  * @author Andrew Thompson
	  */
	 public boolean getFriendsFavouriteMovies(){
		 String userID = FRIENDS;
		 Session session = Session.getActiveSession();
		 //Check to make sure the session is not null, and is in an open state.
		if (session == null || !session.isOpened()) {
			return false;
		}
		//Get the permissions and make sure that they are set.
		//If they are not, request the permissions.
		List<String> permissions = session.getPermissions();
		if (!permissions.containsAll(PERMISSIONS)) {
			requestReadPermissions();
			return false;
		}

		 String graphPath = userID + "?";
		
		 Bundle params = new Bundle();
		 params.putString("fields", NAME + "," + PICTURE_PATH + "," + MOVIES + ".fields(" + DESCRIPTION + "," + GENRE + "," + LIKES + "," + PICTURE_PATH + "," + NAME + ")");
		 Log.i("Client", params.toString());
		 Request request = new Request(Session.getActiveSession(), graphPath, params, HttpMethod.GET, new Request.Callback() {
			 @Override
			 public void onCompleted(Response response) {
				try {
					//If facebook does not return us a graph object, handle the error.
					if (response.getGraphObject() == null){
						Log.i("Client", "Response was null.");
						return;
					}
					
					//Get the array of friends from the graph object.
					JSONArray userArray = response.getGraphObject().getInnerJSONObject().getJSONArray(DATA);
					//Iterate over each friend object
					for (int i = 0; i < userArray.length(); i++){
						JSONObject jsonUser = userArray.getJSONObject(i);
						FacebookUser tempUser = new FacebookUser(jsonUser.getString(ID), jsonUser.getString(NAME));
						tempUser.setPictureURL(jsonUser.getJSONObject(PICTURE).getJSONObject(DATA).getString(URL));
						if (jsonUser.has(MOVIES)){
							//For each friend, if they have movies iterate over their movies
							JSONArray movieArray = jsonUser.getJSONObject(MOVIES).getJSONArray(DATA);
							for (int j = 0; j < movieArray.length(); j++){
								JSONObject jsonMovie = movieArray.getJSONObject(j);
								Movie tempMovie = new Movie();
								//Try and grab as much information as we can about hte facebook movie.
								try{
									tempMovie.setFacebookID(jsonMovie.getString(ID));
								}
								catch (Exception e){}
								try{
									tempMovie.setDescription(jsonMovie.getString(DESCRIPTION));
								}
								catch (Exception e){}
								try{
									tempMovie.setTitle(jsonMovie.getString(NAME));
								}
								catch (Exception e){}
								try{
									tempMovie.setIconUrl(jsonMovie.getJSONObject(PICTURE).getJSONObject(DATA).getString(URL));
								}
								catch (Exception e){}
								try{
									tempMovie.setLikes(jsonMovie.getInt(LIKES));	
								}
								catch (Exception e){}
								try{
									tempMovie.setGenres(jsonMovie.getString(GENRE));
								}
								catch (Exception e){}
								//After gathering as much information about the movie from facebook as we can, check if
								//it is already in our movie database, add the pre-existing version. Otherwise, add the new version
								if (MovieManager.getMovie(tempMovie.getFacebookId()) == null){
									//In this if, the movie does not exist in the movie manager
									
									//Add this movie to the user's favorite movies.
									tempUser.addMovie(tempMovie);
									//If we haven't already found this particular movie, add it to the facebookMovie array
									if (!facebookMovies.containsKey(tempMovie.getFacebookId())){
										facebookMovies.put(tempMovie.getFacebookId(), tempMovie);//Add this user to a list of users who like the movie in the movie manager
										tempMovie.addRecommender(tempUser);
									}
								}
								//
								else{
									//Add this movie to the user's favorite movies.
									tempUser.addMovie(MovieManager.getMovie(tempMovie.getFacebookId()));
									//Add this user to a list of users who like the movie in the movie manager
									MovieManager.getMovie(tempMovie.getFacebookId()).addRecommender(tempUser);
								}
								
							}
						}
						//Add this friend to our list of friends, and continue
						friends.put(tempUser.getId(), tempUser);
					}
					//Flag friend work as being completed.
					retrievedFriendsMovies = true;
					//Call process all movies.
					processAllMovies();
				} catch (JSONException e) {
					e.printStackTrace();
				} 
			 }
		 });
		 request.executeAsync();
		 return true;
	 }
	 
	 /**
	  * Called once both friends and user's movies have been processed.
	  * Both are necessary for application to run. Method is thread safe (synchronized).
	  * Will only run once both friend and user's movies have been retrieved.
	  */
	 public synchronized void processAllMovies(){
		 if (retrievedMyMovies && retrievedFriendsMovies){
			 
			 new Thread(new Runnable(){

				@Override
				public void run() {
					final ArrayList<Movie> movieList = new ArrayList<Movie>(facebookMovies.values());
					 
					 //If the movie manager contains a movie, it should have the most up to date information on that movie.
					 //Go through the facebook movies, and check each one to see if it is already in the movie manager.
					 //If it is, we don't need to get more information on it.
					 for (int i = 0; i < movieList.size(); i++){
						 if (MovieManager.contains(movieList.get(i).getFacebookId())){
							 movieList.remove(i);
							 i--;
						 }
					 }
					//Setup the threadpool. We want to be efficient with our threads, or else there is no point.
					 int  corePoolSize  =    5;
					 int  maxPoolSize   =   10;
					 long keepAliveTime = 20000;
					 ThreadPoolExecutor pool = new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTime, TimeUnit.MILLISECONDS,  new LinkedBlockingQueue<Runnable>());
					 
					 final CountDownLatch latch = new CountDownLatch(movieList.size());
					 //Go over each remaining movie, and request detailed information on the movie. 
					 for (int i = 0; i < movieList.size(); i++){
						 final int num = i;
						 pool.execute(new Runnable(){
								@Override
								public void run() {
									JSONObject object = OMDBCalls.getMovieOMDB(movieList.get(num).getTitle());
									
									try {
										if (object.getString("Response").equalsIgnoreCase("False")){
											latch.countDown();
											Log.i("Client", "Lost a movie.");
											return;
										}
										if (!object.getString(Movie.TYPE).equalsIgnoreCase(Movie.MOVIE_TYPE)){
											latch.countDown();
											Log.i("Client", "Was not a movie.");
											return;
										}
										movieList.get(num).setTitle(object.getString(Movie.TITLE));
										movieList.get(num).setYear(object.getString(Movie.YEAR));
										movieList.get(num).setPgRating(object.getString(Movie.PG_RATED));
										movieList.get(num).setRuntime(object.getString(Movie.RUNTIME));
										movieList.get(num).setGenres(object.getString(Movie.GENRES));
										movieList.get(num).setDescription(object.getString(Movie.PLOT));
										movieList.get(num).setRating(object.getString(Movie.IMDB_RATING));
										movieList.get(num).setImdbVotes(object.getString(Movie.IMDB_VOTES));
										movieList.get(num).setImdbId(object.getString(Movie.IMDB_ID));
										MovieManager.addMovie(movieList.get(num));
										latch.countDown();
									}catch (Exception e){
										latch.countDown();
										e.printStackTrace();
									}
								}
							 });
					 }
					 
					 try {
						latch.await(60, TimeUnit.SECONDS);
						System.out.println("Latch completed with " + latch.getCount() + " remaining.");
						MovieManager.saveMovies(controller);
						controller.resumeUserInput();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					controller.resumeUserInput();
					 
				}
				 
			 }).start();
			 
		 }
		 else{
			 Log.i("Client", "ME: " + retrievedMyMovies + ", FRIENDS: " + retrievedFriendsMovies);
			 return;
		 }
	 }
	 
	 /**
	  * Makes a global recommendation for the user. 
	  */
	 public void makeGlobalRecommendation(){
		 int numFavoriteMovies = 6;
		 
		 
		 if (user.movies().size() <= 0){
			 //User needs to have atleast one movie like in order for app to work.
			 return;
		 }
		 
		 HashMap<String, Float> userMap = user.getGenreMap();
		 normalizeGenreMap(userMap);
		//Get the user's 3 favorite genres.favoriteMovies.put(favoriteGenre, tempMovie);
		 int numGenres = 3;
		 ArrayList<Entry<String,Float>> usersFavoriteGenres = new ArrayList<Entry<String,Float>>();
		 for (Iterator<Entry<String,Float>> iterator = userMap.entrySet().iterator(); iterator.hasNext();) {
			 Entry<String,Float> entry = iterator.next();
			 if (usersFavoriteGenres.size() < numGenres){
				 usersFavoriteGenres.add(entry);
				 continue;
			 }
			 else{
				 usersFavoriteGenres.add(entry);
				 Entry<String,Float> leastFavorite = usersFavoriteGenres.get(0);
				 for (int i = 0; i < usersFavoriteGenres.size(); i++){
					 if(leastFavorite.getValue() > usersFavoriteGenres.get(i).getValue()){
						 leastFavorite = usersFavoriteGenres.get(i);
					 }
				 }
				 usersFavoriteGenres.remove(leastFavorite);
			 }
		 }
		 
		
		
		 //Get all the user movies that are part of 
		 
		 HashMap<String, Movie> usersFavoriteMovies = new HashMap<String, Movie>();
		 //Go through the user's movies, all of them.
		 for (int i = 0; i < user.movies().size(); i++){
			 Movie tempMovie = user.movies().get(i);
			 String key = tempMovie.getFacebookId();
			 boolean hasGenre = false;
			 //Check if it is in the favorite genres.
			 for (int j = 0; j < usersFavoriteGenres.size(); j++){
				 String favoriteGenre = usersFavoriteGenres.get(j).getKey();
				 //If the movie doesn't have one of the user's favorite genres in it continue
				 if (tempMovie.genres().contains(favoriteGenre)){
					 hasGenre = true;
				 }
				 else{
					 continue;
				 }
			 }
			 if (!hasGenre){
				 continue;
			 }
			 if (usersFavoriteMovies.size()< numFavoriteMovies){
				 usersFavoriteMovies.put(key, tempMovie);
			 }
			 else{
				 Movie lowestMovie = null;
				 usersFavoriteMovies.put(key, tempMovie);
				 for (Iterator<Movie> iterator = usersFavoriteMovies.values().iterator(); iterator.hasNext();) {
					 Movie currentMovie = (Movie) iterator.next();
					 if (lowestMovie == null){
						 lowestMovie = currentMovie;
					 }
					 else if(Double.valueOf(lowestMovie.getRating()) > Double.valueOf(currentMovie.getRating())){
						 lowestMovie = currentMovie;
					 }
					 
				 }
				 usersFavoriteMovies.remove(lowestMovie.getFacebookId());
				 
			 }
		 }
		 
		
		 
		 //Create the recommendation info
		 String recommendInfo = "";
		 //Get the user's top movies
		 recommendInfo = recommendInfo + "Top Movies: "+"\n";
		 for (Iterator<Movie> iterator = usersFavoriteMovies.values().iterator(); iterator.hasNext();) {
			Movie movie = (Movie) iterator.next();
			recommendInfo = recommendInfo + movie.getTitle()+"\n";
		 }
		
		 controller.processRecommendationInfo(recommendInfo);
		 //Make recommendations for the user's favorite movies.
		 for (Iterator<Movie> iterator = usersFavoriteMovies.values().iterator(); iterator.hasNext();) {
			Movie favoriteMovie  = (Movie) iterator.next();
			RottenTomatoesCalls.getMovieAlias(favoriteMovie.getImdbId());
		 }
		
	 }

	 
	/**
	 * Given a Genre Map, normalizes out the map into percentages.
	 * If planning on comparing two users, normalizing map is important to equalize playing
	 * field for users who have lots of movie likes, and users who do not.
	 * @param genreMap - Genre map to be normalized.
	 */
	 public void normalizeGenreMap(HashMap<String, Float> genreMap){
		 float totalGenres = 0;
		 for (Iterator<Entry<String, Float>> iterator = genreMap.entrySet().iterator(); iterator.hasNext();) {
				totalGenres = totalGenres + iterator.next().getValue();
		 }
		 for (Iterator<Entry<String, Float>> iterator = genreMap.entrySet().iterator(); iterator.hasNext();) {
				Entry<String, Float> genre = (Entry<String, Float>) iterator.next();
				genre.setValue(genre.getValue() / totalGenres);
		}
		 float test = 0;
		 for (Iterator<Entry<String, Float>> iterator = genreMap.entrySet().iterator(); iterator.hasNext();) {
				test = test + iterator.next().getValue();
		 }
	 }
	 
	 
	 /**
	  * Makes a social recommendation for the user. this function is called if recommend is pressed,
	  * and the user has selected "social" in the settings tab.
	  */
	 public void makeSocialRecommendation(){
		 double pearsonThreshold = Double.parseDouble(SettingsManager.getSetting(SettingsManager.MIN_PEARSON));
		 
		 if (user.movies().size() <= 0){
			 //User needs to have atleast one movie like in order for app to work.
			 return;
		 }
		 HashMap<String, Float> userMap = user.getGenreMap();
		 normalizeGenreMap(userMap);
		 
		//Get the user's 3 favorite genres.favoriteMovies.put(favoriteGenre, tempMovie);
		 int numGenres = 3;
		 ArrayList<Entry<String,Float>> usersFavoriteGenres = new ArrayList<Entry<String,Float>>();
		 for (Iterator<Entry<String,Float>> iterator = userMap.entrySet().iterator(); iterator.hasNext();) {
			 Entry<String,Float> entry = iterator.next();
			 if (usersFavoriteGenres.size() < numGenres){
				 usersFavoriteGenres.add(entry);
				 continue;
			 }
			 else{
				 usersFavoriteGenres.add(entry);
				 Entry<String,Float> leastFavorite = usersFavoriteGenres.get(0);
				 for (int i = 0; i < usersFavoriteGenres.size(); i++){
					 if(leastFavorite.getValue() > usersFavoriteGenres.get(i).getValue()){
						 leastFavorite = usersFavoriteGenres.get(i);
					 }
				 }
				 usersFavoriteGenres.remove(leastFavorite);
			 }
		 }
		 
		 //Determine which friends are similiar
		 ArrayList<FacebookUser> goodFriends = new ArrayList<FacebookUser>();
		for (Iterator<FacebookUser> iterator = friends.values().iterator(); iterator.hasNext();) {
			 FacebookUser friend = (FacebookUser) iterator.next();
			 HashMap<String, Float> friendMap = friend.getGenreMap();
			 normalizeGenreMap(friendMap);
			 friend.setPearsonCoefficient(pearsonCoefficient(userMap, friendMap));
			 if (friend.getPearsonCoefficient() > pearsonThreshold){
				 goodFriends.add(friend);
			 }
		}
		 
		
		//Determine the best friends!
		 int numFriends = 3;
		 ArrayList<FacebookUser> bestFriends = new ArrayList<FacebookUser>();
		 for (Iterator<FacebookUser> iterator = goodFriends.iterator(); iterator.hasNext();) {
			 FacebookUser friend = iterator.next();
			 if (bestFriends.size() < numFriends){
				 bestFriends.add(friend);
				 continue;
			 }
			 else{
				 bestFriends.add(friend);
				 FacebookUser mehFriend = bestFriends.get(0);
				 for (int i = 0; i < bestFriends.size(); i++){
					 if(mehFriend.getPearsonCoefficient() > bestFriends.get(i).getPearsonCoefficient()){
						 mehFriend = bestFriends.get(i);
					 }
				 }
				 bestFriends.remove(mehFriend);
			 }
		 }
		
		 //Get all the user movies that are part of 
		 int numFavoriteMovies = 3;
		 HashMap<String, Movie> usersFavoriteMovies = new HashMap<String, Movie>();
		 //Go through the user's movies, all of them.
		 for (int i = 0; i < user.movies().size(); i++){
			 Movie tempMovie = user.movies().get(i);
			 String key = tempMovie.getFacebookId();
			 boolean hasGenre = false;
			 //Check if it is in the favorite genres.
			 for (int j = 0; j < usersFavoriteGenres.size(); j++){
				 String favoriteGenre = usersFavoriteGenres.get(j).getKey();
				 //If the movie doesn't have one of the user's favorite genres in it continue
				 if (tempMovie.genres().contains(favoriteGenre)){
					 hasGenre = true;
				 }
				 else{
					 continue;
				 }
			 }
			 if (!hasGenre){
				 continue;
			 }
			 if (usersFavoriteMovies.size()< numFavoriteMovies){
				 usersFavoriteMovies.put(key, tempMovie);
			 }
			 else{
				 Movie lowestMovie = null;
				 usersFavoriteMovies.put(key, tempMovie);
				 for (Iterator<Movie> iterator = usersFavoriteMovies.values().iterator(); iterator.hasNext();) {
					 Movie currentMovie = (Movie) iterator.next();
					 if (lowestMovie == null){
						 lowestMovie = currentMovie;
					 }
					 else if(Double.valueOf(lowestMovie.getRating()) > Double.valueOf(currentMovie.getRating())){
						 lowestMovie = currentMovie;
					 }
					 
				 }
				 usersFavoriteMovies.remove(lowestMovie.getFacebookId());
				 
			 }
		 }
		 
		//Get 3 favorite movies for the 3 friends individual favorite genres.
		 HashMap<String, Movie> friendsFavoriteMovies = new HashMap<String, Movie>();
		 for (int i = 0; i < bestFriends.size(); i++){
			 FacebookUser friend = bestFriends.get(i);
			 
			 //Determine the friends top genre
			 Entry<String,Float> topFriendGenre = null;
			 HashMap<String, Float> friendMap = friend.getGenreMap();
			 normalizeGenreMap(friendMap);
			 for (Iterator<Entry<String,Float>> iterator = friendMap.entrySet().iterator(); iterator.hasNext();) {
				 Entry<String,Float> entry = iterator.next();
				 if (topFriendGenre == null){
					 topFriendGenre = entry;	
				 }
				 else{
					 if (entry.getValue() > topFriendGenre.getValue()){
						topFriendGenre = entry;
					 }
				 }
			 }
			 
			 //Check to make sure we have a top genre.
			 if (topFriendGenre == null){
				 continue;
			 }
			 
			 //Pick out the friend's top movie in that genre.
			 Movie topFriendMovie = null;
			 for (int j = 0; j < friend.movies().size(); j++){
			 	Movie movie = friend.movies().get(j);
			 	if (usersFavoriteMovies.containsKey(movie.getFacebookId())){
			 		continue;
			 	}
			 	if (topFriendMovie == null){
			 		topFriendMovie = movie;	
				}
				else{
					 try{
						 if (Double.valueOf(movie.getRating()) > Double.valueOf(topFriendMovie.getRating())){
							 topFriendMovie = movie;
						 }
					 }
					 catch(Exception e){
						 
					 }
				 }
			 }
			 friendsFavoriteMovies.put(bestFriends.get(i).getId(), topFriendMovie);
		 }
		 
		 //Create the recommendation info
		 String recommendInfo = "";
		 //Get the user's top movies
		 recommendInfo = recommendInfo + "Top Movies: "+"\n";
		 for (Iterator<Movie> iterator = usersFavoriteMovies.values().iterator(); iterator.hasNext();) {
			Movie movie = (Movie) iterator.next();
			recommendInfo = recommendInfo + movie.getTitle()+"\n";
		 }
		 //Get the user's top friends contributing to the recommendation
		 recommendInfo = recommendInfo + "\nTop Friends: "+"\n";
		 for (Iterator<FacebookUser> iterator = bestFriends.iterator(); iterator.hasNext();) {
			 FacebookUser friend = (FacebookUser) iterator.next();
			 recommendInfo = recommendInfo + friend.getName()+"\n";
		 }
		 //Get the top friends contributing movies
		 recommendInfo = recommendInfo + "\nFriends Movies: "+"\n";
		 for (Iterator<Movie> iterator = friendsFavoriteMovies.values().iterator(); iterator.hasNext();) {
			Movie movie = (Movie) iterator.next();
			recommendInfo = recommendInfo + movie.getTitle()+"\n";
		 }
		 controller.processRecommendationInfo(recommendInfo);
		 //Make recommendations for the user's favorite movies.
		 for (Iterator<Movie> iterator = usersFavoriteMovies.values().iterator(); iterator.hasNext();) {
			Movie favoriteMovie  = (Movie) iterator.next();
			RottenTomatoesCalls.getMovieAlias(favoriteMovie.getImdbId());
		 }
		//Make recommendations for the friends favorite movies.
		 for (Iterator<Movie> iterator = friendsFavoriteMovies.values().iterator(); iterator.hasNext();) {
			Movie favoriteMovie  = (Movie) iterator.next();
			RottenTomatoesCalls.getMovieAlias(favoriteMovie.getImdbId());
		 }
	 }
	 
	
	 /**
	  * Given two maps of string to float, compares how similiar they are and computes a genre map.
	  * Useful for Genre Maps. Order is unimportant between maps (pearson will remain the same).
	  * @param map1 - First Map
	  * @param map2 - Second Map
	  * @return - Double that is the pearson coefficient between both maps.
	  */
	 public double pearsonCoefficient(HashMap<String, Float> map1, HashMap<String, Float> map2){
			double pearsonCoefficient;
			int n = 0;
			double sumX, sumY, sumXY, sumXSquared, sumYSquared;
			sumX = sumY = sumXY = sumXSquared = sumYSquared = 0;
			for (Iterator<Entry<String, Float>> iterator = map1.entrySet().iterator(); iterator.hasNext();) {
				Entry<String, Float> entry = (Entry<String, Float>) iterator.next();
				double x, y;
				x = entry.getValue();
				y = 0;
				if (map2.containsKey(entry.getKey())){
					y = map2.get(entry.getKey());
					map2.remove(entry.getKey());
				}
				sumX = sumX + x;
				sumY = sumY + y;
				sumXY = sumXY + (x*y);
				sumXSquared = (sumXSquared + Math.pow(x, 2));
				sumYSquared = (sumYSquared + Math.pow(y, 2));
				n++;
			}
			for (Iterator<Entry<String, Float>> iterator = map2.entrySet().iterator(); iterator.hasNext();) {
				Entry<String, Float> entry = (Entry<String, Float>) iterator.next();
				double x, y;
				x = 0;
				y = entry.getValue();
				sumX = sumX + x;
				sumY = sumY + y;
				sumXY = sumXY + (x*y);
				sumXSquared = (sumXSquared + Math.pow(x, 2));
				sumYSquared = (sumYSquared + Math.pow(y, 2));
				n++;
			}
			try{
				pearsonCoefficient = (n*sumXY - (sumX * sumY))/
								Math.sqrt((n*sumXSquared - Math.pow(sumX, 2)) * (n*sumYSquared - Math.pow(sumY, 2)));
				return pearsonCoefficient;
			}
			catch(Exception e){
				return 0;
			}
				
			
			
			
		}


	 /**
	  * Logs the user out of facebook.
	  */
	public void logout() {
		friends.clear();
		facebookMovies.clear();
		Session.getActiveSession().closeAndClearTokenInformation();
	}
	 
}
