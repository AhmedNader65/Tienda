package net.businessmonk.tienda.tienda;

import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ListView;

public class PromotionActivity extends AppCompatActivity implements PromoHeadlinesFragment.OnHeadlineSelectedListener {

    ListView listView ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_promotion);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(0xFF690409);
        }
//        listView= (ListView)findViewById(R.id.promotion_list);

    }


    @Override
    public void onArticleSelected(int position, int company) {
        PromocontentsFragment articleFragment = (PromocontentsFragment) getFragmentManager().findFragmentById(R.id.article_fragment);

        //if we dont find one, we must not be in two pane mode
        //lets swap the Fragments instead
        if(articleFragment != null){

            //we must be in two pane layout
            articleFragment.updateArticleView(position,company);

        }
    }
    public void back(View view) {
        super.onBackPressed();
    }

}
