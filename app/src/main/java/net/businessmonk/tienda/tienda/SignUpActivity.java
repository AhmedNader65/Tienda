package net.businessmonk.tienda.tienda;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class SignUpActivity extends AppCompatActivity {
    private TinyDB tinyDB;
    private EditText userNameView,invitedByView, passwordView, passwordConfirmView, firstNameView, lastNameView, mobileView, phoneView,emailView;
    private String userName,invitedBy, password,firstName,lastName,mobile,email;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        tinyDB = new TinyDB(getApplicationContext());
        userNameView = (EditText)findViewById(R.id.editText3);
        firstNameView = (EditText)findViewById(R.id.editText4);
        lastNameView = (EditText)findViewById(R.id.editText5);
        phoneView = (EditText)findViewById(R.id.editText6);
        mobileView = (EditText)findViewById(R.id.editText7);
        emailView = (EditText)findViewById(R.id.editText8);
        passwordView = (EditText)findViewById(R.id.editText9);
        passwordConfirmView = (EditText)findViewById(R.id.editText10);
        invitedByView = (EditText)findViewById(R.id.editText10);



//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
    }

    public void signNow(View view) {
        userName = userNameView.getText().toString();
        firstName = firstNameView.getText().toString();
        lastName = lastNameView.getText().toString();
        String phone = phoneView.getText().toString();
        mobile= mobileView.getText().toString();
        if(invitedByView.getText().toString()!=null) {
            invitedBy = invitedByView.getText().toString();
        }
        email= emailView.getText().toString();
        if(passwordConfirmView.getText().toString().equals(passwordView.getText().toString())){
            password = passwordConfirmView.getText().toString();
        }
        if(userName!=null&&password!=null&&email!=null){
            new postSignUp().execute();
        }else {
            Toast.makeText(getApplicationContext(),"Fill required data",Toast.LENGTH_SHORT).show();
        }
    }

    private class postSignUp extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {

            String response = "";

            URL url = null;

            try {
                url = new URL(tinyDB.getString("host") + "signup");
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
                        .appendQueryParameter("username", userName)
                        .appendQueryParameter("password", password)
                        .appendQueryParameter("first_name", firstName)
                        .appendQueryParameter("last_name", lastName)
                        .appendQueryParameter("email", email)
                        .appendQueryParameter("mobile_number", mobile)
                        .appendQueryParameter("phone_number", mobile);
                if(invitedBy!=null){
                    builder.appendQueryParameter("parent_id",invitedBy);
                }
                Log.e("first_name_test", firstName);
                Log.e("first_name_test", lastName);

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

                } else {
                    response = "";

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String strings) {
            super.onPostExecute(strings);
            Log.e("resp", strings);
            if (strings != "") {
                try {
                    tinyDB.putString(parsing(strings), "token");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(getApplicationContext(), "wrong", Toast.LENGTH_LONG).show();
            }

        }

    }
    private String parsing(String res) throws JSONException {

        JSONObject jsonObject = new JSONObject(res);
        return jsonObject.getString("msg");


    }
}
