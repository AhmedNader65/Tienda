package net.businessmonk.tienda.tienda;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

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

public class History extends AppCompatActivity {
	private ArrayList<String> points;
	private ArrayList<String> type;
	private ArrayList<String> logo;
	private ArrayList<String> frmType;
	private ArrayList<String> frmWhat_text;
	private ArrayList<String> companies_id;
	private ListView listView;
	private TinyDB tinyDB;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_history);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			getWindow().setStatusBarColor(0xFF004B00);
		}

		tinyDB = new TinyDB(getApplicationContext());
		points = new ArrayList<>();
		logo = new ArrayList<>();
		type = new ArrayList<>();
		companies_id = new ArrayList<>();
		frmType = new ArrayList<>();
		frmWhat_text = new ArrayList<>();
		listView = (ListView)findViewById(R.id.history_list);
		ImageView back = (ImageView)findViewById(R.id.back_Btn);
		new getHist().execute();
		back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				back();
			}
		});
	}
	///////////// GET hist ///////////
	public class getHist extends AsyncTask<String, Void, String> {
		ProgressDialog progDailog;
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			try {
				progDailog = new ProgressDialog(History.this);
				progDailog.setMessage("Loading...");
				progDailog.setIndeterminate(false);
				progDailog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				progDailog.setCancelable(true);
				progDailog.show();
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
				url = new URL(tinyDB.getString("host") + "user_history/" +tinyDB.getString("uid"));
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
				progDailog.dismiss();
			}
			Log.e("hiiii", s);
			if(s=="failed"){
				Toast.makeText(getApplicationContext(),"Check internet connection",Toast.LENGTH_SHORT).show();
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

		JSONArray jsonArray = new JSONArray(res);
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject jsonObject = jsonArray.getJSONObject(i);
			String mType = jsonObject.getString("tybe");
			type.add(mType);
			companies_id.add(jsonObject.getString("company_id"));
			points.add(jsonObject.getString("value"));
			if (mType.equals("earned")){
				if(jsonObject.getString("from_user_id") =="null"){
					frmType.add("buy");
					frmWhat_text.add("Do a survey");
				}else{
					frmType.add("invitation");
					JSONObject jsonObject2 = jsonObject.getJSONObject("from_user");
					String fName = jsonObject2.getString("first_name");
					String lName = jsonObject2.getString("last_name");
					frmWhat_text.add(fName+" "+lName+" was invited");

				}

			}else{
				frmType.add("spend");
				frmWhat_text.add("null");
			}
			JSONObject jsonObject3 = jsonObject.getJSONObject("company");
			String mLogo = jsonObject3.getString("logo_url");
			logo.add(mLogo);

		}
		ListAdapter adapter = new ListAdapter(History.this, points, type, logo, frmType, frmWhat_text,companies_id);
		listView.setAdapter(adapter);
	}
	private void back(){
		super.onBackPressed();
	}
}
