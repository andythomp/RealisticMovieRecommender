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

import com.paradopolis.realisticmovierecommender.Facebook.FacebookUser;

/**
 * Item for the Friend List Fragment
 * @author Andrew Thompson
 *
 */
public class FriendListAdapter extends ArrayAdapter<FacebookUser> {
	

    private final Context context;
    private final ArrayList<FacebookUser> values;

    public FriendListAdapter(Context context, ArrayList<FacebookUser> values) {
            super(context, R.layout.recommenditem, values);
            this.context = context;
            this.values = values;
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
    	
    	LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.frienditem, parent, false);
        
        // Get some elements
        //Get the friend's name
        TextView friendName = (TextView) rowView.findViewById(R.id.friendListName);
        friendName.setText(values.get(position).getName());

        //Get the friends picture view
    	ImageView profilePictureView;
    	profilePictureView = (ImageView) rowView.findViewById(R.id.friendListPic);
    	
		//Set the picture view and cache it away in the user's profile.
        if (values.get(position).getPicture() != null){
        	profilePictureView.setImageBitmap(values.get(position).getPicture());
        	profilePictureView.refreshDrawableState();
        }

	
        return rowView;
    }
    

}