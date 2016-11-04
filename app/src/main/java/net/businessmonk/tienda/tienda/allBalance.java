package net.businessmonk.tienda.tienda;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
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

public class allBalance extends AppCompatActivity {
    private ListView list ;
    public static ArrayList<CompanyList> companyListArrayList ;
    private TinyDB tinyDB ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_balance);
        list = (ListView)findViewById(R.id.allBalanceList);
        tinyDB  = new TinyDB(getApplicationContext());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(0xFF9E7100);
        }
        new getAllBal().execute();
    }
    private class customAdapter extends ArrayAdapter{
        final ArrayList<CompanyList> company;
        final Activity mContext;
        public customAdapter(Activity context, ArrayList<CompanyList> company) {
            super(context, 0);
            this.company = company;
            mContext = context;
        }

        @Override
        public int getCount() {
            return company.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = mContext.getLayoutInflater();
            @SuppressLint("InflateParams") View v = inflater.inflate(R.layout.allbalance_list_item,null,true);
            ImageView logoView = (ImageView)v.findViewById(R.id.com_logo);
            TextView pointsView =(TextView)v.findViewById(R.id.points);
            TextView giftsView =(TextView)v.findViewById(R.id.gifts);
            TextView promosView =(TextView)v.findViewById(R.id.promos);
            Picasso.with(mContext).load("http://hitienda.com/"+company.get(position).getLogo()).into(logoView);

//            logoView.setImageBitmap(decode64(company.get(position).getLogo()));
            pointsView.setText(company.get(position).getPoints());
            giftsView.setText(String.valueOf(company.get(position).getGifts_num()));
            promosView.setText(company.get(position).getPromotions());
            return v;
        }
    }
    ///////////// GET bal ///////////
    public class getAllBal extends AsyncTask<String, Void, String> {
        ProgressDialog progDailog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            try {

                Log.v("hiiii","Gfdgdf");
                progDailog = new ProgressDialog(allBalance.this);
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
        companyListArrayList = new ArrayList<>();
        JSONObject rootObject = new JSONObject(res);
        JSONArray balance_Array = rootObject.getJSONArray("balances");
        for (int i = 0; i < balance_Array.length(); i++) {
            JSONObject jsonObject = balance_Array.getJSONObject(i);
            CompanyList newCompany = new CompanyList();
            newCompany.setPoints(jsonObject.getString("value"));
            String company_id = jsonObject.getString("company_id");
            newCompany.setId(company_id);
            newCompany.setPromotions("4");
            JSONObject jsonObject2 = jsonObject.getJSONObject("company");
            newCompany.setLogo(jsonObject2.getString("logo_url"));
            JSONArray gifts_Array = rootObject.getJSONArray("gifts");
            for(int j = 0;j<gifts_Array.length();j++){
                JSONObject jsonObject3 = gifts_Array.getJSONObject(j);
                if(jsonObject3.getString("company_id").equals(company_id)){
                    newCompany.setGifts_num(1);
                    newCompany.setGifts(jsonObject3.getString("name"));
                }else{
                    continue;
                }
            }
            companyListArrayList.add(newCompany);
        }


        list.setAdapter(new customAdapter(this,companyListArrayList));
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(allBalance.this,Balance.class);
                intent.putExtra("position",i);
                intent.putExtra("company_id",companyListArrayList.get(i).getId());
                startActivity(intent);

            }
        });
    }
    private Bitmap decode64(String hash){
        hash = hash.substring(22);
        byte[] decodedString = Base64.decode(hash, 0);
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
    }
    public void back(View view) {
        super.onBackPressed();
    }

}
