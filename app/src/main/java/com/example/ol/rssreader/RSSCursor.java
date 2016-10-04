package com.example.ol.rssreader;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.ol.rssreader.RSS.RSSItem;

/**
 * Created by ol on 9/22/16.
 */
public class RSSCursor {

  private Cursor mCursor;

  public RSSCursor(DB db) {
    loadChannel(db);
  }

  /**
   *
   * @param db - database storage for RSS items
   * @return true if there is any information (RSS items)
   */
  public boolean loadChannel(DB db) {
    mCursor = db.getAllData();
    db.close();
    if ((mCursor != null) && (mCursor.moveToFirst()))
      return true;
    else
      return false;
  }

  public boolean isFirstItem() throws NullPointerException {
    if (mCursor == null)
      throw new NullPointerException(Constants.Errors.CURSOR_NPE);
    return mCursor.isFirst();
  }

  public boolean isLastItem() throws NullPointerException {
    if (mCursor == null)
      throw new NullPointerException(Constants.Errors.CURSOR_NPE);
    return mCursor.isLast();
  }

  private RSSItem getItem() {
    String title = mCursor.getString(mCursor.getColumnIndex(Constants.DB.COLUMN_TITLE));
    String descr = mCursor.getString(mCursor.getColumnIndex(Constants.DB.COLUMN_DESCR));
    return new RSSItem(title, descr);
  }

  public RSSItem getCurrentItem() {
    if (mCursor == null)
      throw new NullPointerException(Constants.Errors.CURSOR_NPE);
    return getItem();
  }

  public RSSItem getPrevItem() {
    if (mCursor == null)
      throw new NullPointerException(Constants.Errors.CURSOR_NPE);
    mCursor.moveToPrevious();
    return getItem();
  }

  public RSSItem getNextItem() {
    if (mCursor == null)
      throw new NullPointerException(Constants.Errors.CURSOR_NPE);
    mCursor.moveToNext();
    return getItem();
  }
}
