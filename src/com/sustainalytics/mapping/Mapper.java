package com.sustainalytics.mapping;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.url.WebURL;

/**
 * @author Sustainalytics
 * @version 4.0.0 May 27 2015 (new branch from PDFCrawler.java)
 * 
 *          Crawler to map entire website and record time to crawl each web page
 * 
 *          CHANGE:
 *          -Big functionality change. This is a mapper class that takes a URL
 *          and maps the entire website and records the time for the crawler to visi
 *          each web page.
 * 
 * 
 */
public class Mapper extends WebCrawler {
	// -------------------------------------------------------------------------------------------------------------------
	// Variable section
	// -------------------------------------------------------------------------------------------------------------------

	// Some other parameters
	private static File storageFolder; //fixed
	private static File folder;//read from input
	private boolean isRedirect = false;
	private String redirectURL = "";//store redirected url if any
	private static URL startingURL;//company url
	private static String startingDomain;//domain of company url

	//for tracking time to visit a url
	private static String visitDuration = "";
	//for keeping track of the links within links
	private static String links = "";

	// -------------------------------------------------------------------------------------------------------------------
	// Method Section
	// -------------------------------------------------------------------------------------------------------------------
	/**
	 * Method to set the starting url and domain, storage folder and company
	 * folder
	 * 
	 * @param domain
	 *            is the company url to start with
	 * @param storageFolderName
	 *            is a string shows where to store the company folder
	 * @param companyFolderName
	 *            is a string that is taken from the user to store the pdf and
	 *            html files
	 */
	public static void configure(String domain, String storageFolderName,
			String companyFolderName) {

		/*Extracting the domain name of the starting or company url*/
		try {
			startingURL = new URL(domain);
			startingDomain = startingURL.getHost();
			if(startingDomain.startsWith("www")){
				startingDomain = startingDomain.substring(4);
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		/*setting storage folder*/
		storageFolder = new File(storageFolderName);

		if (!storageFolder.exists()) {
			storageFolder.mkdirs();
		}

		/*setting company folders*/
		folder = new File(storageFolder.getAbsolutePath() + "/"
				+ companyFolderName); // company folder within storage folder

		if (!folder.exists()) {
			folder.mkdirs();
		}

	}// end method to configure the crawler

	/**
	 * Method that overrides the crawler's shouldVisit() method
	 */
	@Override
	public boolean shouldVisit(Page page, WebURL url) {

		String href = url.getURL().toLowerCase();
		/*Let us get the current url and its domain--->*/
		URI currentURL = null;

		try {
			currentURL = new URI(href);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		String currentDomain = currentURL.getHost();

		if(currentDomain.startsWith("www")){
			currentDomain = currentDomain.substring(4);
		}
		//<---getting current url's domain is done!

		/*What if the starting url is a redirection?*/
		if(!isRedirect){
			redirectURL = href;
			isRedirect = true;
		}

		/*
		 * Do not crawl pages that are outside of the domain name
		 */
		if (startingURL.getHost().equals(currentURL.getHost()) || href.startsWith(redirectURL)) {
			return true;
		}

		/*
		 * No if condition met? Then do not crawl!
		 */
		return false;

	}// end overridden shouldVisit() method

	/**
	 * Visit Method. visit a page and if certain conditions met, then download
	 * the htmls. If PDFs are found then downlode them. Job done!
	 */
	@Override
	public void visit(Page page) {

		/*Timing the visit in nano seconds, we will convert it into milliseconds later ---> */
		long startVisit = System.nanoTime();

		String url = page.getWebURL().getURL();

		/*Let us record the outgoing links from the url --->*/
		Set<WebURL> outgoingLinks = page.getParseData().getOutgoingUrls();
		String setOfLinks = "\t\t";
		setOfLinks += StringUtils.join(outgoingLinks, "\n\t\t");

		links += url + "\n" + setOfLinks + "\n";

		//<--- recording of outgoing links from current url is done!
		
		long endVisit = System.nanoTime();
		long elapsedTime = (endVisit - startVisit) / 1000;		
		visitDuration += url + " , " + elapsedTime + "\n";
		//<--- timing+recording of visit duration is done (in milliseconds)

	}// end overridden visit() method

	/**
	 * Static Method to write the log file that contains newly written PDFs and
	 * HTMLs
	 */
	public static void writeLogFile() {

		File durationFile = new File(folder.getAbsolutePath() + "/" + "duration.txt");

		try {
			FileUtils.write(durationFile, visitDuration, null);
			visitDuration = "";
		} catch (IOException e) {
			System.out.println("Error writing Duration File");
		}

		File linkMapFile = new File(folder.getAbsolutePath() + "/" + "link.txt");

		try {
			FileUtils.write(linkMapFile, links, null);
			links = "";
		} catch (IOException e) {
			System.out.println("Error writing Duration File");
		}

	}// end method that writes newly written pdf and htmls to a log file

}// end class