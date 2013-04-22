package com.paradopolis.realisticmovierecommender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;
import java.util.Stack;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.paradopolis.realisticmovierecommender.Facebook.FacebookManager;
import com.paradopolis.realisticmovierecommender.Facebook.FacebookUser;
import com.paradopolis.realisticmovierecommender.Facebook.Movie;
import com.paradopolis.realisticmovierecommender.managers.MovieManager;
import com.paradopolis.realisticmovierecommender.managers.SettingsManager;

/**
 * This is the single activity for the application. A lot of the work goes through this activity.
 * It is a fragment activity and so all UI elements get swapped in and out here. In the future, it could
 * be upgraded to use multiple fragments and support tablet screens. 
 * 
 * When a button in any fragment is pressed, it calls the respective button press in this class. This class is then
 * responsible for communicating with it's fragments, and the various managers for the application.
 * @author Paradopolis
 *
 */
public class MainActivity extends FragmentActivity {

	public static final int SPLASH = 0;
	public static final int RECOMMENDER = 1;
	public static final int SETTINGS = 2;
	public static final int MOVIE = 3;
	public static final int RECOMMEND_LIST = 4;
	public static final int FRIEND_LIST = 5;
	public static final int FAVORITE_LIST = 6;
	public static final int FRAGMENT_COUNT = FAVORITE_LIST +1;
	
	/*
	 * Creates a Session Callback that will be called when ever the Session changes states.
	 */
	private Session.StatusCallback callback = new Session.StatusCallback() {
	    @Override
	    public void call(Session session, SessionState state, Exception exception) {
	        onSessionStateChange(session, state, exception);
	    }
	};
	
	private Fragment[] fragments = new Fragment[FRAGMENT_COUNT];
	private boolean isResumed = false;
	private MenuItem settings;
	private static MainActivity singleton;
	
	private UiLifecycleHelper uiHelper;
	private Stack<Integer> backstack;
	private ProgressDialog progressDialog;
	private Handler handler;
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	    uiHelper.onActivityResult(requestCode, resultCode, data);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		/*
		 * The activity has just been created, lets go through
		 * and set the content to the proper layout.
		 */
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_main);
	    singleton = this;
	    
	    /*
	     * Initialize the backstack
	     */
	    backstack = new Stack<Integer>();
	    /*
	     * Establish the UI Helper to preserve the session and UI state
	     * between application sessions
	     */
	    uiHelper = new UiLifecycleHelper(this, callback);
	    uiHelper.onCreate(savedInstanceState);
	    
	   
	    /*
	     * Here we establish the Fragment manager, go through all the fragments, and hide them.
	     */
	    FragmentManager fm = getSupportFragmentManager();
	    fragments[SPLASH] = fm.findFragmentById(R.id.splashFragment);
	    fragments[RECOMMENDER] = fm.findFragmentById(R.id.recommenderFragment);
	    fragments[SETTINGS] = fm.findFragmentById(R.id.settingsFragment);
	    fragments[MOVIE] = fm.findFragmentById(R.id.movieFragment);
	    fragments[RECOMMEND_LIST] = fm.findFragmentById(R.id.recommendList);
	    fragments[FRIEND_LIST] = fm.findFragmentById(R.id.friendList);
	    fragments[FAVORITE_LIST] = fm.findFragmentById(R.id.favoriteList);
	    
	    FragmentTransaction transaction = fm.beginTransaction();
	    for(int i = 0; i < fragments.length; i++) {
	        transaction.hide(fragments[i]);
	    }
	    transaction.commit();
	    
	    /*
	     * Initialize and get a reference to the Facebook Network Manager.
	     */
	    
	    FacebookManager.updateUICallback(this);
	    MovieManager.initialize(this);
	    SettingsManager.initialize(this);
	    
	    progressDialog = new ProgressDialog(this);
	    
	    handler = new Handler();
	}

	
	@Override
	public void onDestroy() {
	    super.onDestroy();
	    uiHelper.onDestroy();
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.equals(settings)) {
            showFragment(SETTINGS, false);
            return true;
        }
        return false;
    }

	@Override
	public void onPause() {
	    super.onPause();
	    uiHelper.onPause();
	    isResumed = false;
	}

	@Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // only add the menu when the selection fragment is showing
        if (fragments[RECOMMENDER].isVisible()) {
            if (menu.size() == 0) {
                settings = menu.add(R.string.settings);
            }
            return true;
        } else {
            menu.clear();
            settings = null;
        }
        return false;
    }
	
	
	@Override
	public void onResume() {
	    super.onResume();
	    uiHelper.onResume();
	    isResumed = true;
	}
	
	@Override
	protected void onResumeFragments() {
	    super.onResumeFragments();
	    Session session = Session.getActiveSession();
	    if (session != null && session.isOpened()) {
	    	//If we have a previous session active, show the recommender fragment
	    	Log.i("Client", "RESUME OPEN SESSION");
	        showFragment(RECOMMENDER, true);
	    } else {
	        //Otherwise, we need a new session, show the splash
	        showFragment(SPLASH, false); 
	    }
	}
	
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
    }
    
    /**
     * Called when the session changes state (via the Session Callback)
     * @param session - Session (best retrieved from statis Session class)
     * @param state - new state
     * @param exception - potential exceptions that may have occured
     * @author Andrew Thompson
     */
    private void onSessionStateChange(Session session, SessionState state, Exception exception) {
	    // Only make changes if the activity is visible
    	Log.i("Client", "Session state change in main.");
	    if (isResumed) {
	        //Check the current session state
	        if (state.isOpened()) {
		    	//If we have a previous session active, show the recommender fragment
	        	getPersonalInfo();
	            showFragment(RECOMMENDER, false);
	        } else if (state.isClosed()) {
		        //Otherwise, we need a new session, show the splash
	            showFragment(SPLASH, false);
	        }
	    }
	}

    
    
    /**
	 * Given a fragment index and a boolean, this function will hide all existing fragments 
	 * and show the given fragmentIndex. If the boolean is set to true, then you can navigate back from the selected fragment
	 * to the previous fragment.
	 * 
	 * @param fragmentIndex - Index of the fragment to be shown.
	 * @param addToBackStack - Whether you want to be able to navigate back from this fragment.
	 * 
	 * @author Andrew Thompson
	 */
	public void showFragment(final int fragmentIndex, final boolean addToBackStack) {
		handler.post(new Runnable(){
			@Override
			public void run() {
				FragmentManager fm = getSupportFragmentManager();
			    FragmentTransaction transaction = fm.beginTransaction();
			    for (int i = 0; i < fragments.length; i++) {
			        if (i == fragmentIndex) {
			            transaction.show(fragments[i]);
			        } else {
			            transaction.hide(fragments[i]);
			        }
			    }
			    //If add to back stack is true, then we can undo this transaction
			    if (addToBackStack) {
			        backstack.push(Integer.valueOf(fragmentIndex));
			    }
			    transaction.commit();
			}
			
		});
	}
    
	/**
	 * Returns a singleton reference to the main activity. Necessary for showing fragments.
	 * @return - Singleton reference to main activity
	 * @author Andrew Thompson
	 */
	public static MainActivity getMainActivity(){
		return singleton;
	}
	
	
	/**
	 * When the back button is pressed...
	 * @author Andrew Thompson
	 */
	@Override
	public void onBackPressed() {
		handler.post(new Runnable(){
			@Override
			public void run() {
				if(backstack.empty()){
					return;
				}
				Integer fragment = backstack.pop();
				if (backstack.empty()){
					backstack.push(fragment);
				}
				else{
					showFragment(backstack.peek(), false);
				}
			}
		});
	}
	
	/**
	 * Creates a spinner, pausing the user from making any additional changes or selections.
	 * After calling this, make sure that resume input is called, or the UI will lock forever.
	 */
	public void pauseUserInput(){
		handler.post(new Runnable(){
			@Override
			public void run() {
				progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				progressDialog.setMessage("Loading Movies...");
				progressDialog.setCancelable(false);
				progressDialog.show();	
			}
		});
	}
	
	/**
	 * Should be called after running pauseUserInput. Removes the spinner.
	 */
	public void resumeUserInput(){
		handler.post(new Runnable(){
			@Override
			public void run() {
				progressDialog.dismiss();
			}
		});
    }
	
	/**
	 * Gets the user's personal info from the Facebook Manager.
	 */
	public void getPersonalInfo(){
		FacebookManager.getFacebookManager().makeMeRequest();
	}
	
	/**
	 * Given the facebook user ID of the user who has logged in, will make the necessary calls
	 * in order to get all the relevant information from facebook. Information pertains to:
	 * -Getting the user's movie likes
	 * -Getting the user's friends' movie likes
	 * -Updating the user's portrait.
	 * @param userID - Facebook User ID of the user who has logged in.
	 */
	public void processPersonalInfo(final String userID){
		handler.post(new Runnable(){
			@Override
			public void run() {
				((MainFragment) fragments[RECOMMENDER]).updateUserProfile(userID);
				FacebookManager.getFacebookManager().getUsersFavouriteMovies();
				FacebookManager.getFacebookManager().getFriendsFavouriteMovies();
			}
		});
	}
	
	
	/**
	 * Processes a list of friends. Starts by sorting them, and then populates the friend list
	 * fragment with those friends.
	 * @param friends - Friends to be processed
	 */
	public void processFriends(final ArrayList<FacebookUser> friends){
		handler.post(new Runnable(){
			@Override
			public void run() {
				Collections.sort(friends);
				((FriendListFragment) fragments[FRIEND_LIST]).populateFriendList(friends);
			}
		});
	}
	
	/**
	 * Processes a list of movies that are the user's favorite movies.
	 * @param movies - Movies to be processed.
	 */
	public void processFavorites(final ArrayList<Movie> movies){
		
		handler.post(new Runnable(){
			@Override
			public void run() {
				((FavoriteListFragment) fragments[FAVORITE_LIST]).populateMovieList(movies);
			}
		});
	}
	
	/**
	 * Processes a list of movies that are the user's recommendation
	 * @param movies - Movies to be processed.
	 */
	public void processRecommendations(final ArrayList<Movie> movies){
		handler.post(new Runnable(){
			@Override
			public void run() {
				((RecommendListFragment) fragments[RECOMMEND_LIST]).populateMovieList(movies);
			}
		});
	}
	
	/**
	 * Processes a string that contains the recommendation info. When a recommendation is made,
	 * information about the recommendation is passed here so that it can be populated on the
	 * recommendation page.
	 * @param movies - Movies to be processed.
	 */
	public void processRecommendationInfo(final String info){
		handler.post(new Runnable(){
			@Override
			public void run() {
				((RecommendListFragment) fragments[RECOMMEND_LIST]).addRecommendInfo(info);
			}
		});
	}
	
	/**
	 * When a movie is selected this is called, so that the controller can show the movie's page.
	 * @param movie - Movie to be shown
	 */
	public void movieSelected(final Movie movie){
		handler.post(new Runnable(){
			@Override
			public void run() {
				((MovieFragment) fragments[MOVIE]).updateMoviePage(movie);
				showFragment(MOVIE, true);
			}
		});
	}
	
	/**
	 * Called from the main fragment, when the recommend button is pressed.
	 * Asks the facebook manager to make a recommendation.
	 */
	public void recommendButtonOnClick(){
		handler.post(new Runnable(){
			@Override
			public void run() {
				if (SettingsManager.getSettings().getProperty(SettingsManager.USE_SOCIAL).equalsIgnoreCase("true")){
					FacebookManager.getFacebookManager().makeSocialRecommendation();
				}
				else{
					FacebookManager.getFacebookManager().makeGlobalRecommendation();
				}
				
				((RecommendListFragment) fragments[RECOMMEND_LIST]).clearMovieList();
				showFragment(RECOMMEND_LIST, true);
				//	processMovies(FacebookNetManager.getFacebookNetManager().getUser().movies());
			}
		});
	}
	
	/**
	 * Called from the main fragment when the favorite button is pressed.
	 * Shows the favorites fragment.
	 */
	public void favoriteButtonOnClick(){
		handler.post(new Runnable(){
			@Override
			public void run() {
				
				processFavorites(FacebookManager.getFacebookManager().getUser().movies());
				showFragment(FAVORITE_LIST, true);
			}
		});
	}
	
	/**
	 * Called from the main fragment when the friends button is pressed.
	 * Shows the friends fragment.
	 */
	public void friendsButtonOnClick(){
		handler.post(new Runnable(){
			@Override
			public void run() {
				processFriends(FacebookManager.getFacebookManager().getFriends());
				showFragment(FRIEND_LIST, true);
			}
		});
	}
	
	/**
	 * Called from the main fragment when the settings button is pressed.
	 * Shows the settings fragment.
	 */
	public void settingsButtonOnClick(){
		handler.post(new Runnable(){
			@Override
			public void run() {
				((SettingsFragment) fragments[SETTINGS]).processProperties(SettingsManager.getSettings());
				showFragment(SETTINGS, true);
			}
		});
	}
	
	private void displayCredits(){
		String title = "Credits";
		String message = "Application developed by Andrew Thompson as an honor's project for Carleton University.\n" +
				"Developed under guidance of Professor Anthony White, Faculty of Computer Science, 2013.";
		AlertDialog alertDialog = new AlertDialog.Builder(this).create();
		alertDialog.setTitle(title);
		alertDialog.setMessage(message);
		alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				// here you can add functions
			}
		});
		alertDialog.show();
	}
	
	/**
	 * Called from the main fragment when the info button is pressed.
	 * 
	 */
	public void infoButtonOnClick(){
		handler.post(new Runnable(){
			@Override
			public void run() {
				displayCredits();
			}
		});
	}

	/**
	 * Called from the main fragment when the help button is pressed.
	 * Currently the button does nothing.
	 * 
	 */
	public void helpButtonOnClick(){
		handler.post(new Runnable(){
			@Override
			public void run() {
				
			}
		});
	}

	/**
	 * Called from the main fragment when the logout button is pressed.
	 * Logs the user out of the application, clears tokens
	 * 
	 */
	public void logoutButtonOnClick() {
		handler.post(new Runnable(){
			@Override
			public void run() {
				FacebookManager.getFacebookManager().logout();
				showFragment(SPLASH, false);
				
			}
		});
	}

	/**
	 * Called from the settings fragment when the save button is pressed.
	 * Tells the settings manager to save the settings to a file.
	 * 
	 */
	public void saveSettings(final Properties newSettings) {
		final Context context = this;
		handler.post(new Runnable(){
			@Override
			public void run() {
				if (SettingsManager.saveProperties(context, newSettings)){
					Toast.makeText(context, "Settings Saved!", Toast.LENGTH_SHORT).show();
				}
				else{
					Toast.makeText(context, "Error Saving Settings", Toast.LENGTH_SHORT).show();
				}
			}
		});
	}
	
}
