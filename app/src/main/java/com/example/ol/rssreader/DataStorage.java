package com.example.ol.rssreader;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.ol.rssreader.RSS.RSSItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by ol on 02.10.16.
 */

/**
 * non thread-safe singleton (that's enough for now)
 */
public class DataStorage {
  /// for logging
  private static final String LOG_TAG = DataStorage.class.getName();

  private static DataStorage sInstance = null;

  /// map for list position storage (per widget)
  /// the Key is widget ID
  private HashMap<Integer, Integer> positionsMap = new HashMap<>();

  /// (RSSItem) list for RSS data storage
  private List<RSSItem> itemsList = new ArrayList<>(); /// working list for widget navigation
  private List<RSSItem> updateList = null; /// list for RSS updates temporary storage

  private int mItemsHashCode = 1; /// cumulative hash code for ALL working items
  private int mUpdateHashCode = 1; /// hash code for NEW (update) list

  private Context mContext;
  private RSSItem mEmptyItem = null;

  private DataStorage(@NonNull Context context) {
    mContext = context;
    mEmptyItem = new RSSItem(
        mContext.getString(R.string.tvEmptyDescrText),
        mContext.getString(R.string.tvEmptyTitleText));
  }

  public static DataStorage getInstance(Context context) {
    if (sInstance == null)
      sInstance = new DataStorage(context);
    return sInstance;
  }

  /**
   * gets RSS item for the new widget (whether next or previous) position in the item list
   * stores new position for further use if succeeded
   * adds starting position for NEW widget
   * @param appWidgetId
   * @param isNext - flag for Next / Prev movement
   * @return <EmptyItem> if data absents
   *         [0] item if widget hasn't been stored yet
   *         original (last) item if new position is out of bounds
   *         new item if succeeded
   */
  public @NonNull RSSItem getNewItem(int appWidgetId, boolean isNext) {
    int currentPos;
    int newPos;
    RSSItem currentItem;
    RSSItem newItem;
//    Log.d(LOG_TAG, "getNewItem(" + appWidgetId + ")");

    /// set default position, get current if exists and evaluate new one
    if (isNext)
      currentPos = -1;
    else
      currentPos = 1;
    Integer posInt = positionsMap.get(appWidgetId);
    if (posInt != null)
      currentPos = posInt.intValue();
    if (isNext)
      newPos = currentPos + 1;
    else
      newPos = currentPos - 1;

    /// get items for positions
    try {
      currentItem = itemsList.get(currentPos);
    } catch (IndexOutOfBoundsException ex) {
      currentItem = mEmptyItem;
    }
    try {
      newItem = itemsList.get(newPos);
    } catch (IndexOutOfBoundsException ex) {
      newItem = null;
    }

    if (newItem != null) {
      /// full success
      positionsMap.put(appWidgetId, newPos);
      Log.d(LOG_TAG,
          "getNewItem(" + appWidgetId + ")[" + newPos + "]-success: newItem=" + newItem + ", newPos=" + newPos);
      return newItem;
    }

    /// partial success (end of data list): - return currentItem
    /// failure: - return EmptyItem
    /// the same step: - don't store new position
    Log.d(LOG_TAG,
        "=====getNewItem(" + appWidgetId + ")[" + newPos + "]-FAILURE: Item=" + currentItem + ", currentPos=" + currentPos);
    return currentItem;
  }

  public void removeItemPosition(int appWidgetId) {
    Log.d(LOG_TAG, "removeItemPosition()");
    positionsMap.remove(appWidgetId);
  }

  /**
   * replaces data item list reference (to new one) and clears list positions of all widgets
   */
  public void updateItemsList() {
//    Log.d(LOG_TAG, "updateItemsList()");
    if (updateList == null)
      return;
    itemsList = updateList; /// get list with new data
    mItemsHashCode = mUpdateHashCode; ///store new one
    positionsMap.clear(); /// force widgets to later show item[0] by getNewItem()
  }

  public void storeNewData(List<RSSItem> newDataList) {
    if (newDataList == null)
      return;
    updateList = newDataList;
    /// eval new hash
    mUpdateHashCode = 1;
    for (RSSItem item : updateList)
      mUpdateHashCode = mUpdateHashCode*17 + item.hashCode();
  }

  public boolean isHashDifferent() {
    return mItemsHashCode != mUpdateHashCode;
  }

  void clear() {
    itemsList.clear();
    positionsMap.clear();
  }

}
