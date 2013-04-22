/**
 * 
 */
package com.paradopolis.realisticmovierecommender;


import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.paradopolis.realisticmovierecommender.Facebook.Movie;

/**
 * This fragment simply shows a movie, and some information about the movie.
 * @author Andrew Thompson
 *
 */
public class MovieFragment extends Fragment {

	
	private TextView movieName, movieDescription, movieGenre;
	private ImageView movieIcon;
	private Handler handler;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	    View view = inflater.inflate(R.layout.movie, container, false);
	    /*
	     * Establish all of our links to the layout
	     */
	    movieName = (TextView) view.findViewById(R.id.movieName);
	    movieDescription = (TextView) view.findViewById(R.id.movieDescription);
	    movieGenre = (TextView) view.findViewById(R.id.movieGenre);
	    movieIcon = (ImageView) view.findViewById(R.id.movieIcon);
	    return view;
	}
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		handler = new Handler();
	}
	
	/**
	 * Populates the movie page given a Movie.
	 * @param movie - Movie used to populate the Movie Page
	 * @author Andrew Thompson
	 */
	public void updateMoviePage(final Movie movie){
		movieName.setText(movie.getTitle());
		movieGenre.setText(movie.genres().toString());
		movieDescription.setText(movie.getDescription());
		
		//Get the icon described by the iconUrl of the movie object
		new Thread(new Runnable(){
			@Override
			public void run() {
				final Bitmap bitmap;
				if (movie.getBitmap() == null){
					bitmap = movie.getImageBitmap(movie.getIconUrl());;
				}
				else{
					bitmap = movie.getBitmap();
				}
				handler.post(new Runnable(){
					@Override
					public void run() {
						movieIcon.setImageBitmap(bitmap);
					}
				});
			}
		}).start();
	}
	
}
