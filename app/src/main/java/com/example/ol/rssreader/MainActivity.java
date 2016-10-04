package com.example.ol.rssreader;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.content.CursorLoader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.view.View;

import android.widget.Button;
import android.widget.EditText;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
  EditText mTitle, mDescription;
  Button btRestartService, btStopService, btPrev, btNext;

  private String rssUrl1 = "https://habrahabr.ru/rss";
  private String rssUrl2 = "https://google.com/rss";

  private DB mDB;
  private RSSCursor mCursor;
  SimpleCursorAdapter mSCAdapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mTitle = (EditText) findViewById(R.id.editText);
    mDescription = (EditText) findViewById(R.id.editText3);

    mCursor = new RSSCursor(mDB);

    btPrev = (Button) findViewById(R.id.btPrev);
    if ( (mCursor != null) && (btPrev != null) )
      btPrev.setEnabled(!mCursor.isFirstItem());

    btNext = (Button) findViewById(R.id.btNext);
    if ( (mCursor != null) && (btNext != null) )
      btNext.setEnabled(!mCursor.isLastItem());

    btRestartService = (Button)findViewById(R.id.btRestartService);
    btRestartService.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        startRssService(Constants.Actions.SERVICE_DATA_CHANGED, rssUrl2);
      }
    });

    btStopService = (Button)findViewById(R.id.btStopService);
    btStopService.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        stopService(new Intent(v.getContext(), RssService.class));
      }
    });
  }

  private void startRssService(String action, String url) {
/*
    Intent intent = new Intent(this, RssService.class);
    intent.setAction(action);
    intent.putExtra(Constants.Extras.SERVICE_EXTRA_PARAM_RSS_URL, url);
    startService(intent);
*/
  }

  @Override
  public void onClick(View view) {
/*
    switch (view.getId()) {
      case R.id.btPrev:
        RSSItem prevItem = mCursor.getPrevItem();
        mTitle.setText(prevItem.getTitle());
        mDescription.setText(prevItem.getDescription());
        view.setEnabled(!mCursor.isFirstItem());
        break;
    }
*/
  }

  static class MyCursorLoader extends CursorLoader {
    DB db;

    public MyCursorLoader(Context context, DB db) {
      super(context);
      this.db = db;
    }

    @Override
    public Cursor loadInBackground() {
      Cursor cursor = db.getAllData();
      try {
        TimeUnit.SECONDS.sleep(3);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      return cursor;
    }

  }
}