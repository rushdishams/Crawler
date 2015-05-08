package com.sustainalytics.ict;

import java.time.Duration;
import java.time.Instant;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

/**
 * The controller class for the crawler. It has a big difference with its
 * previous version.
 * 
 * CHANGES:
 * 
 * -c option is introduced because we had some problems running multiple crawler
 * instances with the same storage folder (the folder is locked)
 * 
 * the option takes folder names from command line where it stores its configuration 
 * files
 * 
 * 
 * @author Sustainalytics
 * @version 3.4.0 May 08 2015
 * internal version 2.2
 *
 */

public class PDFCrawlController {

	public static void main(String[] args) throws Exception {
		
		/*Apache CLI options --->*/
		Options options = new Options();
		options.addOption("u", true, "URL to crawl (include http://)");
		options.addOption("f", true, "Folder to store");
		options.addOption("p", true, "Politeness in Miliseconds (Default 1000), OPTIONAL");
		options.addOption("d", true, "Depth of Crawling (Default 10), OPATIONAL");
		options.addOption("h", false, "Help page, OPTIONAL");
		options.addOption("t", true, "No. of Threads Per Website (Default 10), OPTIONAL");
		options.addOption("c", true, "Crawler Storage Folder");
		
		/*<---Apache CLI options ends*/
		CommandLineParser parser = new PosixParser();
		CommandLine cmd = parser.parse(options, args);

		/* The two mandatory CLI options---> */
		if (!cmd.hasOption("u") || !cmd.hasOption("f") || !cmd.hasOption("c")) {
			System.out.println("You must provide both URL and Directory name, and Crawler configuration storage!");
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp( " ", options );
			System.exit(1);
		}
		/*<---the mandatory option handling ends here*/

		/*Help section*/
		if (cmd.hasOption("h")){
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp( " ", options );
		}
		
		/*Get the CLI parameters into variables*/
		String url = cmd.getOptionValue("u");
		String folder = cmd.getOptionValue("f");
		String politeness = cmd.getOptionValue("p");
		String crawlDepth = cmd.getOptionValue("d");
		String thread = cmd.getOptionValue("t");
		String rootFolder = cmd.getOptionValue("c");
		/*
		 * Configuration storage, no. of crawlers to start with, storage folder
		 * for the downloaded PDFs
		 */
		//String rootFolder = "crawler4jStorage";
		int numberOfCrawlers;
		
		if (cmd.hasOption("t")) {
		
			numberOfCrawlers = Integer.parseInt(thread);
		
		}
		
		else{
		
			numberOfCrawlers = 10;
		
		}
		String storageFolder = "pdfstorage";

		/*Monitoring execution time for performance analysis*/
		Instant start = Instant.now();
		
		// Let's configure the crawler--->
		CrawlConfig config = new CrawlConfig();
		config.setCrawlStorageFolder(rootFolder); // configuration folder
		config.setIncludeBinaryContentInCrawling(true); // PDFs are binary contents
		
		if (cmd.hasOption("p")) {
			
			config.setPolitenessDelay(Integer.parseInt(politeness)); // politeness provided by the user (in ms)
		
		} 
		
		else {
		
			config.setPolitenessDelay(1000); // We are very polite by default (1s)
		
		}
		
		if (cmd.hasOption("d")) {
		
			config.setMaxDepthOfCrawling(Integer.parseInt(crawlDepth)); // depth of crawling by the user
		
		} 
		
		else {
		
			config.setMaxDepthOfCrawling(10); // default depth set to 10
		
		}
		
		config.setMaxPagesToFetch(-1); // Infinite fetching of pages
		config.setIncludeHttpsPages(true); // We move to secured http as well
		config.setMaxDownloadSize(20000000); // Quite a huge file size. In bytes

		PageFetcher pageFetcher = new PageFetcher(config);
		RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
		RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig,
				pageFetcher);
		CrawlController controller = new CrawlController(config, pageFetcher,
				robotstxtServer);

		controller.setCustomData(url.trim());
		controller.addSeed(url.trim());

		// <---configuration done!

		// Set the crawler with all the configurations and start it
		PDFCrawler.configure(url.trim(), storageFolder, folder.trim());
		controller.start(PDFCrawler.class, numberOfCrawlers);

		PDFCrawler.writeLogFile();
		
		Instant end = Instant.now();
		System.out.println("Completion time: " + Duration.between(start, end));

	}// end main

}// end class