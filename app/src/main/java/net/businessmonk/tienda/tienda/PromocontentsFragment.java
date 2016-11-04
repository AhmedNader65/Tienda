package net.businessmonk.tienda.tienda;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by learnovate on 3/30/14.
 */
public class PromocontentsFragment extends Fragment {

	private ArrayList<String> products_name;
	private ArrayList<String> products_left_time;
	private ArrayList<String> companyId;
	private ArrayList<String> body;
	private ArrayList<String> ImageSrc;
	private ListView listView ;
	private TinyDB tinyDB ;
	private final static String ARG_POSITION = "position";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


		if(savedInstanceState != null){
			//if we recreated this Fragment (for instance from a screen rotate)
			//restore the previous article selection by getting it here
			int currentPosition = savedInstanceState.getInt(ARG_POSITION);
		}
		//inflate the view for this fragment
		View myFragmentView = inflater.inflate(R.layout.article_fragment,container,false);
		listView= (ListView)myFragmentView.findViewById(R.id.promotion_list);
		return myFragmentView;

	}
	private ProgressDialog progressDialog;
	private int company;
	public void updateArticleView(int position, int cmp){
		products_name = new ArrayList<>();
		products_left_time = new ArrayList<>();
		companyId = new ArrayList<>();
		body = new ArrayList<>();
		ImageSrc = new ArrayList<>();
		tinyDB = new TinyDB(getActivity().getApplicationContext());
		this.company = cmp;

		progressDialog = new ProgressDialog(getActivity());
		progressDialog.setMessage("Loading...");
		progressDialog.setIndeterminate(false);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setCancelable(true);
		progressDialog.show();
		new getPromotion().execute();
	}

	private class getPromotion extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... strings) {
			String response = "";
			URL url = null;
			try {
				url = new URL(tinyDB.getString("host")+"promotions");
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

			}catch (Exception e){
				e.printStackTrace();
			}
			int responseCode =0;
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
		protected void onPostExecute(String aVoid) {
			super.onPostExecute(aVoid);
			if(aVoid=="failed"){
				Toast.makeText(getActivity().getApplicationContext(),"Check internet connection",Toast.LENGTH_SHORT).show();
			}else{
				try {
					getDataFromJson(aVoid);
				} catch (JSONException e1) {
					e1.printStackTrace();
				}}

		}
	}
	private class customPromotionAdapter extends ArrayAdapter {
		final ArrayList<String> mProducts_name;
		final ArrayList<String> mProducts_left_time;
		final ArrayList<String> body;
		final ArrayList<String> image;
		final Activity mContext ;
		final ArrayList<String> products_filered =new ArrayList<>();
		private int poo;

		public customPromotionAdapter(Activity context, ArrayList x, ArrayList z, ArrayList xx, ArrayList zz) {
			super(context, R.layout.promotion_list_item);
			this.mProducts_left_time = z;
			this.mProducts_name = x;
			this.mContext = context;
			this.body = xx;
			this.image = zz;
			for (int i = 0; i < companyId.size(); i++) {

				Log.e("com",company+"");
				Log.e("com2",companyId.get(i)+"");
				if (companyId.get(i).equals(String.valueOf(company))) {
					products_filered.add(products_name.get(i));
				}
			}
		}
		@Override
		public int getCount() {
			return products_filered.size();
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {

			if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
				// for newer than kitkat
			}else{
				// kitkat or less
			}
			poo = position;
			for(int i =0 ; i <mProducts_name.size();i++){
				if(products_filered.get(position).equals(mProducts_name.get(i))){
					poo = i;
				}
			}
			LayoutInflater inflater=mContext.getLayoutInflater();
			@SuppressLint("InflateParams") View v=inflater.inflate(R.layout.promotion_list_item, null,true);
			TextView pro = (TextView)v.findViewById(R.id.product_text);
				pro.setText(mProducts_name.get(poo));

				v.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						startActivity(new Intent(getActivity(), promotions_selected.class).putExtra("body", body.get(poo))
								.putExtra("head", products_name.get(poo)).putExtra("img", ImageSrc.get(poo)));

//                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(Emergencies.this);
//                    alertDialog.setTitle("About offer");
//                    alertDialog.setMessage("Address : "+hospital_address+"\n"+
//                            "Phone : "+hospital_phone+"\n"+
//                            "Start time : "+hospital_startTime+"\n"+
//                            "End time : "+hospital_endTime);
//                    alertDialog.setPositiveButton("LOCATION", new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int id) {
//                            Intent intent  = new Intent(Emergencies.this,MapsActivity.class);
//                            intent.putExtra("lat",lat);
//                            intent.putExtra("lng",lng);
//                            intent.putExtra("name",hospital_name);
//                            intent.putExtra("address",hospital_address);
//                            intent.putExtra("phone",hospital_phone);
//                            intent.putExtra("startTime",hospital_startTime);
//                            intent.putExtra("endTime",hospital_endTime);
//                            startActivity(intent);
//                        }
//                    });
//                    alertDialog.setNegativeButton("OK", new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int id) {
//                            dialog.cancel();
//                        }
//                    });
//                    alertDialog.show();
					}
				});

			return v;
		}
	}
	private void getDataFromJson(String s) throws JSONException {


		JSONArray jsonArray = new JSONArray(s);
		Log.e("json2",jsonArray.length()+"");
		for(int i =0 ; i<jsonArray.length() ; i++)
		{
			JSONObject jsonObject = jsonArray.getJSONObject(i);
			products_name.add(jsonObject.getString("head"));
			companyId.add(jsonObject.getString("company_id"));
			body.add(jsonObject.getString("body"));
			ImageSrc.add(jsonObject.getString("image_url"));
			products_left_time.add(jsonObject.getString("end_date").substring(0,10));
		}

		customPromotionAdapter adapter = new customPromotionAdapter(getActivity(), products_name, products_left_time, body, ImageSrc);
		listView.setAdapter(adapter);
		progressDialog.dismiss();
		Log.e("hiiii","hiiiii");

	}
}