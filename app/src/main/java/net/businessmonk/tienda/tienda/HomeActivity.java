package net.businessmonk.tienda.tienda;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

public class HomeActivity extends AppCompatActivity {
    public static final String MIME_TEXT_PLAIN = "text/plain";
    public static final String TAG = "NfcDemo";
    private NfcAdapter mNfcAdapter;
    private TinyDB tinyDB;
    private final Paint paint = new Paint();

    private float lockIconX = 0;
    private float lockIconY = 0;
    private int screenWidth;
    private int screenHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startService(new Intent(HomeActivity.this, GPSTracker.class));
        tinyDB = new TinyDB(getApplicationContext());
        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
        appReview.app_launched(this);
        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            buildAlertMessageNoGps();
        }
        startService(new Intent(HomeActivity.this,GPSTracker.class));

        WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();

        screenWidth = display.getWidth();
        screenHeight = display.getHeight();
        try {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
            );
        }catch (Exception e){
            e.printStackTrace();
        }
        DrawingView dv = new DrawingView(HomeActivity.this);

        initLockIconPosition();
        setContentView(dv);
        ///////////////////////////// NFC ///////////////////
        mNfcAdapter = NfcAdapter.getDefaultAdapter(HomeActivity.this);

        if (mNfcAdapter == null) {
            // Stop here, we definitely need NFC
            Toast.makeText(this, "This device doesn't support NFC.", Toast.LENGTH_LONG).show();
            return;

        }

        if (!mNfcAdapter.isEnabled()) {
//            mTextView.setText("NFC is disabled.");
            Toast.makeText(this, "NFC is disabled. turn it on to get your gift!", Toast.LENGTH_LONG).show();

        } else {
//            mTextView.setText(R.string.explanation);
            Toast.makeText(this, "Now checking for your gift stay tuned!", Toast.LENGTH_LONG).show();
        }

        handleIntent(getIntent());

    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.v("resumed","on resumeee");
        DrawingView dv = new DrawingView(HomeActivity.this);
        setContentView(dv);
    }

    private void handleIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {

            String type = intent.getType();
            if (MIME_TEXT_PLAIN.equals(type)) {

                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                new NdefReaderTask().execute(tag);

            } else {
                Log.d(TAG, "Wrong mime type: " + type);
            }
        } else if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {

            // In case we would still use the Tech Discovered Intent
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            String[] techList = tag.getTechList();
            String searchedTech = Ndef.class.getName();

            for (String tech : techList) {
                if (searchedTech.equals(tech)) {
                    new NdefReaderTask().execute(tag);
                    break;
                }
            }
        }
    }
//    public static void setupForegroundDispatch(final Activity activity, NfcAdapter adapter) {
//        final Intent intent = new Intent(activity.getApplicationContext(), activity.getClass());
//        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//
//        final PendingIntent pendingIntent = PendingIntent.getActivity(activity.getApplicationContext(), 0, intent, 0);
//
//        IntentFilter[] filters = new IntentFilter[1];
//        String[][] techList = new String[][]{};
//
//        // Notice that this is the same filter as in our manifest.
//        filters[0] = new IntentFilter();
//        filters[0].addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
//        filters[0].addCategory(Intent.CATEGORY_DEFAULT);
//        try {
//            filters[0].addDataType(MIME_TEXT_PLAIN);
//        } catch (IntentFilter.MalformedMimeTypeException e) {
//            throw new RuntimeException("Check your mime type.");
//        }
//
//        adapter.enableForegroundDispatch(activity, pendingIntent, filters, techList);
//    }

    private class NdefReaderTask extends AsyncTask<Tag, Void, String> {

        @Override
        protected String doInBackground(Tag... params) {
            Tag tag = params[0];

            Ndef ndef = Ndef.get(tag);
            if (ndef == null) {
                // NDEF is not supported by this Tag.
                return null;
            }

            NdefMessage ndefMessage = ndef.getCachedNdefMessage();

            NdefRecord[] records = ndefMessage.getRecords();
            for (NdefRecord ndefRecord : records) {
                if (ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
                    try {
                        return readText(ndefRecord);
                    } catch (UnsupportedEncodingException e) {
                        Log.e(TAG, "Unsupported Encoding", e);
                    }
                }
            }

            return null;
        }

        private String readText(NdefRecord record) throws UnsupportedEncodingException {
        /*
         * See NFC forum specification for "Text Record Type Definition" at 3.2.1
         *
         * http://www.nfc-forum.org/specs/
         *
         * bit_7 defines encoding
         * bit_6 reserved for future use, must be 0
         * bit_5..0 length of IANA language code
         */

            byte[] payload = record.getPayload();

            // Get the Text Encoding
            String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";

            // Get the Language Code
            int languageCodeLength = payload[0] & 0063;

            // String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
            // e.g. "en"

            // Get the Text
            return new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
        }





        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
//                Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
	            Log.v(TAG,result);

//                mTextView.setText("Read content: " + result);
//               Toast.makeText(getApplicationContext(),"hi",Toast.LENGTH_SHORT).show();
	            new CheckForGift().execute(result);
            }
        }
    }


    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }
    private void initLockIconPosition() {
        int red = getResources().getDrawable(R.mipmap.lock_ico).getIntrinsicWidth();

        lockIconX = (screenWidth -red) / 2;
        lockIconY = ((screenHeight - red) / 2) + ((screenHeight - 64) / 5);
    }

    private void drawIcon(Canvas canvas, int ico, float left, float top) {

        Drawable drawable = getResources().getDrawable(ico);
        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();

        canvas.drawBitmap(bitmap, left, top, paint);
    }

    class DrawingView extends View {
        final int iconRadius =  getResources().getDrawable(R.mipmap.info_ico).getIntrinsicWidth()/2;
        float x = 0, y = 0;
        final int ratio = (screenWidth/6);
        float balanceIconX = (ratio/2)- iconRadius, balanceIconY = 170 - iconRadius;
        float historyIconX = ((ratio)+(ratio/2)) - iconRadius, historyIconY = 340 - iconRadius;
        float promotionsIconX = ((ratio*2)+(ratio/2)) - iconRadius, promotionsIconY = 180 - iconRadius;
        float contactIconX = ((ratio*3)+(ratio/2)) - iconRadius, contactIconY = 390 - iconRadius;
        float settingIconX = ((ratio*4)+(ratio/2)) - iconRadius, settingIconY = 210 - iconRadius;
        float infoIconX = ((ratio*5)+(ratio/2)) - iconRadius, infoIconY = 400 - iconRadius;
        String ico = null;
        String choiceText = "Drag icon";
        int[] choosen = new int[]{0,0,0,0,0,0};

        public DrawingView(Context context) {
            super(context);
            setBackgroundResource(R.mipmap.bghome);
        }

        public boolean onTouchEvent(MotionEvent event) {

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    if (event.getX() > infoIconX  &&
                            event.getX() < infoIconX +(iconRadius*2) &&
                            event.getY() > infoIconY  &&
                            event.getY() < infoIconY +(iconRadius*2) ) {
                        choiceText = "Information";
                        ico = "info";
                    } else if (event.getX() > historyIconX  &&
                            event.getX() < historyIconX +(iconRadius*2) &&
                            event.getY() > historyIconY &&
                            event.getY() < historyIconY +(iconRadius*2)) {
                        choiceText = "History";
                        ico = "history";
                    } else if (event.getX() > balanceIconX  &&
                            event.getX() < balanceIconX +(iconRadius*2) &&
                            event.getY() > balanceIconY  &&
                            event.getY() < balanceIconY +(iconRadius*2)) {
                        choiceText = "Balance";
                        ico = "balance";
                    } else if (event.getX() > promotionsIconX  &&
                            event.getX() < promotionsIconX +(iconRadius*2) &&
                            event.getY() > promotionsIconY  &&
                            event.getY() < promotionsIconY+(iconRadius*2) ) {
                        choiceText = "Promotions";
                        ico = "promotions";
                    } else if (event.getX() > contactIconX  &&
                            event.getX() < contactIconX +(iconRadius*2) &&
                            event.getY() > contactIconY  &&
                            event.getY() < contactIconY +(iconRadius*2)) {
                        choiceText = "Contact";
                        ico = "contact";
                    } else if (event.getX() > settingIconX  &&
                            event.getX() < settingIconX  +(iconRadius*2)&&
                            event.getY() > settingIconY  &&
                            event.getY() < settingIconY +(iconRadius*2)) {
                        choiceText = "Settings";
                        ico = "setting";
                    }
                }
                break;

                case MotionEvent.ACTION_MOVE: {
                    if(ico != null){
                        switch (ico ){
                            case "info":
                                infoIconX = event.getX() - iconRadius;
                                infoIconY = event.getY() - iconRadius;
                                break;
                        case "history":
                            historyIconX = event.getX() - iconRadius;
                            historyIconY = event.getY() - iconRadius;
                            break;
                        case "balance":
                            balanceIconX = event.getX() - iconRadius;
                            balanceIconY = event.getY() - iconRadius;
                            break;
                        case "promotions":
                            promotionsIconX = event.getX() - iconRadius;
                            promotionsIconY = event.getY() - iconRadius;
                            break;
                        case "contact":
                            contactIconX = event.getX() - iconRadius;
                            contactIconY = event.getY() - iconRadius;
                            break;
                        case "setting":
                            settingIconX = event.getX() - iconRadius;
                            settingIconY = event.getY() - iconRadius;
                            break;
                    }
                        if(event.getX()>lockIconX-140 && event.getX()<lockIconX+(iconRadius*2)+140
                            &&event.getY()>lockIconY-140&&event.getY()<lockIconY+(iconRadius*2)+140){
                            switch (ico ){
                                case "info":
                                    infoIconX = lockIconX ;
                                    infoIconY = lockIconY ;
                                    choosen = new int[]{1, 0, 0, 0, 0, 0};
                                    break;
                                case "history":
                                    historyIconX = lockIconX ;
                                    historyIconY = lockIconY ;
                                    choosen = new int[]{0, 1, 0, 0, 0, 0};
                                    break;
                                case "balance":
                                    balanceIconX = lockIconX ;
                                    balanceIconY = lockIconY ;
                                    choosen = new int[]{0, 0, 1, 0, 0, 0};
                                    break;
                                case "promotions":
                                    promotionsIconX = lockIconX ;
                                    promotionsIconY = lockIconY ;
                                    choosen = new int[]{0, 0, 0, 1, 0, 0};
                                    break;
                                case "contact":
                                    contactIconX = lockIconX ;
                                    contactIconY = lockIconY ;
                                    choosen = new int[]{0, 0, 0, 0, 1, 0};
                                    break;
                                case "setting":
                                    settingIconX = lockIconX ;
                                    settingIconY = lockIconY ;
                                    choosen = new int[]{0, 0, 0, 0, 0, 1};
                                    break;
                            }
                        }else{
                            choosen = new int[]{0, 0, 0, 0, 0, 0};
                        }
                        invalidate();
                    }
                }

                break;
                case MotionEvent.ACTION_UP:
                    if(ico != null){
                        boolean flag = false;
                            for (int i = 0 ; i <choosen.length;i++){
                              if(choosen[i]==1){
                                  flag = true;
                                  switch (i){
                                      case 0 :
                                          startActivity(new Intent(HomeActivity.this,AboutActivity.class));
                                          break;
                                      case 1 :
                                          startActivity(new Intent(HomeActivity.this,History.class));
                                          break;
                                      case 2:
                                          startActivity(new Intent(HomeActivity.this,allBalance.class));
                                          break;
                                      case 3:
                                          startActivity(new Intent(HomeActivity.this,PromotionActivity.class));

                                          break;
                                      case 4:
                                          startActivity(new Intent(HomeActivity.this,Contact.class));
                                          break;
                                      case 5:
                                          startActivity(new Intent(HomeActivity.this,Settings.class));
                                          break;
                                      default:
                                  }
//                                  HomeActivity.this.finish();
                              }

                            }
                        if(!flag) {
                            Log.v("not in locked", "hhhh");
                            DrawingView dv = new DrawingView(HomeActivity.this);
                            setContentView(dv);
                        }
//                        invalidate();
                    }
                    ico = null;
                    x = (int) event.getX();
                    y = (int) event.getY();
                    System.out.println(".................." + x + "......" + y); //x= 345 y=530
                    invalidate();
                    break;
            }
            return true;

        }
        //Check for icons have been swiped to  locked icon or not
        private void checkIcons(String ico){

//                        switch (ico ){
//                            case "info":
//                                infoIconX = ((ratio*5)+(ratio/2)) - iconRadius;
//                                infoIconY = 400 - iconRadius;
//                                break;
//                            case "history":
//                                historyIconX = ((ratio)+(ratio/2)) - iconRadius;
//                                historyIconY = 340 - iconRadius;
//                                break;
//                            case "balance":
//                                balanceIconX = (ratio/2)- iconRadius;
//                                balanceIconY = 170 - iconRadius;
//                                break;
//                            case "promotions":
//                                promotionsIconX = ((ratio*2)+(ratio/2)) - iconRadius;
//                                promotionsIconY = 180 - iconRadius;
//                                break;
//                            case "contact":
//                                contactIconX = ((ratio*3)+(ratio/2)) - iconRadius;
//                                contactIconY = 390 - iconRadius;
//                                break;
//                            case "setting":
//                                settingIconX = ((ratio*4)+(ratio/2)) - iconRadius;
//                                settingIconY = 210 - iconRadius;
//                                break;
//                        }
        }
        @Override
        public void onDraw(Canvas canvas) {
//            paint.setStyle(Paint.Style.FILL);
//            paint.setColor(Color.CYAN);
            int ratio = (screenWidth/6);
            int yyy = (screenWidth/100);
            paint.setColor(Color.rgb(127, 127, 127));
            paint.setStrokeWidth(yyy);
            canvas.drawLine((ratio/2)+10, 0, (balanceIconX+iconRadius)+10, balanceIconY+iconRadius, paint);
            paint.setColor(Color.rgb(193, 180, 154));
            canvas.drawLine((ratio/2), 0, (balanceIconX+iconRadius), balanceIconY+iconRadius, paint);

            paint.setColor(Color.rgb(127, 127, 127));
            canvas.drawLine(((ratio)+(ratio/2))+10, 0, (historyIconX+iconRadius)+10, historyIconY+iconRadius, paint);
            paint.setColor(Color.rgb(193, 180, 154));
            canvas.drawLine(((ratio)+(ratio/2)), 0, (historyIconX+iconRadius), historyIconY+iconRadius, paint);

            paint.setColor(Color.rgb(127, 127, 127));
            canvas.drawLine(((ratio*2)+(ratio/2))+10, 0, (promotionsIconX+iconRadius)+10, promotionsIconY+iconRadius, paint);
            paint.setColor(Color.rgb(193, 180, 154));
            canvas.drawLine(((ratio*2)+(ratio/2)), 0, (promotionsIconX+iconRadius), promotionsIconY+iconRadius, paint);

            paint.setColor(Color.rgb(127, 127, 127));
            canvas.drawLine(((ratio*3)+(ratio/2))+10, 0, (contactIconX+iconRadius)+10, contactIconY+iconRadius, paint);
            paint.setColor(Color.rgb(193, 180, 154));
            canvas.drawLine(((ratio*3)+(ratio/2)), 0, (contactIconX+iconRadius), contactIconY+iconRadius, paint);

            paint.setColor(Color.rgb(127, 127, 127));
            canvas.drawLine(((ratio*4)+(ratio/2))+10, 0,(settingIconX+iconRadius)+10, settingIconY+iconRadius, paint);
            paint.setColor(Color.rgb(193, 180, 154));
            canvas.drawLine(((ratio*4)+(ratio/2)), 0, (settingIconX+iconRadius), settingIconY+iconRadius, paint);

            paint.setColor(Color.rgb(127, 127, 127));
            canvas.drawLine(((ratio*5)+(ratio/2))+10, 0, (infoIconX+iconRadius)+10, infoIconY+iconRadius, paint);
            paint.setColor(Color.rgb(193, 180, 154));
            canvas.drawLine(((ratio*5)+(ratio/2)), 0,(infoIconX+iconRadius), infoIconY+iconRadius, paint);


            drawIcon(canvas, R.mipmap.info_ico, infoIconX, infoIconY);
            drawIcon(canvas, R.mipmap.history_ico, historyIconX, historyIconY);
            drawIcon(canvas, R.mipmap.balance_ico, balanceIconX, balanceIconY);
            drawIcon(canvas, R.mipmap.promotions_ico, promotionsIconX, promotionsIconY);
            drawIcon(canvas, R.mipmap.notification_ico, contactIconX, contactIconY);
            drawIcon(canvas, R.mipmap.setting_ico, settingIconX, settingIconY);

            drawIcon(canvas, R.mipmap.lock_ico, lockIconX, lockIconY);

            paint.setColor(Color.rgb(90, 22, 71));

            setTextSizeForWidth(paint, choiceText);
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(
                    choiceText, 0, choiceText.length(), canvas.getWidth()/2 , lockIconY+((iconRadius*2)+20) + iconRadius, paint);


        }

        /**
         * Sets the text size for a Paint object so a given string of text will be a
         * given width.
         *  @param paint
         *            the Paint to set the text size for
         * @param text
         */
        private void setTextSizeForWidth(Paint paint,
                                         String text) {

            // Pick a reasonably large value for the test. Larger values produce
            // more accurate results, but may cause problems with hardware
            // acceleration. But there are workarounds for that, too; refer to
            // http://stackoverflow.com/questions/6253528/font-size-too-large-to-fit-in-cache
            final float testTextSize = 48f;

            // Get the bounds of the text, using our testTextSize.
            paint.setTextSize(testTextSize);
            Rect bounds = new Rect();
            paint.getTextBounds(text, 0, text.length(), bounds);

            // Calculate the desired size as a proportion of our testTextSize.
            float desiredTextSize = testTextSize * 300f / bounds.width();

            // Set the paint for that size.
            paint.setTextSize(desiredTextSize);
        }
    }
//    private class getPromotion extends AsyncTask<String, Void, String> {
//
//        @Override
//        protected String doInBackground(String... strings) {
//            String response = "";
//            URL url = null;
//            try {
//                url = new URL(tinyDB.getString("host")+"list_coords");
//            } catch (MalformedURLException e) {
//                e.printStackTrace();
//            }
//
//
//            HttpURLConnection conn = null;
//            try {
//                conn = (HttpURLConnection) url.openConnection();
//
//                conn.setRequestMethod("GET");
//                conn.setDoInput(true);
//                conn.setUseCaches(false);
//                conn.setRequestMethod("GET");
//                conn.setConnectTimeout(20000);
//                conn.addRequestProperty("Accept", "application/json");
//
//            }catch (Exception e){
//                e.printStackTrace();
//            }
//            int responseCode =0;
//            try {
//                responseCode = conn.getResponseCode();
//            } catch (IOException e1) {
//                e1.printStackTrace();
//            }
//            if (responseCode == HttpURLConnection.HTTP_OK) {
//
//                String line;
//                BufferedReader br = null;
//                try {
//                    br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
//                } catch (IOException e1) {
//                    e1.printStackTrace();
//                }
//
//                try {
//                    while ((line = br.readLine()) != null) {
//                        response += line;
//                    }
//                } catch (IOException e1) {
//                    e1.printStackTrace();
//                }
//            } else {
//                response = "";
//
//            }
//
//            Log.e("eee" , response);
//
//
//            return response;
//        }
//
//
//
//
//
//
//
//        @Override
//        protected void onPostExecute(String aVoid) {
//            super.onPostExecute(aVoid);
//
//            try {
//                getDataFromJson(aVoid);
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//
//        }
//    }
//    private void getDataFromJson(String s) throws JSONException {
//
//
//        JSONArray jsonArray = new JSONArray(s);
//        Log.e("json2",jsonArray.length()+"");
//        for(int i =0 ; i<jsonArray.length() ; i++)
//        {
//            JSONObject jsonObject = jsonArray.getJSONObject(i);
//            lat.add(jsonObject.getString("lat"));
//            lng.add(jsonObject.getString("lng"));
//            desc.add(jsonObject.getString("description"));
//        }
//        tinyDB.putListString("lat",lat);
//        tinyDB.putListString("lng",lng);
//        tinyDB.putListString("desc",desc);
//    }
private class CheckForGift extends AsyncTask<String, Void, String> {
	@Override
	protected void onPreExecute() {
		super.onPreExecute();

	}

	@Override
	protected String doInBackground(String... strings) {
		String response = "";
		URL url = null;
		try {
			url = new URL(tinyDB.getString("host")+"scan_nfc/"+strings[0]);
			Log.v("TAG","urllll "+url.toString());
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
	protected void onPostExecute(String strings) {
		super.onPostExecute(strings);
		try {
			parseGift(strings);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		Log.e(TAG,strings);

	}
	private void parseGift(String jsonStr) throws JSONException{
		JSONObject jsonObject = new JSONObject(jsonStr);
		String gift = jsonObject.getString("msg");
		if(gift.equals("good luck!")){
            startActivity(new Intent(HomeActivity.this,Goodluck.class));
		}else if(gift.equals(null)){
			Log.v(TAG,"hiii");
		}


	}

}
}
