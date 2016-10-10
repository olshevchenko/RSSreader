package com.example.ol.rssreader;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ParseException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.ol.rssreader.RSS.FeedParser;
import com.example.ol.rssreader.RSS.RSSItem;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RssService extends Service {
  //for logging
  private static final String LOG_TAG = RssService.class.getName();

  private static int sCount = 0;

  private static DataStorage sDataStorage = null;

  private GetRssNParseTask mGetRssNParseTask = null;
  private FeedParser mFeedParser = null;

  public void onCreate() {
    Log.d(LOG_TAG, "onCreate()");
    super.onCreate();

    /// get access to global RSS data
    sDataStorage = DataStorage.getInstance(this.getApplicationContext());

    FeedParser mFeedParser = new FeedParser();
  }

  public int onStartCommand(Intent intent, int flags, int startId) {
//    Log.d(LOG_TAG, "onStartCommand()");

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
  }

  private void startRead(String url) {
    final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
    String currentDateandTime = sdf.format(new Date());
    Log.d(LOG_TAG, "startRead() - [" + currentDateandTime + "]");

    mGetRssNParseTask = new GetRssNParseTask(this.getApplicationContext());
    mGetRssNParseTask.execute(new String[]{url});
  }

  private void stopRead() {
    if (mGetRssNParseTask != null) {
      Log.d(LOG_TAG, "stopRead()");
      mGetRssNParseTask.cancel(true);
      mGetRssNParseTask = null;
    }
  }

  private class GetRssNParseTask extends AsyncTask<String, Void, Void> {
    private Context mAppContext;

    public GetRssNParseTask(Context context) {
      mAppContext = context;
    }

    @Override
    protected Void doInBackground(String... urls) {
//      Log.d(LOG_TAG, "GetRssNParseTask(" + urls[0] + ") - doInBackground()");
/*
      List<RSSItem> newList = null;
      try {
        URL url = new URL(urls[0]);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000);
        conn.setConnectTimeout(15000);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        conn.connect();
        InputStream stream = conn.getInputStream();
        // parse xml after getting the data
        Log.d(LOG_TAG, "stream: " + stream);
        newList = mFeedParser.parse(stream);
      } catch (MalformedURLException e) {
        Log.d(LOG_TAG, "URL format error");
        e.printStackTrace();
      } catch (XmlPullParserException | ParseException e) {
        Log.d(LOG_TAG, "XML Parsing error");
        e.printStackTrace();
      } catch (IOException e) {
        Log.d(LOG_TAG, "IO error");
        e.printStackTrace();
      }
*/
      List<RSSItem> newList = new ArrayList<>(3);
      RSSItem newItem;
      int idx;
      for (int i = 0; i < 3; i++) {
        idx = sCount*3 + i;
        newItem = new RSSItem("title[" + idx + "]", "descr[" + idx + "]");
        newList.add(newItem);
      }
      if (++sCount >= 5)
        sCount = 0;

      sDataStorage.storeNewData(newList);
      return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
      super.onPostExecute(aVoid);
      if (sDataStorage.isHashDifferent()) {
        Log.d(LOG_TAG, "GetRssNParseTask() = got __NEW__ data");
        Intent notifyIntent = new Intent(mAppContext, RssWidget.class);
        notifyIntent.setAction(Constants.Actions.SERVICE_DATA_CHANGED);
        sendBroadcast(notifyIntent);
      }
    }
  }
}