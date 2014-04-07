package ru.recoilme.freeamp.freemp.org;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.view.*;
import android.webkit.WebView;
import android.widget.*;
import com.androidquery.AQuery;
import com.androidquery.util.AQUtility;
import org.json.JSONArray;
import org.json.JSONObject;
import ru.recoilme.freeamp.*;
import ru.recoilme.freeamp.artworks.AdpArtworks;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

/**
 * Created by recoil on 26.01.14.
 */
public class ActFreemporg extends ActionBarActivity {

    private AQuery aq;
    private Menu optionsMenu;
    private boolean refreshing = true;
    private Activity activity;
    private ArrayList<ClsTrack> tracks;
    private String q = "";
    //UI
    private WebView webView;
    private AdpArtworks adapter;
    private ProgressBar progressBar;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //устанавливаем кастомный бэкграунд акшенбара
        getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.ab_bgr));
        //добавляем кнопку назад
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        activity = this;
        aq = new AQuery(activity);

        Bundle extras = getIntent().getExtras();
        if (extras==null) {
            return;
        }
        else {
            q = extras.getString("q");

        }
        //UI
        final LinearLayout linearLayout = new LinearLayout(activity);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        //Progress
        progressBar = new ProgressBar(activity,null,android.R.attr.progressBarStyleHorizontal);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        progressBar.setLayoutParams(layoutParams);
        progressBar.setVisibility(View.GONE);
        webView = new WebView(activity);
        webView.getSettings().setJavaScriptEnabled(true);
        ViewGroup.LayoutParams layoutParams2 = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        webView.setLayoutParams(layoutParams2);
        linearLayout.addView(progressBar);
        linearLayout.addView(webView);
        setContentView(linearLayout);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.optionsMenu = menu;
        //создаем меню в акшенбаре
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.artwork, menu);

        //только после того как меню создано - запускаем обновление
        update();
        return super.onCreateOptionsMenu(menu);
    }

    public void update() {

        //устанавливаем статус в "обновляется"
        refreshing = true;
        //раскручиваем колесеко
        setRefreshActionButtonState();

        webView.loadUrl("http://freemp.org/artist/search?q="+Uri.encode(q));
        refreshing = false;
        setRefreshActionButtonState();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //закрываем активити на нажатие кнопки домой
                finish();
                return true;
            case R.id.menu_refresh:
                update();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setRefreshActionButtonState() {

        //если статус обновляется - заменяем иконку обновить на крутящийся прогрессбар
        if (optionsMenu != null) {
            final MenuItem refreshItem = optionsMenu
                    .findItem(R.id.menu_refresh);
            if (refreshItem != null) {
                if (refreshing) {
                    MenuItemCompat.setActionView(refreshItem, R.layout.actionbar_indeterminate_progress);
                } else {
                    MenuItemCompat.setActionView(refreshItem,null);
                }
            }
        }
    }
}