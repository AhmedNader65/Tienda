package net.businessmonk.tienda.tienda;

import android.app.Fragment;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by learnovate on 3/30/14.
 */
public class contentsFragment extends Fragment {

	private final static String ARG_POSITION = "position";
	private int currentPosition = -1;
	private static TextView pointsView;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		ArrayList<String> points = new ArrayList<>();
		ArrayList<String> companies = new ArrayList<>();
		TinyDB tinyDB = new TinyDB(getActivity().getApplicationContext());

		if(savedInstanceState != null){
			//if we recreated this Fragment (for instance from a screen rotate)
			//restore the previous article selection by getting it here
			currentPosition = savedInstanceState.getInt(ARG_POSITION);
		}
		int posi = getActivity().getIntent().getIntExtra("position", 0);

		//inflate the view for this fragment
		View myFragmentView = inflater.inflate(R.layout.content_balance,container,false);
		 pointsView = (TextView) myFragmentView.findViewById(R.id.pointsView);
		ArrayList<String> gifts = new ArrayList<>();
//		gifts.add("hii");
//		gifts.add("hii");
//		gifts.add("hii");
		int l = allBalance.companyListArrayList.get(posi).getGifts_num();
		for(int j = 0 ; j<l;j++){
			gifts.add(allBalance.companyListArrayList.get(posi).getGifts(j));
		}
		for(int i = 0 ; i < gifts.size();i++) {
			LinearLayout gift = (LinearLayout) myFragmentView.findViewById(R.id.gifts);
			TextView txt1 = new TextView(getActivity().getApplicationContext());
			txt1.setText("* "+gifts.get(i));
			txt1.setId(i);
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			params.gravity = Gravity.CENTER_HORIZONTAL;
			txt1.setLayoutParams(params);
			gift.addView(txt1);
		}
		return myFragmentView;

	}

	public void updateArticleView(int position,int compId){
		currentPosition = position;
		int compIdd = compId;
		try {
			pointsView.setText(allBalance.companyListArrayList.get(position).getPoints());
		}catch (Exception e){
			pointsView.setText("0");

		}
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
			updateArticleView(args.getInt(ARG_POSITION),Balance.companyyy);
		}else if (currentPosition != -1){
			//set the article based on the saved instance state defined during onCreateView
			updateArticleView(currentPosition,Balance.companyyy);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		//Save the current selection for later recreation of this Fragment
		outState.putInt(ARG_POSITION, currentPosition);
	}


}