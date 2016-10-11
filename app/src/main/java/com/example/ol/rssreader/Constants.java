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
    public static final String WIDGET_NAVIGATE_NEXT = "com.example.ol.rssreader.widget_navigate_next";
    public static final String WIDGET_NAVIGATE_PREV = "com.example.ol.rssreader.widget_navigate_prev";
  }

  public class Rss {
    public static final String RSS_URL = "https://habrahabr.ru/rss";
    public static final int RSS_UPDATE_INTERVAL = 60; ///(sec.) between rss data updates
    public static final String RSS_PARSER_TITLE_NAME = "title";
    public static final String RSS_PARSER_DESCRIPTION_NAME = "description";
    public static final String RSS_PARSER_LINK_NAME = "link";
  }

  public class Errors {
  }

  public class Widget {
  }
} //class Constants



