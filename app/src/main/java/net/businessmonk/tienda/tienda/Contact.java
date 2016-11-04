package net.businessmonk.tienda.tienda;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

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

public class Contact extends AppCompatActivity implements ContactHeadlinesFragment.OnHeadlineSelectedListener{
    private EditText messageET;
    private Button sendBtn;
    @SuppressWarnings("WeakerAccess")
    private
    TinyDB tinyDB ;
    @SuppressWarnings("WeakerAccess")
    private String msgBody;
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);
        tinyDB = new TinyDB(getApplicationContext());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(0xFF99013F);
        }
        initControls();
    }
    private void initControls() {
        ListView messagesContainer = (ListView) findViewById(R.id.messagesContainer);
        messageET = (EditText) findViewById(R.id.messageEdit);
        sendBtn = (Button) findViewById(R.id.chatSendButton);
        sendBtn.setVisibility(View.GONE);
        messageET.setVisibility(View.GONE);

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(messageET.getText().toString().length()>2){
                    msgBody = messageET.getText().toString();
                    messageET.setText("");
                }
                new httpReqPost().execute();
            }
        });
    }
    @SuppressWarnings("WeakerAccess")
    private
    int position;
    @SuppressWarnings("WeakerAccess")
    private
    int compId;
    @SuppressWarnings("WeakerAccess")
    private class httpReqPost extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            String response = "";

            URL url = null;

            try {
                url = new URL(tinyDB.getString("host") + "send_message");
                Log.e("hi", url.toString());
            } catch (MalformedURLException e1) {
                e1.printStackTrace();
            }

            Log.e("route",url+"");
            HttpURLConnection conn ;

            try {
                conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setUseCaches(false);
                conn.setConnectTimeout(20000);
                conn.setReadTimeout(10000);
                conn.setDoOutput(true);
                Log.e("postinnng content",msgBody);
                Log.e("postinnng content",tinyDB.getString("uid"));
                Log.e("postinnng content",String.valueOf(compId));
                Log.e("postinnng content","futc");
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("content", msgBody)
                        .appendQueryParameter("user_id", tinyDB.getString("uid"))
                        .appendQueryParameter("company_id", String.valueOf(compId))
                        .appendQueryParameter("direction", "futc");

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
                    BufferedReader br = null;
                    try {
                        br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    while ((line = br.readLine()) != null) {
                        response += line;
                    }

                } else {
                    response = "failed";

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String strings) {
            super.onPostExecute(strings);
            Log.e("sent",strings);
            if(strings.equals("{\"result\":\"successfuly sent.\"}")){
                Toast.makeText(getApplicationContext(),"Sent Successfully ",Toast.LENGTH_SHORT).show();
                msgssFragment.msg.add(msgBody);
                msgssFragment.isMe.add("futc");
                msgssFragment.companies.add(String.valueOf(compId));
                msgssFragment.msgDate.add("24-5-2016");
                msgssFragment.chatAdapter.notifyDataSetChanged();

            }else{
                Toast.makeText(getApplicationContext(),"Check your connection ",Toast.LENGTH_SHORT).show();

            }
        }
    }
    @Override
    public void onArticleSelected(int position, int company) {
        msgssFragment.isMe = new ArrayList<>();
        msgssFragment.msgDate = new ArrayList<>();
        msgssFragment.companies = new ArrayList<>();
        msgssFragment.msg = new ArrayList<>();

        progressDialog = new ProgressDialog(Contact.this);
        progressDialog.setMessage("Loading...");
        progressDialog.setIndeterminate(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(true);
        progressDialog.show();
        new getMes().execute();
        sendBtn.setVisibility(View.VISIBLE);
        messageET.setVisibility(View.VISIBLE);
        this.position = position;
        this.compId = company;
    }

//    private void scroll() {
//        messagesContainer.setSelection(messagesContainer.getCount() - 1);
//    }

    ///////////// GET bal ///////////
    @SuppressWarnings("WeakerAccess")
    private class getMes extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            String response = "";
            //className = strings[1];
            URL url = null;
            try {
                url = new URL(tinyDB.getString("host") + "list_messages/" +tinyDB.getString("uid")+ "/"+compId);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            Log.e("liiiink",url+"");
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
            }else{
                response = "failed";
            }
            return response;

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            Log.e("hiiii", s);
            if(s=="failed"){
                Toast.makeText(getApplicationContext(),"Check internet connection",Toast.LENGTH_SHORT).show();
            }else{
                try {
                    parsing(s);
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }}
	        progressDialog.dismiss();

        }

    }
    @SuppressWarnings("WeakerAccess")
    private void parsing(String res) throws JSONException {

        JSONArray jsonArray = new JSONArray(res);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String p = jsonObject.getString("content");
            msgssFragment.msg.add(p);
            msgssFragment.isMe.add(jsonObject.getString("direction"));
            msgssFragment.companies.add(jsonObject.getString("company_id"));
            msgssFragment.msgDate.add(jsonObject.getString("created_at"));
        }
        msgssFragment articleFragment = (msgssFragment) getFragmentManager().findFragmentById(R.id.article_fragment);
//        updateArticleView(contentsFragment.posi,Integer.parseInt(contentsFragment.companies.get(contentsFragment.posi)));
        if(articleFragment != null){

            //we must be icn two pane layout
            articleFragment.updateArticleView(position,compId);

        }
    }
    public void back(View view) {
        super.onBackPressed();
    }

}
