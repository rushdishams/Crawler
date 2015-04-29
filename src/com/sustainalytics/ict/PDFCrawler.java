package com.sustainalytics.ict;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.common.io.Files;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;


/**
 * @author Sustainalytics
 * @version 3.0 April 29 2015
 * 
 * This class shows how you can crawl PDFs on the web and store them in a
 * folder. Also the program crawls and downloads html pages that contain some specific terms.
 * This version also has significant differences than the previous versions.
 * (1) PDF and HTML pages are now saved based on their original names
 * (2) If the name contains special characters that are not allowed in Windows file system then the program has
 * mechanism to resolve it.
 * (3)If the file already exists in the directory, don't download; otherwise download. This works as a Change
 * Detection and Notification (CDN) for PDFs
 */
public class PDFCrawler extends WebCrawler {
	//-------------------------------------------------------------------------------------------------------------------
	//Variable section
	//-------------------------------------------------------------------------------------------------------------------

	//Where we should not go crawling
	private static final Pattern filters = Pattern.compile(".*\\.(bmp|ashx|gif|jpe?g|png|tiff?|ico|xaml|pict|rif|pptx?|ps" +
			"|mid|mp2|mp3|mp4|wav|wma|au|aiff|flac|ogg|3gp|aac|amr|au|vox" +
			"|avi|mov|mpe?g|ra?m|m4v|smil|wm?v|swf|aaf|asf|flv|mkv" +
			"|zip|rar|gz|7z|aac|ace|alz|apk|arc|arj|dmg|jar|lzip|lha|css|js|bmp|gif|jpe?g|png|tiff?)" +
			"(\\?.*)?$"); // For url Query parts ( URL?q=... )
	//Target patterns for PDFs
	private static final Pattern pdfPatterns = Pattern.compile(".*(\\.(pdf?))$");
	//Some other parameters
	private static File storageFolder;
	private static String crawlDomain = "";
	private static File folder;
	//For naming unnamed html and pdf. It was not used in V3.0 but we have plans to work on this later
	private int htmlSequence = 0, pdfSequence = 0;

	//-------------------------------------------------------------------------------------------------------------------
	//Method Section
	//-------------------------------------------------------------------------------------------------------------------
	/**
	 * Method to set the configuration parameters of the crawler
	 * @param domain is the company url to start with
	 * @param storageFolderName is a string shows where to store the downloaded pdfs
	 */
	public static void configure(String domain, String storageFolderName) {

		PDFCrawler.crawlDomain = domain;
		storageFolder = new File(storageFolderName);

		if (!storageFolder.exists()) {
			storageFolder.mkdirs();
		}

		folder = new File(storageFolder.getAbsolutePath() + "/" + crawlDomain.substring(7)); //get rid of the "HTTP://" part. The folders name will be the company url

		if (!folder.exists()) {
			folder.mkdirs();
		}
	}//end method to configure the crawler

	/**
	 * Method that overrides the crawler's shouldVisit() method
	 */
	@Override
	public boolean shouldVisit(Page page, WebURL url) {

		String href = url.getURL().toLowerCase();

		/*
		 *Do not crawl pages that contain the filter RegExes
		 */
		if (filters.matcher(href).matches()) {
			return false;
		}
		/*
		 *Do not crawl pages that are outside of the domain name
		 */
		if (href.startsWith(crawlDomain)) {
			return true;
		}
		/*
		 *PDFs? Perfect!
		 */
		if (pdfPatterns.matcher(href).matches()) {
			return true;
		}   

		/*
		 *No if condition met? Then do not crawl!
		 */
		return false;
	}//end overridden shouldVisit() method

	/**
	 * Visit Method. visit a page and if PDF is found, download and store. Job done! 
	 */
	@Override
	public void visit(Page page) {

		String url = page.getWebURL().getURL();

		//if the parsed data is HTML, let's check if it contains certain terms of interest--->
		if (page.getParseData() instanceof HtmlParseData) {

			HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
			String text = htmlParseData.getText();

			//Conditions to match a few terms--->
			if(StringUtils.containsIgnoreCase(text, "sust") || StringUtils.containsIgnoreCase(text, "csr") || StringUtils.containsIgnoreCase(text, "report") || StringUtils.containsIgnoreCase(text, "responsibility") || StringUtils.containsIgnoreCase(text, "social") || StringUtils.containsIgnoreCase(text, "society")|| StringUtils.containsIgnoreCase(text, "energy") || StringUtils.containsIgnoreCase(text, "community") || StringUtils.containsIgnoreCase(text, "human rights") || StringUtils.containsIgnoreCase(text, "child labor") || StringUtils.containsIgnoreCase(text, "sust") || StringUtils.containsIgnoreCase(text, "sppl")){
				//System.out.println("-- Regular Expression Matched, Storing HTML --");

				//Storing HTML--->
				String hashedName = FilenameUtils.getBaseName(url).replaceAll("[^a-zA-Z0-9.-]", ""); //getting rid of characters not allowed in Windows file names
				String filename = folder.getAbsolutePath() + "/" + hashedName + ".html";
				File output = new File(filename); //output file
				//if the file does not exist, it is a new file. Download --->
				if (!output.exists()){
					
					System.out.println("---A new file is found " + filename + " ---");
					try {
						Files.write(page.getContentData(), new File(filename));
					} catch (IOException iox) {
						System.out.println("Error storing HTMLs");
					}
					
				}//<--- the html is new

				//<---HTML storing done

			}//<---selection of webpage to download is done

		}//<---yes, the data was HTML

		/*
		 *Return if there is no PDF
		 */
		if (!pdfPatterns.matcher(url).matches()) {

			return;

		}

		// Naming the PDF file
		String extension = url.substring(url.lastIndexOf("."));
		String hashedName = FilenameUtils.getBaseName(url).replaceAll("[^a-zA-Z0-9.-]", "") + extension; //removing characters not allowed in Windows file system
		String filename = folder.getAbsolutePath() + "/" + hashedName;
		File output = new File(filename); //output file
		//if the file is new--->
		if(!output.exists()){
		
			System.out.println("---A new file is found " + filename + " ---");
			
			// store PDF--->
			try {
				Files.write(page.getContentData(), new File(filename));
			} catch (IOException iox) {
				System.out.println("Error storing PDFs");
			}
		
		}//<--- the file was new
		
		//<--- Done storing PDF!

	}//end overridden visit() method

}//end class