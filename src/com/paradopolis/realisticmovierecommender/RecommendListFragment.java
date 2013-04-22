/**
 * 
 */
package com.paradopolis.realisticmovierecommender;

import java.util.ArrayList;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.paradopolis.realisticmovierecommender.Facebook.Movie;
import com.paradopolis.realisticmovierecommender.managers.RecommendationManager;

/**
 * The recommendation list fragment. Responsible for displaying the user's recommendation.
 * @author Andrew Thompson
 *
 */
public class RecommendListFragment extends ListFragment {

	private RecommendListAdapter adapter;
	private ArrayList<Movie> movies;
	private MainActivity controller;
	private TextView recommendInfo, recommendTitle;
	private Handler handler;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	    View view = inflater.inflate(R.layout.recommendlist, container, false);
	    recommendInfo = (TextView) view.findViewById(R.id.recommend_list_info);
	    recommendTitle = (TextView) view.findViewById(R.id.recommend_list_title);
	    Button button = (Button) view.findViewById(R.id.recommend_list_button);
	    button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				RecommendationManager.saveMovies(controller, movies);
				Toast.makeText(controller, "Saved to CSV!", Toast.LENGTH_SHORT).show();
			}
		});
	    return view;
	}
	    
	    
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		handler = new Handler();
		controller = (MainActivity) this.getActivity();
		movies = new ArrayList<Movie>();
		adapter = new RecommendListAdapter(this.getActivity().getBaseContext(), movies);
		this.setListAdapter(adapter);
	}
	
	
	@Override
	public void onListItemClick(ListView list, View view, int position, long id) {
		super.onListItemClick(list, view, position, id);
		controller.movieSelected(adapter.getItem(position));
	}
	
	/**
	 * Adds a movie to the recommendation
	 * @param movie - Movie to be added
	 */
	public void addMovie(Movie movie){
		this.movies.add(movie);
	}
	
	public void clearMovieList(){
		this.adapter.clear();
	}
	
	/**
	 * Populates the movie list.
	 * @param movies - Movies to be populated.
	 */
	public void populateMovieList(final ArrayList<Movie> movies){
		
		
		if (movies == null)
			return;
		
		adapter.addAll(movies);
		recommendTitle.setText(controller.getResources().getString(R.string.recommendations) + " (" + adapter.getCount() + ")");
		//Get the icon described by the iconUrl of the movie object
		new Thread(new Runnable(){
			@Override
			public void run() {
				for (int i = 0; i < movies.size(); i++){
					final Bitmap bitmap = movies.get(i).getImageBitmap(movies.get(i).getIconUrl());
					movies.get(i).setBitmap(bitmap);
					final int index = i;
					handler.post(new Runnable(){
						@Override
						public void run() {
							try{
								adapter.notifyDataSetChanged();
							}
							catch (Exception e){
								Log.i("Client", "Couldn't set picture to index: " + index);
							}
						}
					});
					
				}
			}
		}).start();
	}




	/**
	 * Sets the recommendation information. 
	 * Any string passed will be displayed at the top of the recommendation fragment.
	 * @param info
	 */
	public void addRecommendInfo(String info) {
		recommendInfo.setText(info);
		
	}
}
