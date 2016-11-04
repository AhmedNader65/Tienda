package net.businessmonk.tienda.tienda;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * Created by ahmed on 28/05/16.
 */
public class promotions_selected  extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.selection_promotion);
        TextView body = (TextView) findViewById(R.id.body);
        TextView head = (TextView) findViewById(R.id.head);
        ImageView img = (ImageView) findViewById(R.id.tip_img);
        body.setText(getIntent().getStringExtra("body"));
        head.setText(getIntent().getStringExtra("head"));
//        img.setImageBitmap(decode64(getIntent().getStringExtra("img")));
        Picasso.with(this).load("http://hitienda.com/"+getIntent().getStringExtra("img")).into(img);
    }

    public void back(View view) {
        super.onBackPressed();
    }
}
