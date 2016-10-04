package com.example.ol.rssreader;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.ol.rssreader.RSS.RSSItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RssService extends Service {
  //for logging
  private static final String LOG_TAG = RssService.class.getName();

  private DB mDB;

  private static DataStorage sDataStorage = null;

  GetRssNParseTask mGetRssNParseTask = null;
  private static int sCount = 0;

  public void onCreate() {
    Log.d(LOG_TAG, "onCreate()");
    super.onCreate();
    mDB = new DB(this);
    mDB.open();

    /// get access to global RSS data
    sDataStorage = DataStorage.getInstance(this);
  }

  public int onStartCommand(Intent intent, int flags, int startId) {
    Log.d(LOG_TAG, "onStartCommand()");

    if (Constants.Actions.SERVICE_GET_DATA.equals(intent.getAction()))
      handleStart(intent.getExtras());

//    return super.onStartCommand(intent, flags, startId);
//    return Service.START_REDELIVER_INTENT;
    return Service.START_NOT_STICKY;
  }

  private void handleStart(Bundle bundle) {
    /// default for empty intent
    String rssUrlNew = Constants.Rss.RSS_URL;

    if (bundle != null) {
      rssUrlNew = bundle.getString(
          Constants.Extras.SERVICE_EXTRA_PARAM_RSS_URL, Constants.Rss.RSS_URL);
    }

    stopRead();
    startRead(rssUrlNew);
  }


  public IBinder onBind(Intent intent) {
    return null;
  }

  public void onDestroy() {
    super.onDestroy();
    Log.d(LOG_TAG, "onDestroy()");
    stopRead();
    mDB.close();
  }

  private void startRead(String url) {
    final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
    String currentDateandTime = sdf.format(new Date());
    Log.d(LOG_TAG, "startRead() - [" + currentDateandTime + "]");

    if (mDB != null) {
      mGetRssNParseTask = new GetRssNParseTask(mDB);
      mGetRssNParseTask.execute(new String[]{url});
    }
  }

  private void stopRead() {
    Log.d(LOG_TAG, "stopRead()");
    if (mGetRssNParseTask != null) {
      mGetRssNParseTask.cancel(true);
      mGetRssNParseTask = null;
    }
  }
  private class GetRssNParseTask extends AsyncTask<String, Void, Void> {
    int mOldRssDBHash = 1; /// hash for RSS DB
    private DB mDB;

    public GetRssNParseTask(@NonNull DB db) {
      this.mDB = db;
    }

    @Override
    protected void onPreExecute() {
      super.onPreExecute();
      mOldRssDBHash = mDB.getCumHashCode(); /// store hash from original DB
    }

    @Override
    protected Void doInBackground(String... urls) {
      Log.d(LOG_TAG, "GetRssNParseTask(" + urls[0] + ") - doInBackground()");
      mDB.clear();
      List<RSSItem> newList = new ArrayList<>(3);
      RSSItem newItem = null;
      int newHash = 1;
      for (int i = 1; i < 3; i++) {
        if (sCount != 0)
          newItem = new RSSItem("title[" + i + "]:" + urls[0], "descr[" + i + "]:");
        else
          newItem = new RSSItem("_title[" + i + "]:" + urls[0], "_descr[" + i + "]:");
        newHash = newHash*17 + newItem.hashCode();
        newList.add(newItem);
      }
      if (++sCount >= 3)
        sCount = 0;
      if (mDB.getCumHashCode() != newHash) {
        /// store NEW data only
        mDB.fillTabWithRecList(newList, newHash);
        sDataStorage.storeNewData(newList);
      }
      return null;
  }

    @Override
    protected void onPostExecute(Void aVoid) {
      super.onPostExecute(aVoid);
      if (mDB.getCumHashCode() != mOldRssDBHash) {
        Log.d(LOG_TAG, "GetRssNParseTask() = got __NEW__ data");
        Intent notifyIntent = new Intent();
        notifyIntent.setAction(Constants.Actions.SERVICE_DATA_CHANGED);
        sendBroadcast(notifyIntent);
      }
    }

    @Override
    protected void onCancelled() {
      super.onCancelled();
      mDB.clear();
    }
  }
}
