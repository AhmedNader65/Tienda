package net.businessmonk.tienda.tienda;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
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

public class LoginActivity extends AppCompatActivity {
    private TinyDB tinyDB ;
    private EditText userNameView;
    private EditText passwordView ;
    private String userName;
    private String password;
    CoordinatorLayout loginContainer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tinyDB = new TinyDB(getApplicationContext());
        boolean login =tinyDB.getBoolean("login");
        if(login){
	        startActivity(new Intent(LoginActivity.this,HomeActivity.class));
            LoginActivity.this.finish();
        }
        setContentView(R.layout.activity_login);
        loginContainer = (CoordinatorLayout) findViewById(R.id.loginContainer);
        tinyDB.putString("host","http://hitienda.com/api/");
        userNameView = (EditText)findViewById(R.id.editText);
        passwordView = (EditText)findViewById(R.id.editText2);
    }

    public void go(View v){
        loginContainer.setVisibility(View.GONE);
        userName = userNameView.getText().toString();
        password = passwordView.getText().toString();
        new postLogin().execute();
    }

    public void signUp(View view) {
        startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
    }
    private class postLogin extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            String response = "";
            URL url = null;

            try {
                url = new URL(tinyDB.getString("host")+"login" );
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
                        .appendQueryParameter("password", password);
                Log.e("user name & password",userName+"&&"+password);
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
            return response;
        }

        @Override
        protected void onPostExecute(String strings) {
            super.onPostExecute(strings);
            Log.e("resp",strings);

            if(strings.contains("token")){
                try {
                    parsing(strings);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent(LoginActivity.this,HomeActivity.class);
	            tinyDB.putBoolean("login",true);
                startActivity(intent);
                LoginActivity.this.finish();
            }
            else {
                loginContainer.setVisibility(View.VISIBLE);
                Toast.makeText(getApplicationContext(),"check information or connection",Toast.LENGTH_LONG).show();
            }
        }

    }
    private void parsing(String res) throws JSONException {
        String[] x = new String[2];
        JSONObject jsonObject = new JSONObject(res);
        x[0]=jsonObject.getString("token");
        x[1]=jsonObject.getString("uid");

        tinyDB.putString("token",x[0]);
        tinyDB.putString("uid",x[1]);
    }
}
