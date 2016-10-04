package com.example.ol.rssreader;

/**
 * Constants necessary for operation
 */
public final class Constants {

  public class Prefs {
    public static final String NAME = "com.example.ol.rssreader.RssWidget";
    public static final String RSS_URL_KEY = "rss_url";
    public static final String RSS_UPDATE_INTERVAL_KEY = "rss_time";
  }

  public class Extras {
    public static final String SERVICE_EXTRA_PARAM_RSS_URL = "com.example.ol.rssreader.extra.rss_url";
    public static final String SERVICE_EXTRA_PARAM_RSS_UPDATE_INTERVAL = "com.example.ol.rssreader.extra.rss_time";
    public static final String WIDGET_NAVIGATE_NEXT_FLAG = "com.example.ol.rssreader.widget_navigate_next_flag";
  }

  public class Actions {
    public static final String SERVICE_GET_DATA = "com.example.ol.rssreader.service_get_data";
    public static final String SERVICE_DATA_CHANGED = "com.example.ol.rssreader.service_data_changed";
    public static final String WIDGET_NAVIGATE = "com.example.ol.rssreader.widget_navigate";
  }

  public class Rss {
    public static final String RSS_URL = "https://habrahabr.ru/rss";
    public static final int RSS_UPDATE_INTERVAL = 10;
  }

  public class DB {

    public static final String DB_NAME = "mydb";
    public static final int DB_VERSION = 1;
    public static final String DB_TABLE = "mytab";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_DESCR = "descr";
  }

  public class Errors {
    public static final String CURSOR_NPE = "RSS DB cursor is null";
  }

  public class Widget {
    public static final String REFRESH_THREAD_NAME = "WidgetRefreshDataThread";
  }
} //class Constants



