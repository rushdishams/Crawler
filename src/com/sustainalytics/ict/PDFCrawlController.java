package com.sustainalytics.ict;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.io.FileUtils;

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
 * -u and -f options are deprecated since the requirement now is to 
 * read folder names and urls from text file. The folder name and urls in the text
 * files are provided such as:
 * <folder_name><space><||><space><url> 
 * 
 * 
 * @author Sustainalytics
 * @version 3.8.1 May 21 2015 internal version 2.3
 *
 */

public class PDFCrawlController {

	public static void main(String[] args) throws Exception {

		/* Apache CLI options ---> */
		Options options = new Options();
		options.addOption("p", true,
				"Politeness in Miliseconds (Default 1000), OPTIONAL");
		options.addOption("d", true,
				"Depth of Crawling (Default 10), OPATIONAL");
		options.addOption("h", false, "Help page, OPTIONAL");
		options.addOption("t", true,
				"No. of Threads Per Website (Default 10), OPTIONAL");
		options.addOption("c", true, "Crawler Storage Folder");
		options.addOption("i", true,
				"Input file (with Folder name(s) and URL(s)");

		/* <---Apache CLI options ends */

		/*Parsing CLI options --->*/
		CommandLineParser parser = new PosixParser();
		CommandLine cmd = parser.parse(options, args);
		/*<--- CLI options parsing done*/

		/* The mandatory CLI option---> */
		if (!cmd.hasOption("c") || !cmd.hasOption("i")) {
			System.out
			.println("You must provide Crawler configuration storage folder name and input file that contains folder(s) and URL(s)!");
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(" ", options);
			System.exit(1);
		}
		/* <---the mandatory option handling ends here */

		/* Help section */
		if (cmd.hasOption("h")) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(" ", options);
		}

		/* Get the CLI parameters into variables */
		String politeness = cmd.getOptionValue("p");
		String crawlDepth = cmd.getOptionValue("d");
		String thread = cmd.getOptionValue("t");
		String rootFolder = cmd.getOptionValue("c");
		String inputFile = cmd.getOptionValue("i");

		/* Apache Commons IO usage for getting the urls from a source text file --->*/
		File urlFile = new File(inputFile);
		String content = "";
		try {
			/*Read the entire content of the file at once*/
			content = FileUtils.readFileToString(urlFile); 

		} catch (IOException e) {
			System.out.println("Error to read urls from text file");
		}

		/*This array holds both folder names and urls*/
		String[] lines = content.trim().split("\n");

		/*
		 * Configuration no. of crawlers to start with, storage folder for the downloaded PDFs and htmls --->
		 */

		int numberOfCrawlers;

		if (cmd.hasOption("t")) {
			numberOfCrawlers = Integer.parseInt(thread);
		}
		else {
			numberOfCrawlers = 10;
		}

		String storageFolder = "pdfstorage";

		/*<--- configuration ends*/

		/* Monitoring execution time for performance analysis */
		Instant start = Instant.now();

		/*for each line of the input text file --->*/
		for(String line: lines){
			// Let's configure MORE the crawler--->	
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
			RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
			CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);

			/*In the input file, the folders and urls are separated by < || > delimiter*/
			String[] folderAndUrl = line.split(" \\|\\| ");
			String folder = folderAndUrl[0];
			String url = folderAndUrl[1];

			controller.setCustomData(url.trim());
			controller.addSeed(url.trim());

			// <---configuration done!

			// Set the crawler with all the configurations and start it
			PDFCrawler.configure(url.trim(), storageFolder, folder.trim());
			controller.start(PDFCrawler.class, numberOfCrawlers);

			PDFCrawler.writeLogFile();
		}// <--- exits from here when all the lines of the input file are being read
		
		Instant end = Instant.now();
		System.out.println("Completion time: " + Duration.between(start, end));

	}// end main

}// end class