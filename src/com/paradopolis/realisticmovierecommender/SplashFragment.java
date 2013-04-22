/**
 * 
 */
package com.paradopolis.realisticmovierecommender;


import java.util.ArrayList;
import java.util.List;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.widget.LoginButton;
import com.paradopolis.realisticmovierecommender.Facebook.FacebookManager;

/**
 * Fragment used to display a splash screen, containing the title of the application as well as
 * a button to log in. Once the user has logged in, this screen is no longer required.
 * @author Andrew Thompson
 *
 */
public class SplashFragment extends Fragment {
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	    View view = inflater.inflate(R.layout.splash, container, false);
	    LoginButton button = (LoginButton) view.findViewById(R.id.login_button);
	    List<String> permissions = new ArrayList<String>();
		permissions.addAll(FacebookManager.getPermissions());
	    button.setReadPermissions(permissions);
	    
	    //Set the font of the on screen text.
	    Typeface myTypeface = Typefaces.get(getActivity(), "fonts/bettynoir.ttf");
	    TextView myTextView = (TextView) view.findViewById(R.id.splashTitle);
	    myTextView.setTypeface(myTypeface);
	    
	    return view;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

}
