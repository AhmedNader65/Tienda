package net.businessmonk.tienda.tienda;

/**
 * Created by ahmed on 08/06/16.
 */
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

class ListAdapter extends ArrayAdapter {
    private final ArrayList<String> mPoints;
    private final ArrayList<String> mType;
    private final ArrayList<String> mLogo;
    private final ArrayList<String> mFromWhat_text;
    private final ArrayList<String> mFromType;
    private final ArrayList<String> mCompanies;
    private final LayoutInflater inflater;
    TinyDB tinyDB;
    private final Activity mContext;
    private ArrayList<String> survey_line_id;
    private ArrayList<String> criteria_value;
    ArrayList<String> criteria_list;
    ArrayList<TextView> SurveyTextValues;
    public ListAdapter(Activity context,ArrayList points,ArrayList type,ArrayList logo,ArrayList frmType,ArrayList fromWhat_text,ArrayList companies) {
        super(context, 0);
        this.mType = type;
        this.mPoints = points;
        this.mLogo=logo;
        this.mFromWhat_text = fromWhat_text;
        this.mFromType = frmType;
        this.mCompanies = companies;
        mContext = context;
        tinyDB = new TinyDB(mContext);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    @Override
    public int getCount() {
        return mPoints.size();
    }

    @Override
    public View getView(final int position, final View convertView, ViewGroup parent) {
        LayoutInflater inflater = mContext.getLayoutInflater();
        @SuppressLint("InflateParams") final View v = inflater.inflate(R.layout.history_list_item,null,true);
            TextView point = (TextView) v.findViewById(R.id.points_history);
            TextView frmText1 = (TextView) v.findViewById(R.id.fromwhat_text1_history);
            TextView frmText2 = (TextView) v.findViewById(R.id.fromwhat_text2_history);
            ImageView logo = (ImageView)v.findViewById(R.id.logo_history);
            ImageView type = (ImageView)v.findViewById(R.id.type_history);
            ImageView frmLogo = (ImageView)v.findViewById(R.id.fromwhat_logo_history);
            point.setText(mPoints.get(position));
        String x = mFromType.get(position);
        final String text = mFromWhat_text.get(position);
        Log.e("From text  "+position+"",text);
        switch (x) {
            case "invitation":
                frmText2.setText(text.substring(text.length() - 11));
                frmText1.setText(text.substring(0, text.length() - 11));
                frmLogo.setImageDrawable(mContext.getResources().getDrawable(R.drawable.invited_ico));
                break;
            case "buy":
                frmLogo.setImageDrawable(mContext.getResources().getDrawable(R.drawable.surv_ico));
                frmText1.setText(text);
                frmText2.setVisibility(View.GONE);
                frmLogo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new getSurvey().execute(mCompanies.get(position));
                    }
                });
                break;
            default:
                frmText1.setVisibility(View.GONE);
                frmText2.setVisibility(View.GONE);
                break;
        }
        Picasso.with(mContext).load("http://hitienda.com/"+mLogo.get(position)).into(logo);
//        logo.setImageBitmap(decode64(mLogo.get(position)));
        Log.e(" type "+position+"", mType.get(position));
        Log.e("From type "+position+"",x);



            point.setText(mPoints.get(position));

        if(mType.get(position).equals("earned")){
            type.setImageDrawable(mContext.getResources().getDrawable(R.drawable.added_ico));
        }else{
            type.setImageDrawable(mContext.getResources().getDrawable(R.drawable.removed_ico));
        }
        return v;
    }
    private Bitmap decode64(String hash){
        hash = hash.substring(22);
        byte[] decodedString = Base64.decode(hash, 0);
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
    }

    private LinearLayout createView(String name){
        LinearLayout linear=new LinearLayout(mContext);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 20, 0, 0);
        linear.setLayoutParams(params);
        linear.setOrientation(LinearLayout.HORIZONTAL);
        TextView text=new TextView(mContext);
        text.setText(name);
        final TextView text2=new TextView(mContext);
        text2.setText("3");

        linear.setWeightSum(5);

        SeekBar seek=new SeekBar(mContext);
        seek.setMax(5);
        seek.setProgress(3);
        text.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.5f));
        text2.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, .5f));
        seek.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 3f));
        linear.addView(text);
        linear.addView(seek);
        linear.addView(text2);
        SurveyTextValues.add(text2);
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                text2.setText(i+"");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        return linear;
    }
    private class getSurvey extends AsyncTask<String,Void,String>{
        ProgressDialog progDailog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            try {
                progDailog = new ProgressDialog(mContext.getApplicationContext());
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
                url = new URL(tinyDB.getString("host") + "company_survey/" +strings[0]);
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
            progDailog.dismiss();
            try {
                JSONArray jsonArray = new JSONArray(s);
                JSONObject jsonObject = jsonArray.getJSONObject(0);
                JSONObject survey = jsonObject.getJSONObject("survey");
                String survey_name = survey.getString("name");
                criteria_list = new ArrayList<>();
                survey_line_id = new ArrayList<>();
                for(int i = 0 ;i<jsonArray.length();i++){
                    JSONObject jsonObject2 = jsonArray.getJSONObject(i);
                    survey_line_id.add(jsonObject2.getString("id"));
                    JSONObject criteria = jsonObject2.getJSONObject("criteria");
                    criteria_list.add(criteria.getString("name"));
                }
                Log.e("criteria_id",survey_line_id.size()+"");
                final AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
                SurveyTextValues = new ArrayList<>();
                criteria_value = new ArrayList<>();
                alert.setTitle(survey_name);
                alert.setMessage("How satisfied are you with the following:");
                final LinearLayout root_linear=new LinearLayout(mContext);
                root_linear.setPadding(20,20,20,20);
                root_linear.setOrientation(LinearLayout.VERTICAL);
                for(int i = 0 ; i <criteria_list.size();i++)
                    root_linear.addView(createView(criteria_list.get(i)));
                alert.setView(root_linear);



                alert.setPositiveButton("Ok",new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog,int id)
                    {
                        for(int i = 0 ; i <criteria_list.size();i++) {
                            String x = SurveyTextValues.get(i).getText().toString();
                            criteria_value.add(x);
                        }
                        try {
                           new SubmitSurvey().execute(createJson());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                });

                alert.setNegativeButton("Cancel",new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog,int id)
                    {
                        Toast.makeText(mContext, "Cancel Pressed", Toast.LENGTH_LONG).show();
                    }
                });

                alert.show();
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    private class SubmitSurvey extends AsyncTask<String,Void,Void>{

        protected Void doInBackground(String... strings) {
            String response = "";
            URL url = null;

            try {
                url = new URL(tinyDB.getString("host")+"submit_survey" );
                Log.e("hi", url.toString());
            } catch (MalformedURLException e1) {
                e1.printStackTrace();
            }


            HttpURLConnection conn ;

            try {
                conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setUseCaches(false);
                conn.setConnectTimeout(20000);
                conn.setReadTimeout(10000);
                conn.setDoOutput(true);
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("survey_submission", strings[0]);
                String query = builder.build().getEncodedQuery();
                OutputStream os = conn.getOutputStream();
                BufferedWriter writer ;
                writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));

                writer.write(query);
                writer.flush();
                writer.close();
                os.close();
                int responseCode ;
                responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    String line;
                    BufferedReader br ;
                    br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                    while ((line = br.readLine()) != null) {
                        response += line;
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Toast.makeText(mContext,"Thank you !",Toast.LENGTH_SHORT).show();
        }
    }
    private String createJson() throws JSONException {
        JSONObject rootObj = new JSONObject();
        rootObj.put("uid",tinyDB.getString("uid"));
        JSONArray jsonArray = new JSONArray();
        for(int i = 0 ; i < survey_line_id.size();i++){
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("survey_line_id",survey_line_id.get(i));
            jsonObject.put("value",criteria_value.get(i));
            jsonArray.put(jsonObject);
        }
        rootObj.put("survey",jsonArray);
        Log.e("JSON",rootObj.toString());
        return rootObj.toString();
    }
}