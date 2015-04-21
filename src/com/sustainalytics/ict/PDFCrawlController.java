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
 * The controller class for the crawler. It has a main method that reads all the company URLs
 * from a source text file and crawls them one by one. The method configures the crawler parameters. Also
 * the method keeps track of the time taken by the crawler
 * @author Sustainalytics
 * @version 1.1 April 20 2015
 *
 */

public class PDFCrawlController {

	public static void main(String[] args) throws Exception {
		/*
		 * Configuration storage, no. of crawlers to start with, storage folder for the downloaded PDFs
		 */
		String rootFolder = "crawler4jStorage";
		int numberOfCrawlers = 10;
		String storageFolder = "pdfstorage";


		//Apache Commons IO usage for getting the urls from a source text file --->
		File urlFile = new File("input/urls-test.txt");
		String url = "";
		try {
			url = FileUtils.readFileToString(urlFile);

		} catch (IOException e) {
			System.out
			.println("Error to read urls from text file");
			e.printStackTrace();
		}

		String[] urls = url.trim().split("\n");
		//<--- url reading done
		Instant start = Instant.now(); //start timer

		//for each url--->
		for (String domain: urls){	

			//Let's configure the crawler--->
			CrawlConfig config = new CrawlConfig();
			config.setCrawlStorageFolder(rootFolder); //configuration folder
			config.setIncludeBinaryContentInCrawling(true); //PDFs are binary contents
			config.setPolitenessDelay(1); //We are very unpolite. In miliseconds
			config.setMaxDepthOfCrawling(10); //Infinite depth
			config.setMaxPagesToFetch(-1); //Infite fetching of pges
			config.setIncludeHttpsPages(true); //We move to secured http as well
			config.setMaxDownloadSize(20000000); //Quite a huge file size. In bytes

			PageFetcher pageFetcher = new PageFetcher(config);
			RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
			RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
			CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);

			controller.setCustomData(domain.trim());
			controller.addSeed(domain.trim());

			//<---configuration done!
			//Set the crawler with all the configurations and start it
			PDFCrawler.configure(domain.trim(), storageFolder);
			controller.start(PDFCrawler.class, numberOfCrawlers);

		}//<---end looping through all the URLs

		Instant end = Instant.now();
		System.out.println("Completion time: " + Duration.between(start, end));

	}//end main

}//end class