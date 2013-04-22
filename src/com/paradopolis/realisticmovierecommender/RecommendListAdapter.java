/**
 * 
 */
package com.paradopolis.realisticmovierecommender;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.paradopolis.realisticmovierecommender.Facebook.Movie;
import com.paradopolis.realisticmovierecommender.managers.MovieManager;
import com.paradopolis.realisticmovierecommender.managers.SettingsManager;

/**
 * This is the adapter for the Recommendation items.
 * @author Andrew Thompson
 *
 */
public class RecommendListAdapter extends ArrayAdapter<Movie> {
	

    private final Context context;
    private final ArrayList<Movie> values;

    public RecommendListAdapter(Context context, ArrayList<Movie> values) {
            super(context, R.layout.recommenditem, values);
            this.context = context;
            this.values = values;
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
    	LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.recommenditem, parent, false);
        // Get some elements
        try{
	        TextView movieName = (TextView) rowView.findViewById(R.id.recommendListName);
	        movieName.setText(values.get(position).getTitle());
        }
        catch(Exception e){
        	
        }

        ImageView movieIcon = (ImageView) rowView.findViewById(R.id.movieListIcon);
        if (values.get(position).getBitmap() != null){
        	movieIcon.setImageBitmap(values.get(position).getBitmap());
            movieIcon.refreshDrawableState();
        }
        
        ImageView recommendIcon = (ImageView) rowView.findViewById(R.id.recommendedImage);
        recommendIcon.setVisibility(ImageView.INVISIBLE);
        
        
        String key = values.get(position).getImdbId();
        if (key == null){
        	return rowView;
        }
        
        Movie movie = MovieManager.getMovie(key);
        if (movie == null){
        	return rowView;
        }
        	
        double pearsonThreshold = Double.parseDouble(SettingsManager.getSetting(SettingsManager.MIN_PEARSON));
		for (int i = 0; i < movie.getRecommenders().size(); i++){
			
        	if (movie.getRecommenders().get(i).getPearsonCoefficient() > pearsonThreshold){
                recommendIcon.setVisibility(ImageView.VISIBLE);
        	}
        	else{
        		 recommendIcon.setVisibility(ImageView.INVISIBLE);
        	}
		}
    	
    	
        
        
        return rowView;
    }
    
    public void updateMovieIcon(Bitmap bitmap, int index){
    }

}