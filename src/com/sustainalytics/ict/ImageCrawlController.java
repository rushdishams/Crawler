package com.sustainalytics.ict;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

public class ImageCrawlController {
    private static Logger logger = LoggerFactory.getLogger(ImageCrawlController.class);

  public static void main(String[] args) throws Exception {


    String rootFolder = "crawler4jStorage";
    int numberOfCrawlers = 5;
    String storageFolder = "secondstorage";

    CrawlConfig config = new CrawlConfig();

    config.setCrawlStorageFolder(rootFolder);
  
    /*
     * Since images are binary content, we need to set this parameter to
     * true to make sure they are included in the crawl.
     */
    config.setIncludeBinaryContentInCrawling(true);
    
    //My configurations-->
    config.setPolitenessDelay(1);
    config.setMaxDepthOfCrawling(3);
    config.setMaxPagesToFetch(-1);

    String[] crawlDomains = new String[] { "http://www.annies.com/" };

    PageFetcher pageFetcher = new PageFetcher(config);
    RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
    RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
    CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);
    for (String domain : crawlDomains) {
      controller.addSeed(domain);
    }

    ImageCrawler.configure(crawlDomains, storageFolder);

    controller.start(ImageCrawler.class, numberOfCrawlers);
  }
}