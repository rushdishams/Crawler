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
public class BasicCrawler extends WebCrawler {

  private final static Pattern filters =
        Pattern.compile(".*\\.(bmp|gif|jpe?g|png|tiff?|pdf|ico|xaml|pict|rif|pptx?|ps" +
        "|mid|mp2|mp3|mp4|wav|wma|au|aiff|flac|ogg|3gp|aac|amr|au|vox" +
        "|avi|mov|mpe?g|ra?m|m4v|smil|wm?v|swf|aaf|asf|flv|mkv" +
        "|zip|rar|gz|7z|aac|ace|alz|apk|arc|arj|dmg|jar|lzip|lha)" +
        "(\\?.*)?$"); // For url Query parts ( URL?q=... )

  /**
   * You should implement this function to specify whether the given url
   * should be crawled or not (based on your crawling logic).
   */
  public boolean shouldVisit(WebURL url) {
    String href = url.getURL().toLowerCase();
    System.out.println("Domain: " + url.getDomain() );
    return !filters.matcher(href).matches() && href.startsWith("http://en.wikipedia.org/wiki/Main_Page");
  }

  /**
   * This function is called when a page is fetched and ready to be processed
   * by your program.
   */

  public void visit(Page page) {
	  int docid = page.getWebURL().getDocid();
	  
          String url = page.getWebURL().getURL();
          
          System.out.println("URL: " + url);
          

          if (page.getParseData() instanceof HtmlParseData) {
                  HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
                  String text = htmlParseData.getText();
                  String html = htmlParseData.getHtml();
                  List<WebURL> links = htmlParseData.getOutgoingUrls();

                  System.out.println("Text length: " + text.length());
                  System.out.println("Html length: " + html.length());
                  System.out.println("Number of outgoing links: " + links.size());
          }
  

    Header[] responseHeaders = page.getFetchResponseHeaders();
    if (responseHeaders != null) {
      logger.debug("Response headers:");
      for (Header header : responseHeaders) {

      }
    }

    logger.debug("=============");
  }
}