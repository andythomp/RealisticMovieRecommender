/**
 * 
 */
package com.paradopolis.realisticmovierecommender;

import java.util.Properties;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.paradopolis.realisticmovierecommender.managers.SettingsManager;

/**
 * Settings fragment, responsible for showing the current settings.
 * Also allows the user to interact with the settings manager through itself.
 * @author Andrew Thompson
 *
 */
public class SettingsFragment extends Fragment{

	
	
	
	
	private EditText minRatingField, minPearsonField;
	private Switch useSocialSwitch;
	private Button saveButton;
	private MainActivity controller;
	
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	    View view = inflater.inflate(R.layout.settings, container, false);
	    controller = (MainActivity) this.getActivity();
	    
	    //Minimum Pearson Field
	    minPearsonField = (EditText) view.findViewById(R.id.minPearsonField);
	    minPearsonField.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				double minPearson = Double.valueOf(minPearsonField.getText().toString());
				if (minPearson > 1 || minPearson < -1){
					Toast.makeText(getActivity(), "Error: Min Pearson should be between -1 and 1", Toast.LENGTH_SHORT).show();
				}
			}
		});

		
	    
	    //Minimum Rating Field
	    minRatingField = (EditText) view.findViewById(R.id.minRatingField);
	    
	    minRatingField.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				int minRating = Integer.valueOf(minRatingField.getText().toString());
				if (minRating > 100 || minRating < 0){
					Toast.makeText(getActivity(), "Error: Min Rating should be between 0 and 100", Toast.LENGTH_SHORT).show();
				}
			}
		});
	    
	    
	    useSocialSwitch = (Switch) view.findViewById(R.id.useSocialSwitch);
	    saveButton = (Button) view.findViewById(R.id.saveButton);
	    
	    saveButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				saveButtonClicked();
			}
		 });
	    return view;
	}
	
	/**
	 * Processes properties to be dispalyed
	 * @param properties
	 */
	public void processProperties(Properties properties){
		boolean switched = Boolean.parseBoolean(properties.getProperty(SettingsManager.USE_SOCIAL));
		minPearsonField.setText(properties.getProperty(SettingsManager.MIN_PEARSON));
		minRatingField.setText(properties.getProperty(SettingsManager.MIN_RATING));
		useSocialSwitch.setChecked(switched);
	}
	
	
	/**
	 * Called on save button click. Tells the settings manager to save the currently input settings.
	 */
	public void saveButtonClicked(){
		Properties settings = new Properties();
		boolean useSocialNetworks = useSocialSwitch.isChecked();
		int minRating = Integer.valueOf(minRatingField.getText().toString());
		double minPearson = Double.valueOf(minPearsonField.getText().toString());
		
		settings.put(SettingsManager.USE_SOCIAL, Boolean.toString(useSocialNetworks));
		settings.put(SettingsManager.MIN_RATING, Integer.toString(minRating));
		settings.put(SettingsManager.MIN_PEARSON, Double.toString(minPearson));
		
		controller.saveSettings(settings);
	}

}
