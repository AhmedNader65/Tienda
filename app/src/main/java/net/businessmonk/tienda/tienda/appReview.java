package net.businessmonk.tienda.tienda;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * Created by ahmed on 10/06/16.
 */
class appReview {
	private final static String APP_TITLE = "Tienda";// App Name
	private final static String APP_PNAME = "net.businessmonk.tienda.tienda";// Package Name

	private final static int DAYS_UNTIL_PROMPT = 3;//Min number of days
	private final static int LAUNCHES_UNTIL_PROMPT = 3;//Min number of launches

	public static void app_launched(Context mContext) {
		SharedPreferences prefs = mContext.getSharedPreferences("apprater", 0);
		if (prefs.getBoolean("dontshowagain", false)) { return ; }

		SharedPreferences.Editor editor = prefs.edit();

		// Increment launch counter
		long launch_count = prefs.getLong("launch_count", 0) + 1;
		editor.putLong("launch_count", launch_count);

		// Get date of first launch
		Long date_firstLaunch = prefs.getLong("date_firstlaunch", 0);
		if (date_firstLaunch == 0) {
			date_firstLaunch = System.currentTimeMillis();
			editor.putLong("date_firstlaunch", date_firstLaunch);
		}

		// Wait at least n days before opening
		if (launch_count >= LAUNCHES_UNTIL_PROMPT) {
			if (System.currentTimeMillis() >= date_firstLaunch +
					(DAYS_UNTIL_PROMPT *24 * 60 * 60 * 1000)) {
				showRateDialog(mContext, editor);
				editor.putLong("launch_count", 0);
			}
		}
		editor.apply();
	}

	private static void showRateDialog(final Context mContext, final SharedPreferences.Editor editor) {
		final Dialog dialog = new Dialog(mContext);
		dialog.setTitle("Rate " + APP_TITLE);

		LinearLayout ll = new LinearLayout(mContext);
		ll.setOrientation(LinearLayout.VERTICAL);
		ll.setMinimumWidth(400);
		TextView tv = new TextView(mContext);
		tv.setText("If you enjoy using " + APP_TITLE + ", please take a moment to rate it. Thanks for your support!");
		tv.setPadding(4, 0, 4, 10);

		ll.addView(tv);

		Button b1 = new Button(mContext);
		b1.setText("Rate " + APP_TITLE);
		b1.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + APP_PNAME)));
				dialog.dismiss();
			}
		});
		ll.addView(b1);

		Button b2 = new Button(mContext);
		b2.setText("Remind me later");
		b2.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		ll.addView(b2);

		Button b3 = new Button(mContext);
		b3.setText("No, thanks");
		b3.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (editor != null) {
					editor.putBoolean("dontshowagain", true);
					editor.commit();
				}
				dialog.dismiss();
			}
		});
		ll.addView(b3);

		dialog.setContentView(ll);
		dialog.show();
	}
public static void showFeedBackDialog(final Context mContext) {
	final Dialog dialog = new Dialog(mContext);
	dialog.setContentView(R.layout.feedback_layout);
	dialog.setCanceledOnTouchOutside(true);
	dialog.setTitle("Send Feedback");
	Button dialogButton = (Button) dialog.findViewById(R.id.ButtonSendFeedback);
	final EditText nameField = (EditText) dialog.findViewById(R.id.EditTextName);
	final EditText feedbackField = (EditText) dialog.findViewById(R.id.EditTextFeedbackBody);
	final Spinner feedbackSpinner = (Spinner) dialog.findViewById(R.id.SpinnerFeedbackType);
	final CheckBox responseCheckbox = (CheckBox) dialog.findViewById(R.id.CheckBoxResponse);
	// if button is clicked, close the custom dialog
	dialogButton.setOnClickListener(new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			String name = nameField.getText().toString();
			String feedback = feedbackField.getText().toString();
			String feedbackType = feedbackSpinner.getSelectedItem().toString();
			boolean bRequiresResponse = responseCheckbox.isChecked();
			String resp;
			if(bRequiresResponse)
				resp = "requires a response";
			else
			resp = "";
			Intent email = new Intent(Intent.ACTION_SEND);
			email.putExtra(Intent.EXTRA_EMAIL, new String[]{"ahmed.nader1994@gmail.com"});
			email.putExtra(Intent.EXTRA_SUBJECT, "Tienda FeedBack("+feedbackType+")" );
			email.putExtra(Intent.EXTRA_TEXT, feedback+"\n\n __________________________________ "+name+"\n\n "+resp);
			email.setType("message/rfc822");
			mContext.startActivity(Intent.createChooser(email, "Choose an Email client :"));
			dialog.dismiss();
		}
	});
	dialog.show();
	}
}
