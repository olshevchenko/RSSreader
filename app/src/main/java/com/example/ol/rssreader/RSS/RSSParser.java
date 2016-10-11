package com.example.ol.rssreader.RSS;

import android.provider.SyncStateContract;
import android.util.Xml;

import com.example.ol.rssreader.Constants;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by shevchenko_oi on 11.10.2016.
 */

public class RSSParser {

  private final String ns = null;

  public List<RSSItem> parse(InputStream inputStream) throws XmlPullParserException, IOException {
    try {
      XmlPullParser parser = Xml.newPullParser();
      parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
      parser.setInput(inputStream, null);
      parser.nextTag();
      return readFeed(parser);
    } finally {
      inputStream.close();
    }
  }

  private List<RSSItem> readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
    parser.require(XmlPullParser.START_TAG, null, "rss");
    String title = null;
    String link = null;
    String description = null;
    List<RSSItem> items = new ArrayList<>();
    while (parser.next() != XmlPullParser.END_DOCUMENT) {
      if (parser.getEventType() != XmlPullParser.START_TAG) {
        continue;
      }
      String name = parser.getName();
      if (Constants.Rss.RSS_PARSER_TITLE_NAME.equals(name)) { //"title"
        title = readTitle(parser);
      } else if (Constants.Rss.RSS_PARSER_DESCRIPTION_NAME.equals(name)) { //"description"
        description = readDescription(parser);
      } else if (Constants.Rss.RSS_PARSER_LINK_NAME.equals(name)) { //"link"
        link = readLink(parser);
      }
      if (title != null && description != null) {
        RSSItem item = new RSSItem(title, description);
        items.add(item);
        title = null;
        description = null;
      }
    }
    return items;
  }

  private String readTitle(XmlPullParser parser) throws XmlPullParserException, IOException {
    parser.require(XmlPullParser.START_TAG, ns, Constants.Rss.RSS_PARSER_TITLE_NAME);
    String title = readText(parser);
    parser.require(XmlPullParser.END_TAG, ns, Constants.Rss.RSS_PARSER_TITLE_NAME);
    return title;
  }

  private String readDescription(XmlPullParser parser) throws XmlPullParserException, IOException {
    parser.require(XmlPullParser.START_TAG, ns, Constants.Rss.RSS_PARSER_DESCRIPTION_NAME);
    String description = readText(parser);
    parser.require(XmlPullParser.END_TAG, ns, Constants.Rss.RSS_PARSER_DESCRIPTION_NAME);
    return description;
  }

  private String readLink(XmlPullParser parser) throws XmlPullParserException, IOException {
    parser.require(XmlPullParser.START_TAG, ns, Constants.Rss.RSS_PARSER_LINK_NAME);
    String link = readText(parser);
    parser.require(XmlPullParser.END_TAG, ns, Constants.Rss.RSS_PARSER_LINK_NAME);
    return link;
  }

  /**
   * for the string tags like title, description, link, extracts their text values
   */
  private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
    String result = "";
    if (parser.next() == XmlPullParser.TEXT) {
      result = parser.getText();
      parser.nextTag();
    }
    return result;
  }
}
