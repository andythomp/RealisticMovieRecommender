/**
 * 
 */
package com.paradopolis.realisticmovierecommender;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.UiLifecycleHelper;
import com.facebook.widget.ProfilePictureView;
import com.paradopolis.realisticmovierecommender.Facebook.FacebookManager;




/**
 * This is the primary fragment. It shows the user their profile picture, among other things.
 * It has access to most buttons that let the user navigate the application.
 * @author Andrew Thompson
 *
 */
public class MainFragment extends Fragment {
	
	
	//UI Elements
	private ProfilePictureView profilePictureView;
	private Button recommendButton;
	private ImageButton friendsButton, favoriteButton, settingsButton, helpButton, infoButton, logoutButton;
	private UiLifecycleHelper uiHelper;
	
	private MainActivity controller;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	    super.onCreateView(inflater, container, savedInstanceState);
	    View view = inflater.inflate(R.layout.mainfrag, container, false);
	    
		// Find the user's profile picture custom view
		profilePictureView = (ProfilePictureView) view.findViewById(R.id.recommender_profile_pic);
		profilePictureView.setCropped(true);

		
		//Setup the main font we will be using
		Typeface myTypeface = Typefaces.get(getActivity(), "fonts/bettynoir.ttf");
	    TextView myTextView = (TextView) view.findViewById(R.id.mainFragTitle);
	    myTextView.setTypeface(myTypeface);
		
		//Establish the Recommender button
		recommendButton = (Button) view.findViewById(R.id.recommendButton);
		recommendButton.setTypeface(myTypeface);
		recommendButton.setText("Recommend");
		recommendButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				recommendButtonOnClick(arg0);
			}
		});
		 
		//Establish the favorites button
		favoriteButton = (ImageButton) view.findViewById(R.id.favouritesButton);
		favoriteButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				favoriteButtonOnClick(arg0);
			}
		 });
		
		//Establish the friends button
		friendsButton = (ImageButton) view.findViewById(R.id.friendsButton);
		friendsButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				friendsButtonOnClick(arg0);
			}
		 });
		
		//Establish the settings button
		settingsButton = (ImageButton) view.findViewById(R.id.settingsButton);
		settingsButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				settingsButtonOnClick(arg0);
			}
		 });
		
		//Establish the help button
		helpButton = (ImageButton) view.findViewById(R.id.helpButton);
		helpButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				helpButtonOnClick(arg0);
			}
		 });
		
		//Find the information button
		infoButton = (ImageButton) view.findViewById(R.id.infoButton);
		infoButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				infoButtonOnClick(arg0);
			}
		 });
		
		
		//Find the information button
		logoutButton = (ImageButton) view.findViewById(R.id.logoutButton);
		logoutButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				logoutButtonOnClick(arg0);
			}
		 });
		 
	    return view;
	}
	
	public void updateUserProfile(String userID){
		// Set the id for the ProfilePictureView
        // view that in turn displays the profile picture.
        profilePictureView.setProfileId(userID);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    controller = (MainActivity) this.getActivity();
	    
	   
	    uiHelper = new UiLifecycleHelper(getActivity(), null);
	    uiHelper.onCreate(savedInstanceState);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	    if (requestCode == FacebookManager.REAUTH_ACTIVITY_CODE) {
	      uiHelper.onActivityResult(requestCode, resultCode, data);
	    }
	}
	
	@Override
	public void onResume() {
	    super.onResume();
	    uiHelper.onResume();
	}

	@Override
	public void onSaveInstanceState(Bundle bundle) {
	    super.onSaveInstanceState(bundle);
	    uiHelper.onSaveInstanceState(bundle);
	}

	@Override
	public void onPause() {
	    super.onPause();
	    uiHelper.onPause();
	}

	@Override
	public void onDestroy() {
	    super.onDestroy();
	    uiHelper.onDestroy();
	}
	 
	public void postMessage(final String message){
		Toast.makeText(this.getActivity(), message, Toast.LENGTH_LONG).show();
		
	}
	
	/**
	 * On click method for the Recommend button. Called when ever the recommend button is pressed.
	 * 
	 * @author Andrew Thompson
	 */
	public void recommendButtonOnClick(View view){
		controller.recommendButtonOnClick();
	}
	
	/**
	 * On click method for the Favorite button. Called when ever the favorite button is pressed.
	 * 
	 * @author Andrew Thompson
	 */
	public void favoriteButtonOnClick(View view){
		controller.favoriteButtonOnClick();
	}
	
	/**
	 * On click method for the Friends button. Called when ever the friends button is pressed.
	 * 
	 * @author Andrew Thompson
	 */
	public void friendsButtonOnClick(View view){
		controller.friendsButtonOnClick();
	}
	
	/**
	 * On click method for the Settings button. Called when ever the settings button is pressed.
	 * 
	 * @author Andrew Thompson
	 */
	public void settingsButtonOnClick(View view){
		controller.settingsButtonOnClick();
	}
	
	/**
	 * On click method for the Logout button. Called when ever the logout button is pressed.
	 * 
	 * @author Andrew Thompson
	 */
	public void logoutButtonOnClick(View view){
		controller.logoutButtonOnClick();
	}
	
	/**
	 * On click method for the Help button. Called when ever the help button is pressed.
	 * 
	 * @author Andrew Thompson
	 */
	public void helpButtonOnClick(View view){
		controller.helpButtonOnClick();
	}
	
	/**
	 * On click method for the Info button. Called when ever the information button is pressed.
	 * 
	 * @author Andrew Thompson
	 */
	public void infoButtonOnClick(View view){
		controller.infoButtonOnClick();
	}
	

	
	
}
