package com.sustainalytics.ict;

import java.time.Duration;
import java.time.Instant;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

/**
 * The controller class for the crawler. It has a big difference with its
 * previous version.
 * 
 * It has a main method that takes two arguments: (1) URL to crawl (2) Directory
 * to save the files. The method configures the crawler parameters. Also the
 * method keeps track of the time taken by the crawler.
 * 
 * @author Sustainalytics
 * @version 2.0 May 05 2015
 *
 */

public class PDFCrawlController {

	public static void main(String[] args) throws Exception {
		/*
		 * Configuration storage, no. of crawlers to start with, storage folder
		 * for the downloaded PDFs
		 */
		String rootFolder = "crawler4jStorage";
		int numberOfCrawlers = 10;
		String storageFolder = "pdfstorage";

		if (args.length < 2) {
			System.out.println("You must provide URL and Directory name");
			System.exit(1);
		}

		Instant start = Instant.now();
		// Let's configure the crawler--->
		CrawlConfig config = new CrawlConfig();
		config.setCrawlStorageFolder(rootFolder); // configuration folder
		config.setIncludeBinaryContentInCrawling(true); // PDFs are binary
														// contents
		config.setPolitenessDelay(1000); // We are very unpolite. In miliseconds
		config.setMaxDepthOfCrawling(10); // Infinite depth
		config.setMaxPagesToFetch(-1); // Infite fetching of pges
		config.setIncludeHttpsPages(true); // We move to secured http as well
		config.setMaxDownloadSize(20000000); // Quite a huge file size. In bytes

		PageFetcher pageFetcher = new PageFetcher(config);
		RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
		RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig,
				pageFetcher);
		CrawlController controller = new CrawlController(config, pageFetcher,
				robotstxtServer);

		controller.setCustomData(args[0].trim());
		controller.addSeed(args[0].trim());

		// <---configuration done!

		// Set the crawler with all the configurations and start it
		PDFCrawler.configure(args[0].trim(), storageFolder, args[1].trim());
		controller.start(PDFCrawler.class, numberOfCrawlers);

		Instant end = Instant.now();
		System.out.println("Completion time: " + Duration.between(start, end));

	}// end main

}// end class