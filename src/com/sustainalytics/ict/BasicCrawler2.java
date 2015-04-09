package com.sustainalytics.ict;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.http.Header;

/**
 * @author Yasser Ganjisaffar [lastname at gmail dot com]
 */
public class BasicCrawler2 extends WebCrawler {

  private final static Pattern BINARY_FILES_EXTENSIONS =
        Pattern.compile(".*\\.(bmp|gif|jpe?g|png|tiff?|ico|xaml|pict|rif|pptx?|ps" +
        "|mid|mp2|mp3|mp4|wav|wma|au|aiff|flac|ogg|3gp|aac|amr|au|vox" +
        "|avi|mov|mpe?g|ra?m|m4v|smil|wm?v|swf|aaf|asf|flv|mkv" +
        "|zip|rar|gz|7z|aac|ace|alz|apk|arc|arj|dmg|jar|lzip|lha)" +
        "(\\?.*)?$"); // For url Query parts ( URL?q=... )

  /**
   * You should implement this function to specify whether the given url
   * should be crawled or not (based on your crawling logic).
   */
  @Override
  public boolean shouldVisit(WebURL url) {
    String href = url.getURL().toLowerCase();

    return !BINARY_FILES_EXTENSIONS.matcher(href).matches() && href.contains("annies");
    //return true;
  }

  /**
   * This function is called when a page is fetched and ready to be processed
   * by your program.
   */
  @Override
  public void visit(Page page) {
    int docid = page.getWebURL().getDocid();
    String url = page.getWebURL().getURL();
    String domain = page.getWebURL().getDomain();
    String path = page.getWebURL().getPath();
    String subDomain = page.getWebURL().getSubDomain();
    String parentUrl = page.getWebURL().getParentUrl();
    String anchor = page.getWebURL().getAnchor();
    

    System.out.println("Docid: " +  docid);
    System.out.println("URL: " + url);
    System.out.println("Domain: " + domain);
    System.out.println("Sub-domain: " + subDomain);
    System.out.println("Path: "+ path);
    System.out.println("Parent page: "+ parentUrl);
    System.out.println("Anchor text: "+ anchor);

    if (page.getParseData() instanceof HtmlParseData) {
      HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
      String text = htmlParseData.getText();
      String html = htmlParseData.getHtml();
      List<WebURL> links = htmlParseData.getOutgoingUrls();

      System.out.println("Text: " +  text);
      System.out.println("Text length: " +  text.length());
      System.out.println("Html length: " +  html.length());
      System.out.println("Number of outgoing links: {}" + links.size());
    }
  }
}
