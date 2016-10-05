package com.example.ol.rssreader;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.RemoteViews;

import com.example.ol.rssreader.RSS.RSSItem;

import java.util.HashMap;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link RssWidgetConfigureActivity RssWidgetConfigureActivity}
 */
public class RssWidget extends AppWidgetProvider {
  /// for logging
  private static final String LOG_TAG = RssWidget.class.getName();

  private AppWidgetManager mAppWidgetManager;
  private ComponentName mComponentName;

  /// global settings
  private static String sRssUrl = Constants.Rss.RSS_URL;
  private static int sRssTime = Constants.Rss.RSS_UPDATE_INTERVAL;

  private static DataStorage sDataStorage = null;

  private static HandlerThread sRefreshDataThread;
  private static Handler sRefreshDataHandler;

  /// alarm for refresh data (through service)
  private static AlarmManager sAlarmManager = null;
  private static Intent sRefreshDataIntent = null;

  public void onReceive(Context context, Intent intent) {
    if (Constants.Actions.SERVICE_DATA_CHANGED.equals(intent.getAction()))
      /// got service event for data change
      refreshRSSData(context);
    else if (Constants.Actions.WIDGET_NAVIGATE.equals(intent.getAction())) {
      /// got navigation click => get new data and show it
      changeNaviData(context, intent.getExtras());
    } else
      /// pass standard intent for widget
      super.onReceive(context, intent);
  }


  static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                              int appWidgetId) {

    /// build view objects & use it
    RemoteViews rv = buildRemoteViews(context, appWidgetId);
    RemoteViewsStorage.addRemoteViews(appWidgetId, rv);
    appWidgetManager.updateAppWidget(appWidgetId, rv);
  }

  @Override
  public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
    for (int appWidgetId : appWidgetIds) {
      updateAppWidget(context, appWidgetManager, appWidgetId);
    }
  }

  @Override
  public void onDeleted(Context context, int[] appWidgetIds) {
    if (sDataStorage == null)
      return;
    for (int appWidgetId : appWidgetIds) {
      sDataStorage.removeItemPosition(appWidgetId);
    }
  }

  @Override
  public void onEnabled(Context context) {
    super.onEnabled(context);

    mAppWidgetManager = AppWidgetManager.getInstance(context);
    mComponentName = new ComponentName(context, RssWidget.class);

    /// load params for ALL widgets
    RssWidgetConfigureActivity.RSSConfigData rssConfigData =
        RssWidgetConfigureActivity.loadPrefs(context);
    sRssUrl = rssConfigData.rssUrl;
    sRssTime = rssConfigData.rssTime;

    /// get access to global RSS data
    sDataStorage = DataStorage.getInstance(context);

    /// start separate thread for data refresh
/*
    sRefreshDataThread = new HandlerThread(Constants.Widget.REFRESH_THREAD_NAME);
    sRefreshDataThread.start();
    sRefreshDataHandler = new Handler(sRefreshDataThread.getLooper());
*/

    sAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    sRefreshDataIntent = new Intent(context, RssService.class);
    sRefreshDataIntent.setAction(Constants.Actions.SERVICE_GET_DATA);
    sRefreshDataIntent.putExtra(Constants.Extras.SERVICE_EXTRA_PARAM_RSS_URL, sRssUrl);
    context.startService(sRefreshDataIntent); /// first start
    startCycleRefresh(context, sRssUrl, sRssTime);
  }

  @Override
  public void onDisabled(Context context) {
    super.onDisabled(context);
    RemoteViewsStorage.clear();
    if (sDataStorage != null)
      sDataStorage.clear();
    stopCycleRefresh(context); /// stop cyclical alarm signalling
    context.stopService(sRefreshDataIntent);

    /// when removing LAST widget, delete the preference associated
    RssWidgetConfigureActivity.deleteTitlePref(context);
  }

  private void startCycleRefresh(Context context, String rssUrl, int rssTime) {
    if (sAlarmManager == null)
      return;
    /// reconfigure url right over existing intent param
    sRefreshDataIntent.putExtra(Constants.Extras.SERVICE_EXTRA_PARAM_RSS_URL, rssUrl);
    Log.d(LOG_TAG, "startCycleRefresh() - rssTime=" + rssTime);
    sAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
        rssTime, PendingIntent.getService(
            context, 0, sRefreshDataIntent, PendingIntent.FLAG_CANCEL_CURRENT));
  }

  private void stopCycleRefresh(Context context) {
    if (sAlarmManager == null)
      return;
    Log.d(LOG_TAG, "stopCycleRefresh()");
    sAlarmManager.cancel(PendingIntent.getService(
        context, 0, sRefreshDataIntent, PendingIntent.FLAG_CANCEL_CURRENT));
  }

  private static RemoteViews buildRemoteViews(Context context, int appWidgetId) {
    RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.rssreader_widget_layout);
    rv.setTextViewText(R.id.tvTitle, context.getString(R.string.tvEmptyTitleText));
    rv.setTextViewText(R.id.tvDescr, context.getString(R.string.tvEmptyDescrText));

    /// bind the click intent for the rss item navigation buttons on the widget
    final Intent navigateIntent = new Intent(context, RssWidget.class);
    navigateIntent.setAction(Constants.Actions.WIDGET_NAVIGATE);
    navigateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
    final PendingIntent navigateNextPendingIntent =
        PendingIntent.getBroadcast(context, appWidgetId,
            navigateIntent.putExtra(Constants.Extras.WIDGET_NAVIGATE_NEXT_FLAG, true),
            PendingIntent.FLAG_UPDATE_CURRENT);
    final PendingIntent navigatePrevPendingIntent =
        PendingIntent.getBroadcast(context, appWidgetId,
            navigateIntent.putExtra(Constants.Extras.WIDGET_NAVIGATE_NEXT_FLAG, false),
            PendingIntent.FLAG_UPDATE_CURRENT);
    rv.setOnClickPendingIntent(R.id.ibPrev, navigatePrevPendingIntent);
    rv.setOnClickPendingIntent(R.id.ibNext, navigateNextPendingIntent);

    /// bind config button with config. activity
    final Intent configIntent = new Intent(context, RssWidgetConfigureActivity.class);
    configIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
    configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
    PendingIntent configPendingIntent =
        PendingIntent.getActivity(context, 0, configIntent, 0);
    rv.setOnClickPendingIntent(R.id.ibSettings, configPendingIntent);

    return rv;
  }

  /**
   * refreshes RSS items in ALL widgets
   * @param context
   */
  private void refreshRSSData(Context context) {
    sDataStorage.updateItemsList(); /// replace data in storage

    /// redraws ALL widgets (just like we've made navigate next in all widgets)
    int[] appWidgetIds = mAppWidgetManager.getAppWidgetIds(mComponentName);
    for (int appWidgetId : appWidgetIds) {
      changeWidgetDataItem(context, appWidgetId, true);
    }
  }

  /**
   * gets data item from the storage for the widget and direction pointed
   * updates data views and redraws the widget
   * @param appWidgetId
   * @param nextFlag
   * @return data item or null if any errors
   */
  private void changeWidgetDataItem(Context context, int appWidgetId, boolean nextFlag) {
    /// get items data according navigation direction exactly for this widget
    RSSItem item = sDataStorage.getNewItem(appWidgetId, nextFlag);

    /// get existed view for the widget & modify it
    RemoteViews views = RemoteViewsStorage.getRemoteViews(appWidgetId);
    if (views != null) {
      views.setTextViewText(R.id.tvTitle, item.getTitle());
      views.setTextViewText(R.id.tvDescr, item.getDescription());
      mAppWidgetManager.updateAppWidget(appWidgetId, views);
    }
  }

  /**
   * defines widget & movement direction, then changes data views
   * @param context
   * @param bundle - Bundle containing widget ID and navigation direction
   */
  private void changeNaviData(Context context, @NonNull Bundle bundle) {
    if (bundle == null)
      return;
    int appWidgetId = bundle.getInt(
        AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
    boolean nextFlag = bundle.getBoolean(
        Constants.Extras.WIDGET_NAVIGATE_NEXT_FLAG, true);

    changeWidgetDataItem(context, appWidgetId, nextFlag);
  }

  /**
   * class for existed RemoteViews storage (will be used later for refresh widgets data)
   */
  private static class RemoteViewsStorage {
    /// the Key is widget ID
    static HashMap<Integer, RemoteViews> viewsMap = new HashMap<>();

    static RemoteViews getRemoteViews(int appWidgetId) {
      return viewsMap.get(appWidgetId);
    }

    static void addRemoteViews(int appWidgetId, RemoteViews views) {
      viewsMap.put(appWidgetId, views);
    }

    static void clear() {
      viewsMap.clear();
    }
  }
}

