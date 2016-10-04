package com.example.ol.rssreader;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.EditText;

/**
 * The configuration screen for the {@link RssWidget RssWidget} AppWidget.
 */
public class RssWidgetConfigureActivity extends Activity implements View.OnClickListener {
  /// for logging
  private static final String LOG_TAG = RssWidgetConfigureActivity.class.getName();

  int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
  Intent mResultValue = null;
  EditText mEtRssUrl;
  EditText mEtRssTime;
  String mRssUrl = Constants.Rss.RSS_URL;
  int mRssTime = Constants.Rss.RSS_UPDATE_INTERVAL;

  public RssWidgetConfigureActivity() {
    super();
  }

  /// Write the prefix to the SharedPreferences object for this widget
  static void savePrefs(@NonNull Context context,
                        @NonNull RSSConfigData rssConfigData) {
    SharedPreferences.Editor prefs =
        context.getSharedPreferences(Constants.Prefs.NAME, 0).edit();
    prefs.putString(Constants.Prefs.RSS_URL_KEY, rssConfigData.rssUrl);
    prefs.putInt(Constants.Prefs.RSS_UPDATE_INTERVAL_KEY, rssConfigData.rssTime);
    prefs.apply();
  }

  /// Read the prefix from the SharedPreferences object for this widget
  static @NonNull RSSConfigData loadPrefs(@NonNull Context context) {
    SharedPreferences prefs = context.getSharedPreferences(Constants.Prefs.NAME, 0);
    String rssUrl =
        prefs.getString(Constants.Prefs.RSS_URL_KEY, Constants.Rss.RSS_URL);
    int rssTime =
        prefs.getInt(Constants.Prefs.RSS_UPDATE_INTERVAL_KEY, Constants.Rss.RSS_UPDATE_INTERVAL);
    return new RSSConfigData(rssUrl, rssTime);
  }

  static void deleteTitlePref(@NonNull Context context) {
    SharedPreferences.Editor prefs = context.getSharedPreferences(Constants.Prefs.NAME, 0).edit();
    prefs.remove(Constants.Prefs.RSS_URL_KEY);
    prefs.remove(Constants.Prefs.RSS_UPDATE_INTERVAL_KEY);
    prefs.apply();
  }

  @Override
  public void onCreate(Bundle bundle) {
    super.onCreate(bundle);

    setContentView(R.layout.rssreader_widget_configure);

    /// Get the widget id from the intent
    Intent intent = getIntent();
    Bundle extras = intent.getExtras();
    if (extras != null) {
      mAppWidgetId = extras.getInt(
          AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    /// Set the result to CANCELED (to make the widget host to cancel
    /// out of the widget placement if the user presses the back button).
    mResultValue = new Intent();
    /// Make sure we pass back the original appWidgetId
    mResultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
    setResult(RESULT_CANCELED, mResultValue);

    if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
      finish();
      return;
    }

    /// Get preferences data and fill the forms with it
    RSSConfigData rssConfigData = loadPrefs(RssWidgetConfigureActivity.this);
    mEtRssUrl = (EditText) findViewById(R.id.etRSSUrl);
    if (mEtRssUrl != null)
      mEtRssUrl.setText(rssConfigData.rssUrl);
    mEtRssTime = (EditText) findViewById(R.id.etRSSTime);
    if (mEtRssTime != null)
      mEtRssTime.setText(String.valueOf(rssConfigData.rssTime));

    findViewById(R.id.btAddWidget).setOnClickListener(this);
  }


  @Override
  public void onClick(View v) {
    switch (v.getId()) {
      case R.id.btAddWidget:
        final Context context = RssWidgetConfigureActivity.this;

        /// store params locally
        mRssUrl = mEtRssUrl.getText().toString();
        try {
          mRssTime = Integer.valueOf(mEtRssTime.getText().toString());
        } catch (NumberFormatException ex) {
        }
        savePrefs(context, new RSSConfigData(mRssUrl, mRssTime));

        /// then, update the app widget
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        RssWidget.updateAppWidget( context, appWidgetManager, mAppWidgetId );
        setResult(RESULT_OK, mResultValue);
        finish();
        break;
      default:
        break;
    }
  }

  public static class RSSConfigData {
    public String rssUrl;
    public int rssTime;

    public RSSConfigData(String rssUrl, int rssTime) {
      this.rssUrl = rssUrl;
      this.rssTime = rssTime;
    }
  }
}

