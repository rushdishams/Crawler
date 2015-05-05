package com.sustainalytics.ict;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

import org.apache.commons.io.FileUtils;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

/**
 * The controller class for the crawler. It has a main method that reads all the
 * company URLs from a source text file and crawls them one by one. The method
 * configures the crawler parameters. Also the method keeps track of the time
 * taken by the crawler
 * 
 * @author Sustainalytics
 * @version 1.1 April 20 2015
 *
 */

public class PDFCrawlController {

	public static void main(String[] args) throws Exception {
		/*
		 * Configuration storage, no. of crawlers to start with, storage folder
		 * for the downloaded PDFs
		 */
		String rootFolder = "Crawler4jStorage";
		int numberOfCrawlers = 10;
		String storageFolder = "apple-to-apple-storage";

		// Apache Commons IO usage for getting the urls from a source text file
		// --->
		File urlFile = new File("input/urls-test.txt");
		String url = "";
		try {
			url = FileUtils.readFileToString(urlFile);

		} catch (IOException e) {
			System.out.println("Error to read urls from text file");
			e.printStackTrace();
		}

		String[] urls = url.trim().split("\n");
		// <--- url reading done
		Instant start = Instant.now(); // start timer

		// for each url--->
		for (int i = 0; i < urls.length - 1; i += 2) {

			// Let's configure the crawler--->
			CrawlConfig config1 = new CrawlConfig();
			CrawlConfig config2 = new CrawlConfig();
			
			config1.setCrawlStorageFolder(rootFolder + "/crawler1"); // configuration folder
			config2.setCrawlStorageFolder(rootFolder + "/crawler2"); // configuration folder
			
			config1.setIncludeBinaryContentInCrawling(true); // PDFs are binary
															// contents
			config2.setIncludeBinaryContentInCrawling(true); // PDFs are binary
			// contents
			config1.setPolitenessDelay(1000); // We are very unpolite. In miliseconds
			config2.setPolitenessDelay(1000); // We are very unpolite. In miliseconds
			
			config1.setMaxDepthOfCrawling(10); // Infinite depth
			config2.setMaxDepthOfCrawling(10); // Infinite depth
			
			config1.setMaxPagesToFetch(-1); // Infite fetching of pges
			config2.setMaxPagesToFetch(-1); // Infite fetching of pges
			
			config1.setIncludeHttpsPages(true); // We move to secured http as
												// well
			config2.setIncludeHttpsPages(true); // We move to secured http as
			// well

			config1.setMaxDownloadSize(20000000); // Quite a huge file size. In
													// bytes
			config2.setMaxDownloadSize(20000000); // Quite a huge file size. In
			// bytes

			PageFetcher pageFetcher1 = new PageFetcher(config1);
			PageFetcher pageFetcher2 = new PageFetcher(config2);
			
			RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
			RobotstxtServer robotstxtServer = new RobotstxtServer(
					robotstxtConfig, pageFetcher1);
			CrawlController controller1 = new CrawlController(config1,
					pageFetcher1, robotstxtServer);
			CrawlController controller2 = new CrawlController(config2,
					pageFetcher2, robotstxtServer);

			controller1.setCustomData(urls[i].trim());
			controller1.addSeed(urls[i].trim());
			
			controller2.setCustomData(urls[i + 1].trim());
			controller2.addSeed(urls[i + 1].trim());
			//System.out.println("=== " + domain.trim() + " ===");
			String[] domain = {urls[i], urls[i+1]};
			// <---configuration done!
			// Set the crawler with all the configurations and start it
			PDFCrawler.configure(domain, storageFolder);
			
			controller1.startNonBlocking(PDFCrawler.class, numberOfCrawlers);
			controller2.startNonBlocking(PDFCrawler.class, numberOfCrawlers);
			
			controller1.waitUntilFinish();
			controller2.waitUntilFinish();

		}// <---end looping through all the URLs

		Instant end = Instant.now();
		System.out.println("Completion time: " + Duration.between(start, end));

	}// end main

}// end class