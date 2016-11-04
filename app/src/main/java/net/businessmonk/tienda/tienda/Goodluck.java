package net.businessmonk.tienda.tienda;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class Goodluck extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_goodluck);
	}

	public void ok(View view) {
		Goodluck.this.finish();
	}
}
