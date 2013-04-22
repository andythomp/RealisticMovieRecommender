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

import com.paradopolis.realisticmovierecommender.Facebook.FacebookUser;

/**
 * Friend list fragment, responsible for showing the user their list of friends
 * @author Andrew Thompson
 *
 */
public class FriendListFragment extends ListFragment {

	private FriendListAdapter adapter;
	private ArrayList<FacebookUser> friendList;
	
	@SuppressWarnings("unused")
	private MainActivity controller;
	private Handler handler;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	    View view = inflater.inflate(R.layout.friendlist, container, false);
	    return view;
	}
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		handler = new Handler();
		controller = (MainActivity) this.getActivity();
		friendList = new ArrayList<FacebookUser>();
		adapter = new FriendListAdapter(this.getActivity().getBaseContext(), friendList);
		this.setListAdapter(adapter);
	}
	
	
	@Override
	public void onListItemClick(ListView list, View view, int position, long id) {
		super.onListItemClick(list, view, position, id);
	}
	
	public void addFriend(FacebookUser friend){
		this.friendList.add(friend);
	}
	
	
	/**
	 * Populates the array list of friends with the friends parameter
	 * @param friends - Friends to be populated
	 */
	public void populateFriendList(final ArrayList<FacebookUser> friends){
		if (friendList == null)
			return;
		
		this.adapter.clear();
		adapter.addAll(friends);
		//Get the icon described by the iconUrl of the movie object
		new Thread(new Runnable(){
			@Override
			public void run() {
				for (int i = 0; i < friends.size(); i++){
					final Bitmap bitmap = friends.get(i).getImageBitmap(friends.get(i).getPictureURL());
					friends.get(i).setPicture(bitmap);
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
