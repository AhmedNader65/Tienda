package net.businessmonk.tienda.tienda;

import android.app.Activity;
import android.app.Fragment;
import android.app.ListFragment;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class PromoHeadlinesFragment extends ListFragment {

	private OnHeadlineSelectedListener callback;
	private TinyDB tinyDB;

	public interface OnHeadlineSelectedListener{
		void onArticleSelected(int position, int company);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		//Make sure that the container Activity has implemented
		//the interface. if not, throw an exception so we can fix it
		try{
			callback = (OnHeadlineSelectedListener) activity;
		}catch(ClassCastException e ){
			throw new ClassCastException(activity.toString() + "must implement OnHeadlineSelectedListener");
		}

	}
	public static ArrayList<String> logo;
	private ArrayList<String> companies;
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		tinyDB = new TinyDB(getActivity().getApplicationContext());
		 logo = new ArrayList<>();
		 companies = new ArrayList<>();
		new getBal().execute();

	}

	@Override
	public void onStart() {
		super.onStart();

		//When in a two-pane layout, set the lightview to highlight the list item
		//instead of just simply blinking

		Fragment f = getFragmentManager().findFragmentById(R.id.article_fragment);
		ListView v = getListView();
		if(f != null && v != null){
			v.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		}
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		int companyIdClicked = Integer.parseInt(companies.get(position));
		//Notify the parent of the selected item
		callback.onArticleSelected(position, companyIdClicked);

		//again set the item to be highlighted in a two-pane layout
		l.setItemChecked(position,true);

	}
	private class customAdapter extends ArrayAdapter{
		 final ArrayList<String> mLogo;
		final Activity mContext;
		public customAdapter(Activity context, ArrayList<String> logo) {
			super(context, R.layout.headline_fragment);
			this.mLogo = logo;
			this.mContext = context;
			Log.e("hi","gfgfgf");
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = mContext.getLayoutInflater();
			Log.e("hi","gfgfg32f");

			View v =inflater.inflate(R.layout.headline_fragment,parent,false);
			ImageView img = (ImageView)v.findViewById(R.id.com_logo);
			Picasso.with(mContext).load("http://hitienda.com/"+mLogo.get(position)).into(img);

//			img.setImageBitmap(decode64(mLogo.get(position)));
			return  v;
		}
		@Override
		public int getCount() {
			return mLogo.size();
		}
	}
	///////////// GET bal ///////////
	public class getBal extends AsyncTask<String, Void, String> {
		ProgressDialog progDailog;
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			try {
//			progDailog = new ProgressDialog(getActivity());
//			progDailog.setMessage("Loading...");
//			progDailog.setIndeterminate(false);
//			progDailog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//			progDailog.setCancelable(true);
//			progDailog.show();
		}catch (Exception e){
				e.printStackTrace();
			}
		}

		//String className;
		@Override
		protected String doInBackground(String... strings) {
			String response = "";
			//className = strings[1];
			URL url = null;
			try {
				url = new URL(tinyDB.getString("host") + "balance/" +tinyDB.getString("uid"));
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}


			HttpURLConnection conn = null;
			try {
				conn = (HttpURLConnection) url.openConnection();

				conn.setRequestMethod("GET");
				conn.setDoInput(true);
				conn.setUseCaches(false);
				conn.setRequestMethod("GET");
				conn.setConnectTimeout(20000);
				conn.addRequestProperty("Accept", "application/json");
				OutputStream os = conn.getOutputStream();
				OutputStreamWriter wr = new OutputStreamWriter(os);
				wr.flush();
			} catch (Exception e) {
				e.printStackTrace();
			}
			int responseCode = 0;
			try {
				responseCode = conn.getResponseCode();
			} catch (IOException e1) {
				e1.printStackTrace();
			}


			if (responseCode == HttpURLConnection.HTTP_OK) {
				String line;
				BufferedReader br = null;
				try {
					br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				} catch (IOException e1) {
					e1.printStackTrace();
				}

				try {
					while ((line = br.readLine()) != null) {
						response += line;
					}
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			} else {
				response = "failed";

			}
			return response;

		}

		@Override
		protected void onPostExecute(String s) {
			super.onPostExecute(s);
			if(progDailog != null) {
//				progDailog.dismiss();
			}
			Log.e("hiiii", s);
			if(s=="failed"){
				Toast.makeText(getActivity().getApplicationContext(),"Check internet connection",Toast.LENGTH_SHORT).show();
			}else{
				try {
					parsing(s);
				} catch (JSONException e1) {
					e1.printStackTrace();
				}}
		}

	}
	private void parsing(String res) throws JSONException {
		Log.e("jsonString", res);
		Log.e("finished", "parse start");
		JSONObject all = new JSONObject(res);
		JSONArray jsonArray = all.getJSONArray("balances");
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject jsonObject = jsonArray.getJSONObject(i);
			JSONObject jsonObject2 = jsonObject.getJSONObject("company");
			String mLogo = jsonObject2.getString("logo_url");
			logo.add(mLogo);
			companies.add(jsonObject.getString("company_id"));
		}
		setListAdapter(new customAdapter(getActivity(),logo));

	}

}