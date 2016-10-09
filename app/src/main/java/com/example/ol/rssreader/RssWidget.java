package com.example.ol.rssreader;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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

  private static AppWidgetManager mAppWidgetManager;
  private static ComponentName mComponentName;

  /// global settings
  private static String sRssUrl = Constants.Rss.RSS_URL;
  private static int sRssTime = Constants.Rss.RSS_UPDATE_INTERVAL;

  private static DataStorage sDataStorage = null;

  /// alarm for refresh data (through service)
  private static AlarmManager sAlarmManager = null;
  private static Intent sRefreshDataIntent = null;

  public void onReceive(Context context, Intent intent) {
    if (Constants.Actions.SERVICE_DATA_CHANGED.equals(intent.getAction()))
      /// got service event for data change
      changeAllWidgetsData();
    else if (Constants.Actions.WIDGET_NAVIGATE_NEXT.equals(intent.getAction()))
      /// got navigation click => get new data and show it
      changeNaviData(intent.getExtras(), true);
    else if (Constants.Actions.WIDGET_NAVIGATE_PREV.equals(intent.getAction())) {
      /// got navigation click => get new data and show it
      changeNaviData(intent.getExtras(), false);
    } else
      /// pass standard intent for widget
      super.onReceive(context, intent);
  }


  static void updateAppWidget(Context appContext,
                              AppWidgetManager appWidgetManager,
                              int appWidgetId) {
    /// build view objects & use it
    RemoteViews rv = buildRemoteViews(appContext, appWidgetId);
    RemoteViewsStorage.addRemoteViews(appWidgetId, rv);
    /// redraws widget in order to show real RSS data (just like we've made navigate next in all widgets)
    changeWidgetDataItem(appWidgetId, true);
  }

  @Override
  public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
    for (int appWidgetId : appWidgetIds) {
      updateAppWidget(context.getApplicationContext(), appWidgetManager, appWidgetId);
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

    Log.d(LOG_TAG, "onEnabled()");

    Context appContext = context.getApplicationContext();
    mAppWidgetManager = AppWidgetManager.getInstance(appContext);
    mComponentName = new ComponentName(appContext, RssWidget.class);
    Log.d(LOG_TAG,
        "onEnabled() - mAppWidgetManager=" + mAppWidgetManager +
            ", mComponentName=" + mComponentName );

    /// load params for ALL widgets
    RssWidgetConfigureActivity.RSSConfigData rssConfigData =
        RssWidgetConfigureActivity.loadPrefs(appContext);
    sRssUrl = rssConfigData.rssUrl;
    sRssTime = rssConfigData.rssTime;

    /// get access to global RSS data
    sDataStorage = DataStorage.getInstance(appContext);

    sAlarmManager = (AlarmManager) appContext.getSystemService(Context.ALARM_SERVICE);
    sRefreshDataIntent = new Intent(appContext, RssService.class);
    sRefreshDataIntent.setAction(Constants.Actions.SERVICE_GET_DATA);
    sRefreshDataIntent.putExtra(Constants.Extras.SERVICE_EXTRA_PARAM_RSS_URL, sRssUrl);
    context.startService(sRefreshDataIntent); /// first start
  }

  @Override
  public void onDisabled(Context context) {
    super.onDisabled(context);
    RemoteViewsStorage.clear();
    if (sDataStorage != null)
      sDataStorage.clear();
    stopCycleRefresh(context.getApplicationContext()); /// stop cyclical alarm signalling
    sRefreshDataIntent = new Intent(context.getApplicationContext(), RssService.class);
    context.stopService(sRefreshDataIntent);

    /// when removing LAST widget, delete the preference associated
//    RssWidgetConfigureActivity.deletePrefs(context.getApplicationContext());
  }

  public static void startCycleRefresh(Context appContext, String rssUrl, int rssTime) {
    if (sAlarmManager == null)
      return;
    /// reconfigure url right over existing intent param
    sRefreshDataIntent.putExtra(Constants.Extras.SERVICE_EXTRA_PARAM_RSS_URL, rssUrl);
    Log.d(LOG_TAG, "startCycleRefresh() - rssTime=" + rssTime);
    sAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
        rssTime*1000, PendingIntent.getService(
            appContext, 0, sRefreshDataIntent, PendingIntent.FLAG_CANCEL_CURRENT));
  }

  private void stopCycleRefresh(Context appContext) {
    if (sAlarmManager == null)
      return;
    Log.d(LOG_TAG, "stopCycleRefresh()");
    sAlarmManager.cancel(PendingIntent.getService(
        appContext, 0, sRefreshDataIntent, PendingIntent.FLAG_CANCEL_CURRENT));
  }

  private static RemoteViews buildRemoteViews(Context appContext, int appWidgetId) {
    RemoteViews rv = new RemoteViews(
        appContext.getPackageName(), R.layout.rssreader_widget_layout);
    rv.setTextViewText(R.id.tvTitle, appContext.getString(R.string.tvEmptyTitleText));
    rv.setTextViewText(R.id.tvDescr, appContext.getString(R.string.tvEmptyDescrText));

    /// bind the click intent for the rss item navigation buttons on the widget
    Intent navigateIntentNext = new Intent(appContext, RssWidget.class);
    navigateIntentNext.setAction(Constants.Actions.WIDGET_NAVIGATE_NEXT);
    navigateIntentNext.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
    rv.setOnClickPendingIntent(
        R.id.ibNext, PendingIntent.getBroadcast(
            appContext, appWidgetId, navigateIntentNext, PendingIntent.FLAG_UPDATE_CURRENT
        )
    );
    Intent navigateIntentPrev = new Intent(navigateIntentNext);
    navigateIntentPrev.setAction(Constants.Actions.WIDGET_NAVIGATE_PREV);
    rv.setOnClickPendingIntent(
        R.id.ibPrev, PendingIntent.getBroadcast(
            appContext, appWidgetId, navigateIntentPrev, PendingIntent.FLAG_UPDATE_CURRENT
        )
    );

    /// bind config button with config. activity
    final Intent configIntent = new Intent(appContext, RssWidgetConfigureActivity.class);
    configIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
    configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
    PendingIntent configPendingIntent =
        PendingIntent.getActivity(appContext, 0, configIntent, 0);
    rv.setOnClickPendingIntent(R.id.ibSettings, configPendingIntent);

    return rv;
  }

  /**
   * refreshes RSS items shown in ALL widgets
   */
  private void changeAllWidgetsData() {
    if (sDataStorage != null)
      sDataStorage.updateItemsList(); /// replace data in storage

    if (mComponentName != null) {
      /// redraws ALL widgets (just like we've made navigate next in all widgets)
      int[] appWidgetIds = mAppWidgetManager.getAppWidgetIds(mComponentName);
      for (int appWidgetId : appWidgetIds) {
        changeWidgetDataItem(appWidgetId, true);
      }
    }
  }

  /**
   * gets data item from the storage for the widget and direction pointed
   * updates data views and redraws the widget
   * @param appWidgetId
   * @param nextFlag
   */
  private static void changeWidgetDataItem(int appWidgetId, boolean nextFlag) {
    if (sDataStorage == null)
      return;
    Log.d(LOG_TAG, "changeWidgetDataItem(" + appWidgetId + ") flag=" + nextFlag);

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
   * @param bundle - Bundle containing widget ID and navigation direction
   * @param nextFlag - 'direction to next' flag
   */
  private void changeNaviData(@NonNull Bundle bundle, boolean nextFlag) {
    if (bundle == null)
      return;
    int appWidgetId = bundle.getInt(
        AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
    changeWidgetDataItem(appWidgetId, nextFlag);
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

