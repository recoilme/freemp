package org.freemp.droid.web;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.androidquery.AQuery;

import org.freemp.droid.ClsTrack;
import org.freemp.droid.R;
import org.freemp.droid.playlist.albums.AdpArtworks;

import java.util.ArrayList;

/**
 * Created by recoil on 26.01.14.
 */
public class ActFreemporg extends AppCompatActivity {

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
        getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.actionbar_bgr));
        //добавляем кнопку назад
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        activity = this;
        aq = new AQuery(activity);

        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            return;
        } else {
            q = extras.getString("q");

        }
        //UI
        final LinearLayout linearLayout = new LinearLayout(activity);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        //Progress
        progressBar = new ProgressBar(activity, null, android.R.attr.progressBarStyleHorizontal);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        progressBar.setLayoutParams(layoutParams);
        progressBar.setVisibility(View.GONE);
        webView = new WebView(activity);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
                                     @Override
                                     public boolean shouldOverrideUrlLoading(WebView view, String url) {
                                         if (Uri.parse(url).getHost().contains("freemp.org")) {
                                             // This is my web site, so do not override; let my WebView load the page
                                             return false;
                                         }
                                         // Otherwise, the link is not for a page on my site, so launch another Activity that handles URLs
                                         Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                         startActivity(intent);
                                         return true;
                                     }
                                 }
        );
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
        inflater.inflate(R.menu.menu_artwork, menu);

        //только после того как меню создано - запускаем обновление
        update();
        return super.onCreateOptionsMenu(menu);
    }

    public void update() {

        //устанавливаем статус в "обновляется"
        refreshing = true;
        //раскручиваем колесеко
        setRefreshActionButtonState();
        webView.loadUrl("https://www.last.fm/music/" + Uri.encode(q)); //+ "&l=" + (Locale.getDefault().getLanguage().contains("ru") ? "ru" : "en"));
        webView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {

                if (progress >= 99) {
                    refreshing = false;
                    setRefreshActionButtonState();
                }
            }
        });
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
                    MenuItemCompat.setActionView(refreshItem, R.layout.actionbar_progress);
                } else {
                    MenuItemCompat.setActionView(refreshItem, null);
                }
            }
        }
    }
}