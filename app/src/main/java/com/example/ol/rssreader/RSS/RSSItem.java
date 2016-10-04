package com.example.ol.rssreader.RSS;

/**
 * Created by ol on 9/22/16.
 */
public class RSSItem {
  String mTitle;
  String mDescription;

  public RSSItem(String title, String description) {
    this.mTitle = title;
    this.mDescription = description;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (!(o instanceof RSSItem))
      return false;

    RSSItem rssItem = (RSSItem) o;

    if (!mTitle.equals(rssItem.mTitle))
      return false;
    return mDescription.equals(rssItem.mDescription);

  }

  @Override
  public int hashCode() {
    int result = mTitle.hashCode();
    result = 31 * result + mDescription.hashCode();
    return result;
  }

  public String getTitle() {
    return mTitle;
  }

  public void setTitle(String title) {
    this.mTitle = title;
  }

  public String getDescription() {
    return mDescription;
  }

  public void setDescription(String description) {
    this.mDescription = description;
  }
}
