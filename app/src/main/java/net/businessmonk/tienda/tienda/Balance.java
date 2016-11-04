package net.businessmonk.tienda.tienda;

import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

public class Balance extends AppCompatActivity implements PromoHeadlinesFragment.OnHeadlineSelectedListener{

    static int companyyy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_balance);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(0xFF764F00);
        }
        TinyDB tinyDB = new TinyDB(getApplicationContext());
        //check if the activity is using the layout version
        //with the FrameLayout.  if so, we have to add the fragment
        //(it wont be done automatically )
        contentsFragment articleFragment = (contentsFragment) getFragmentManager().findFragmentById(R.id.article_fragment);
//        updateArticleView(contentsFragment.posi,Integer.parseInt(contentsFragment.companies.get(contentsFragment.posi)));
        if(articleFragment != null){
             companyyy= Integer.parseInt(getIntent().getStringExtra("company_id"));
            int posss = getIntent().getIntExtra("position", 0);

            //we must be icn two pane layout
            Log.e("compaaaaaaaany",companyyy+"");
            articleFragment.updateArticleView(posss, companyyy);

        }

    }

    public void back(View view) {
        super.onBackPressed();
    }

    public void invite(View view) {

        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "\n\n");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, "hi, I'm using Tienda. It's amazing app you can also have it from here , My id is 384 let them know");
        startActivity(Intent.createChooser(sharingIntent,  "Where to invite"));
    }

    @Override
    public void onArticleSelected(int position,int compId) {
//Capture the article fragment from the activity's dual-pane layout
        //if we dont find one, we must not be in two pane mode
        //lets swap the Fragments instead
        int position1 = position;
        int compId1 = compId;
        contentsFragment articleFragment = (contentsFragment) getFragmentManager().findFragmentById(R.id.article_fragment);
//        updateArticleView(contentsFragment.posi,Integer.parseInt(contentsFragment.companies.get(contentsFragment.posi)));
        if(articleFragment != null){

            //we must be icn two pane layout
            articleFragment.updateArticleView(position,compId);

        }
    }

}
