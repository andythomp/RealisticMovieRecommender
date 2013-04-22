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
import android.view.ViewGroup;
import android.widget.ListView;

import com.paradopolis.realisticmovierecommender.Facebook.Movie;

/**
 * Favorite List Fragment. Fragment responsible for showing the user's favorite movies.
 * @author Andrew Thompson
 *
 */
public class FavoriteListFragment extends ListFragment {

	private FavoriteListAdapter adapter;
	private ArrayList<Movie> movies;
	private MainActivity controller;
	private Handler handler;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	    View view = inflater.inflate(R.layout.favoritelist, container, false);
	    return view;
	}
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		handler = new Handler();
		controller = (MainActivity) this.getActivity();
		movies = new ArrayList<Movie>();
		adapter = new FavoriteListAdapter(this.getActivity().getBaseContext(), movies);
		this.setListAdapter(adapter);
	}
	
	
	@Override
	public void onListItemClick(ListView list, View view, int position, long id) {
		super.onListItemClick(list, view, position, id);
		controller.movieSelected(adapter.getItem(position));
	}
	
	/**
	 * Adds a movie to the adapter's array list
	 * @param movie - Movie to be added
	 */
	public void addMovie(Movie movie){
		this.movies.add(movie);
	}
	
	
	/**
	 * Populates the adapters array list with a set of movies.
	 * @param movies - Movies to be shown
	 */
	public void populateMovieList(final ArrayList<Movie> movies){
		if (movies == null)
			return;
		
		this.adapter.clear();
		adapter.addAll(movies);
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
}
