package com.example.ol.rssreader.RSS;

import android.net.ParseException;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ol on 09.10.16.
 */

public class FeedParser {

  // Constants indicting XML element names that we're interested in
  private static final int TAG_TITLE = 1;
  private static final int TAG_SUMMARY = 2;

  // We don't use XML namespaces
  private static final String ns = null;

  /**
   * parses RSS feed, returns a collection of Entry objects
   * @param in - stream of RSS feed
   * @return List of RSSItem objects
   */
  public List<RSSItem> parse(InputStream in)
      throws XmlPullParserException, IOException, ParseException {
    try {
      XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
      XmlPullParser parser = factory.newPullParser();
      parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
      parser.setInput(in, null);
      parser.nextTag();
      return readFeed(parser);
    } finally {
      in.close();
    }
  }

  /**
   * decodes a feed attached to an XmlPullParser
   * @param parser Incoming XMl
   * @return List of RSSItem objects
   */
  private List<RSSItem> readFeed(XmlPullParser parser)
      throws XmlPullParserException, IOException, ParseException {
    List<RSSItem> items = new ArrayList<RSSItem>();

    parser.require(XmlPullParser.START_TAG, ns, "feed");
    while (parser.next() != XmlPullParser.END_TAG) {
      if (parser.getEventType() != XmlPullParser.START_TAG) {
        continue;
      }
      String name = parser.getName();
      if (name.equals("entry")) {
        items.add(readEntry(parser));
      } else {
        skip(parser);
      }
    }
    return items;
  }
  /**
   * Parses the contents of an entry. If it encounters a title, summary, or link tag, hands them
   * off to their respective "read" methods for processing. Otherwise, skips the tag.
   */
  private RSSItem readEntry(XmlPullParser parser)
      throws XmlPullParserException, IOException, ParseException {
    parser.require(XmlPullParser.START_TAG, ns, "entry");
    String title = null;
    String summary = null;

    while (parser.next() != XmlPullParser.END_TAG) {
      if (parser.getEventType() != XmlPullParser.START_TAG) {
        continue;
      }
      String name = parser.getName();
      if (name.equals("title")) {
        // Example: <title>Article title</title>
        title = readTag(parser, TAG_TITLE);
      } if (name.equals("description")) {
        // Example: <description>Article summary goes here.</description>
        summary = readTag(parser, TAG_SUMMARY);
      } else {
        skip(parser);
      }
    }
    return new RSSItem(title, summary);
  }

  /**
   * Process an incoming tag and read the selected value from it.
   */
  private String readTag(XmlPullParser parser, int tagType)
      throws IOException, XmlPullParserException {
    String tag = null;
    String endTag = null;

    switch (tagType) {
      case TAG_TITLE:
        return readBasicTag(parser, "title");
      case TAG_SUMMARY:
        return readBasicTag(parser, "summary");
      default:
        throw new IllegalArgumentException("Unknown tag type: " + tagType);
    }
  }

  /**
   * Reads the body of a basic XML tag, which is guaranteed not to contain any nested elements.
   *
   * <p>You probably want to call readTag().
   *
   * @param parser Current parser object
   * @param tag XML element tag name to parse
   * @return Body of the specified tag
   * @throws java.io.IOException
   * @throws org.xmlpull.v1.XmlPullParserException
   */
  private String readBasicTag(XmlPullParser parser, String tag)
      throws IOException, XmlPullParserException {
    parser.require(XmlPullParser.START_TAG, ns, tag);
    String result = readText(parser);
    parser.require(XmlPullParser.END_TAG, ns, tag);
    return result;
  }

  /**
   * for the tags title and summary, extracts their text values.
   */
  private String readText(XmlPullParser parser)
      throws IOException, XmlPullParserException {
    String result = null;
    if (parser.next() == XmlPullParser.TEXT) {
      result = parser.getText();
      parser.nextTag();
    }
    return result;
  }

  /**
   * skips unnecessary tags
   */
  private void skip(XmlPullParser parser)
      throws XmlPullParserException, IOException {

    if (parser.getEventType() != XmlPullParser.START_TAG) {
      throw new IllegalStateException();
    }
    int depth = 1;
    while (depth != 0) {
      switch (parser.next()) {
        case XmlPullParser.END_TAG:
          depth--;
          break;
        case XmlPullParser.START_TAG:
          depth++;
          break;
      }
    }
  }

}
