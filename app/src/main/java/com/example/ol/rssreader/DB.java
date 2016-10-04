package com.example.ol.rssreader;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

import com.example.ol.rssreader.RSS.RSSItem;

import java.util.List;

import static com.example.ol.rssreader.Constants.DB.COLUMN_DESCR;
import static com.example.ol.rssreader.Constants.DB.COLUMN_ID;
import static com.example.ol.rssreader.Constants.DB.COLUMN_TITLE;
import static com.example.ol.rssreader.Constants.DB.DB_NAME;
import static com.example.ol.rssreader.Constants.DB.DB_TABLE;
import static com.example.ol.rssreader.Constants.DB.DB_VERSION;

/**
 * Created by ol on 9/21/16.
 */
public class DB {

  private static final String DB_CREATE =
      "create table " + DB_TABLE + "(" +
          COLUMN_ID + " integer primary key autoincrement, " +
          COLUMN_TITLE + " text, " +
          COLUMN_DESCR + " text" +
          ");";

  private final Context mCtx;

  private DBHelper mDBHelper = null;
  private SQLiteDatabase mDB = null;

  private int mCumHashCode = 1; /// cumulative hash code for ALL <DB_TABLE> records

  public DB(Context ctx) {
    mCtx = ctx;
    mDBHelper = new DBHelper(mCtx, DB_NAME, null, DB_VERSION);
  }

  public int getCumHashCode() {
    return mCumHashCode;
  }

  public void open() {
    mDB = mDBHelper.getWritableDatabase();
  }

  public void close() {
    if (mDBHelper!=null)
      mDBHelper.close();
  }

  public Cursor getAllData() {
    if (mDB == null)
      return null;
    else
      return mDB.query(DB_TABLE, null, null, null, null, null, null);
  }

  public void clear() {
    if (mDB == null)
      return;
    else {
      mDB.delete(DB_TABLE, null, null);
      mCumHashCode = 1;
    }
  }

  /**
   * overwrites table with new RSS items (list)
   * @param rssList
   * @param hashCode - cumulative hash code of new content
   */
  public void fillTabWithRecList(List<RSSItem> rssList, int hashCode) {
    if ((mDB == null) || (rssList == null))
      return;
    clear();
    for (RSSItem rssItem : rssList) {
      addRec(rssItem);
    }
    mCumHashCode = hashCode;
  }

  private void addRec(@NonNull RSSItem rssItem) {
    ContentValues cv = new ContentValues();
    cv.put(COLUMN_TITLE, rssItem.getTitle());
    cv.put(COLUMN_DESCR, rssItem.getDescription());
    mDB.insert(DB_TABLE, null, cv);
    mCumHashCode = mCumHashCode*17 + rssItem.hashCode();
  }

  private class DBHelper extends SQLiteOpenHelper {

    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,
                    int version) {
      super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
      db.execSQL(DB_CREATE);
      for (int i = 1; i < 3; i++)
        addRec(new RSSItem("title " + i, "descr " + i));
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
      super.onDowngrade(db, oldVersion, newVersion);
    }
  }
}
