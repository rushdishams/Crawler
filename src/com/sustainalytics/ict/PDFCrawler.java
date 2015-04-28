package com.sustainalytics.ict;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.google.common.io.Files;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;


/**
 * @author Sustainalytics
 * @version 2.0 April 27 2015
 * 
 * This class shows how you can crawl PDFs on the web and store them in a
 * folder. Also the program crawls and downloads html pages that contain some specific terms.
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

	//Regex patterns for 12 different terms to be found on web pages...
	private static final Pattern regEx1 = Pattern.compile(".*sust.*"),
			regEx2 = Pattern.compile("csr.*"),
			regEx3 = Pattern.compile(".*report.*"),
			regEx4 = Pattern.compile(".*responsibility.*"),
			regEx5 = Pattern.compile(".*social.*"),
			regEx6 = Pattern.compile(".*society.*"),
			regEx7 = Pattern.compile(".*water.*"),
			regEx8 = Pattern.compile(".*energy.*"),
			regEx9 = Pattern.compile(".*community.*"),
			regEx10 = Pattern.compile(".*human rights.*"),
			regEx11 = Pattern.compile(".*child labor.*"),
			regEx12 = Pattern.compile(".*suppl.*")
	;

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
			if(regEx1.matcher(text).matches() || regEx2.matcher(text).matches() || regEx3.matcher(text).matches() || regEx4.matcher(text).matches() || regEx5.matcher(text).matches() || regEx6.matcher(text).matches() || regEx7.matcher(text).matches() || regEx8.matcher(text).matches() || regEx9.matcher(text).matches() || regEx10.matcher(text).matches() || regEx11.matcher(text).matches() || regEx12.matcher(text).matches()){
				System.out.println("-- Regular Expression Matched, Storing HTML --");
				
				//Storing HTML--->
				String hashedName = UUID.randomUUID().toString();
				String filename = folder.getAbsolutePath() + "/" + hashedName + ".html";
				try {
					Files.write(page.getContentData(), new File(filename));
				} catch (IOException iox) {
					System.out.println("Error storing HTMLs");
				}
				//<---HTML storing done
			
			}//<---selection of webpage to download is done
			
		}//<---yes, the data was HTML
		
		/*
		 *Return if there is no PDF
		 */
		if (!pdfPatterns.matcher(url).matches()) {
			
			System.out.println("I am in " +  url + " and leaving.");
			return;
			
		}
		
		System.out.println("--- Found something at " + url + " ---");
		
		// Naming the PDF file
		String extension = url.substring(url.lastIndexOf("."));
		String hashedName = UUID.randomUUID().toString() + extension;
		String filename = folder.getAbsolutePath() + "/" + hashedName;

		// store PDF--->
		try {
			Files.write(page.getContentData(), new File(filename));
		} catch (IOException iox) {
			System.out.println("Error storing PDFs");
		}
		//<--- Done storing PDF!

	}//end overridden visit() method

}//end class