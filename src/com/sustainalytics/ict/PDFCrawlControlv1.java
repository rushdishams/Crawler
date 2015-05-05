package com.sustainalytics.ict;

import java.time.Duration;
import java.time.Instant;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

public class PDFCrawlControlv1 {
	public static void main(String[] args) throws Exception {
		Instant start = Instant.now();
		
		String rootFolder = "crawler4jStorage";
		int numberOfCrawlers = 10;
		String storageFolder = "pdfstorage";

		CrawlConfig config = new CrawlConfig();
		config.setCrawlStorageFolder(rootFolder);
		/*
		 * Since images are binary content, we need to set this parameter to
		 * true to make sure they are included in the crawl.
		 */
		config.setIncludeBinaryContentInCrawling(true);
		//My configurations-->
		config.setPolitenessDelay(1);
		config.setMaxDepthOfCrawling(10);
		config.setMaxPagesToFetch(-1);
		//config.setIncludeHttpsPages(true);
		config.setMaxDownloadSize(20000000);
		
		
		//<--My configuration ends

		String domain = "http://www.annies.com";

		PageFetcher pageFetcher = new PageFetcher(config);
		RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
		RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
		CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);

		controller.setCustomData(domain);
		controller.addSeed(domain);

		PDFCrawler.configure(domain, storageFolder);

		controller.start(PDFCrawler.class, numberOfCrawlers);
		Instant end = Instant.now();
		System.out.println("Completion time: " + Duration.between(start, end));
	}
}