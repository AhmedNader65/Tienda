package net.businessmonk.tienda.tienda;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Base64;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

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

public class GPSTracker extends Service implements GoogleApiClient.ConnectionCallbacks,
		GoogleApiClient.OnConnectionFailedListener,
		LocationListener {
	private TinyDB tinyDB;
	private static final String TAG = "LocationService";
	private double user_lat , user_lng;
	// use the websmithing defaultUploadWebsite for testing and then check your
	// location with your browser here: https://www.websmithing.com/gpstracker/displaymap.php
	private String defaultUploadWebsite;

	private boolean currentlyProcessingLocation = false;
	private static LocationRequest locationRequest;
	private static GoogleApiClient googleApiClient;

	public GoogleApiClient getGoogleApiClient() {
		return googleApiClient;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// if we are currently trying to get a location and the alarm manager has called this again,
		// no need to start processing a new location.
		if (!currentlyProcessingLocation) {
			currentlyProcessingLocation = true;
			startTracking();
		}
		Log.d(TAG, "startTracking");
		tinyDB = new TinyDB(getApplicationContext());
		return START_NOT_STICKY;
	}

	private void startTracking() {
		Log.d(TAG, "startTracking");

		if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS) {

			googleApiClient = new GoogleApiClient.Builder(this)
					.addApi(LocationServices.API)
					.addConnectionCallbacks(this)
					.addOnConnectionFailedListener(this)
					.build();

			if (!googleApiClient.isConnected() || !googleApiClient.isConnecting()) {
				googleApiClient.connect();
			}
		} else {
			Log.e(TAG, "unable to connect to google play services.");
		}
	}


	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	Handler handler =null;
	@Override
	public void onLocationChanged(final Location location) {
		if(handler==null){
			handler= new Handler();
			handler.postDelayed(new Runnable() {
				public void run() {
					// Actions to do after 10 seconds
					if (location != null) {
						user_lng = location.getLongitude();
						user_lat = location.getLatitude();
						Log.e(TAG, "position: " + user_lat+ ", " + user_lng + " accuracy: " + location.getAccuracy());
						new postCoords().execute();
						// we have our desired accuracy of 500 meters so lets quit this service,
						// onDestroy will be called and stop our location updates
//			if (location.getAccuracy() < 500.0f) {
//				stopLocationUpdates();
//			}
						handler = null;
					}
				}
			},20*1000);
		}

	}

	private void stopLocationUpdates() {
		if (googleApiClient != null && googleApiClient.isConnected()) {
			googleApiClient.disconnect();
		}
	}

	public LocationRequest getLocationRequest() {
		return locationRequest;
	}

	/**
	 * Called by Location Services when the request to connect the
	 * client finishes successfully. At this point, you can
	 * request the current location or start periodic updates
	 */
	@Override
	public void onConnected(Bundle bundle) {
		Log.d(TAG, "onConnected");

		locationRequest = LocationRequest.create();
		locationRequest.setInterval(1000); // milliseconds
		locationRequest.setFastestInterval(1000); // the fastest rate in milliseconds at which your app can handle location updates
		locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			// TODO: Consider calling
			//    ActivityCompat#requestPermissions
			// here to request the missing permissions, and then overriding
			//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
			//                                          int[] grantResults)
			// to handle the case where the user grants the permission. See the documentation
			// for ActivityCompat#requestPermissions for more details.
			return;
		}

		LocationServices.FusedLocationApi.requestLocationUpdates(
				googleApiClient, locationRequest, this);
	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		Log.e(TAG, "onConnectionFailed");

		stopLocationUpdates();
		stopSelf();
	}

	@Override
	public void onConnectionSuspended(int i) {
		Log.e(TAG, "GoogleApiClient connection has been suspend");
	}
	private class postCoords extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... strings) {
			String response = "";
			URL url = null;

			try {
				url = new URL(tinyDB.getString("host")+"submit_coords" );
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
						.appendQueryParameter("uid", tinyDB.getString("uid"))
						.appendQueryParameter("lat", String.valueOf(user_lat))
						.appendQueryParameter("lng", String.valueOf(user_lng));
				Log.e("lat & lng",user_lat+"&&"+user_lng);
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
			Log.e("notification",strings);

			if(strings.contains("location matched")){
				try {
					parsing(strings);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}

		private void parsing(String res) throws JSONException {
			String companyId ;
			JSONObject jsonObject = new JSONObject(res);
			try {
				companyId = jsonObject.getString("company_id");
				new getNotification().execute(companyId);
			}catch (Exception e){
				e.printStackTrace();
			}
		}
	}


	///////////// GET notification ///////////
	public class getNotification extends AsyncTask<String, Void, String> {


		//String className;
		@Override
		protected String doInBackground(String... strings) {
			String response = "";
			//className = strings[1];
			URL url = null;
			try {
				url = new URL(tinyDB.getString("host") + "promotion_of_company/" +strings[0]);
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
			try {
				parsing(s);
			} catch (JSONException e) {
				e.printStackTrace();
			}

		}
		private void parsing(String res) throws JSONException {
			JSONObject jsonObject = new JSONObject(res);
			try {
				notify(jsonObject.getString("head"),jsonObject.getString("body"),decode64(jsonObject.getString("image")),jsonObject.getString("image"));
			}catch (Exception e){
				e.printStackTrace();
			}
		}
		private void notify(String title, String msg, Bitmap img,String encoded_img){
			NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
			NotificationCompat.Builder  mBuilder = new NotificationCompat.Builder(getBaseContext());
			Intent intent = new Intent(getApplicationContext(), promotions_selected.class);
			intent.putExtra("head",title);
			intent.putExtra("body",msg);
			intent.putExtra("img",encoded_img);

			PendingIntent contentIntent =
					PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
			mBuilder.setContentTitle(title);
			mBuilder.setContentText(msg);
			mBuilder.setTicker(msg);
			mBuilder.setDefaults(Notification.DEFAULT_SOUND);
			mBuilder.setSmallIcon(R.drawable.coin);
			mBuilder.setLargeIcon(img);
			mBuilder.setContentIntent(contentIntent);
			mBuilder.setAutoCancel(true);
			notificationManager.notify(0, mBuilder.build());

		}
		public Bitmap decode64(String hash){
			hash = hash.substring(22);
			byte[] decodedString = Base64.decode(hash, 0);
			return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
		}
	}

}
