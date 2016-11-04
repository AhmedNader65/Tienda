package net.businessmonk.tienda.tienda;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by learnovate on 3/30/14.
 */
public class msgssFragment extends Fragment {

	private final static String ARG_POSITION = "position";
	private int currentPosition = -1;
	static ChatAdapter chatAdapter;
	static ArrayList<String> msg,isMe,companies,msgDate;
	TextView pointsView;
	private ListView msgContainer;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		msg = new ArrayList<>();
		isMe = new ArrayList<>();
		msgDate = new ArrayList<>();
		companies = new ArrayList<>();
		TinyDB tinyDB = new TinyDB(getActivity().getApplicationContext());

		if(savedInstanceState != null){
			//if we recreated this Fragment (for instance from a screen rotate)
			//restore the previous article selection by getting it here
			currentPosition = savedInstanceState.getInt(ARG_POSITION);
		}
		int posi = getActivity().getIntent().getIntExtra("position", 0);

		//inflate the view for this fragment
		View myFragmentView = inflater.inflate(R.layout.content_contact,container,false);

		msgContainer = (ListView)myFragmentView.findViewById(R.id.messagesContainer);
		return myFragmentView;

	}

	public void updateArticleView(int position,int compId){
		currentPosition = position;
		int compIdd = compId;
		chatAdapter = new ChatAdapter(getActivity(),companies,msg,isMe,msgDate);
		msgContainer.setAdapter(chatAdapter);
		////// inveeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee
	}

	@Override
	public void onStart() {
		super.onStart();

		//During startup, we should check if there are arguments (data)
		//passed to this Fragment. We know the layout has already been
		//applied to the Fragment so we can safely call the method that
		//sets the article text

		Bundle args = getArguments();
		if(args != null){
			//set the article based on the argument passed in
			updateArticleView(args.getInt(ARG_POSITION),Integer.parseInt(companies.get(args.getInt(ARG_POSITION))));
		}else if (currentPosition != -1){
			//set the article based on the saved instance state defined during onCreateView
			updateArticleView(currentPosition,Integer.parseInt(companies.get(currentPosition)));
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		//Save the current selection for later recreation of this Fragment
		outState.putInt(ARG_POSITION, currentPosition);
	}

}