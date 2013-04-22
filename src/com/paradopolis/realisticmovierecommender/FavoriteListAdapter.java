/**
 * 
 */
package com.paradopolis.realisticmovierecommender;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.paradopolis.realisticmovierecommender.Facebook.Movie;

/**
 * List adapter for the Favorite List Fragment
 * @author Andrew Thompson
 *
 */
public class FavoriteListAdapter extends ArrayAdapter<Movie> {
	

    private final Context context;
    private final ArrayList<Movie> values;

    public FavoriteListAdapter(Context context, ArrayList<Movie> values) {
            super(context, R.layout.favoriteitem, values);
            this.context = context;
            this.values = values;
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
    	LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        
        View rowView = inflater.inflate(R.layout.favoriteitem, parent, false);
        
        //Get the movie name
        try{
        TextView movieName = (TextView) rowView.findViewById(R.id.favorite_item_title);
        movieName.setText(values.get(position).getTitle());
        }
        catch(Exception e){
        	
        }
        //Get the movie icon
        ImageView movieIcon = (ImageView) rowView.findViewById(R.id.favorite_item_icon);
        if (values.get(position).getBitmap() != null){
        	movieIcon.setImageBitmap(values.get(position).getBitmap());
            movieIcon.refreshDrawableState();
        }
        
        return rowView;
    }
    

}