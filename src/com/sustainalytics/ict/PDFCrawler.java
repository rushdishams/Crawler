package com.sustainalytics.ict;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.regex.Pattern;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.common.io.Files;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

/**
 * @author Sustainalytics
 * @version 3.8.1 May 21 2015
 * 
 *          This class shows how you can crawl PDFs on the web and store them in
 *          a folder. Also the program crawls and downloads html pages that
 *          contain some specific terms.
 * 
 *          CHANGE:
 *          Minor change in redirection handling
 *          Major change in html downloading: instead of the text body, we are now looking
 *          into the url for keywords to download htmls
 *          Major change in file name for html: htmls are being saved by using an SHA hashing
 *          algorithm on the urls of the htmls files. So the names are unique.
 * 
 * 
 */
public class PDFCrawler extends WebCrawler {
	// -------------------------------------------------------------------------------------------------------------------
	// Variable section
	// -------------------------------------------------------------------------------------------------------------------

	// Where we should not go crawling
	private static final Pattern filters = Pattern
			.compile(".*\\.(bmp|ashx|gif|jpe?g|png|tiff?|ico|xaml|pict|rif|pptx?|ps"
					+ "|mid|mp2|mp3|mp4|wav|wma|au|aiff|flac|ogg|3gp|aac|amr|au|vox"
					+ "|avi|mov|mpe?g|ra?m|m4v|smil|wm?v|swf|aaf|asf|flv|mkv"
					+ "|zip|rar|gz|7z|aac|ace|alz|apk|arc|arj|dmg|jar|lzip|lha|css|js|bmp|gif|jpe?g|png|tiff?)"
					+ "(\\?.*)?$"); // For url Query parts ( URL?q=... )
	// Target patterns for PDFs
	private static final Pattern pdfPatterns = Pattern
			.compile(".*(\\.(pdf?))$");
	// Some other parameters
	private static File storageFolder; //fixed
	private static File folder;//read from input
	private boolean isRedirect = false;
	private String redirectURL = "";//store redirected url if any
	private static URL startingURL;//company url
	private static String startingDomain;//domain of company url

	// for log file entry
	private static String logEntry = "";
	// for tracking urls from which files are downloaded
	private static String urlEntry = "";
	//for tracking time to visit a url
	private static String visitDuration = "";

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
		 * Do not crawl pages that contain the filter RegExes
		 */
		if (filters.matcher(href).matches()) {
			return false;
		}

		/*
		 * Do not crawl pages that are outside of the domain name
		 */
		if (startingURL.getHost().equals(currentURL.getHost()) || href.startsWith(redirectURL)) {
			return true;
		}

		/*
		 * PDFs? Perfect!
		 */
		if (pdfPatterns.matcher(href).matches()) {
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
		
		long startVisit = System.nanoTime();
		
		String url = page.getWebURL().getURL();

		// if the parsed data is HTML, let's check if it contains certain terms
		// of interest--->
		if (page.getParseData() instanceof HtmlParseData) {

			// Conditions to match a few terms--->
			if (StringUtils.containsIgnoreCase(url, "sust")
					|| StringUtils.containsIgnoreCase(url, "csr")
					|| StringUtils.containsIgnoreCase(url, "report")
					|| StringUtils.containsIgnoreCase(url, "responsibility")
					|| StringUtils.containsIgnoreCase(url, "social")
					|| StringUtils.containsIgnoreCase(url, "society")
					|| StringUtils.containsIgnoreCase(url, "energy")
					|| StringUtils.containsIgnoreCase(url, "community")
					|| StringUtils.containsIgnoreCase(url, "human rights")
					|| StringUtils.containsIgnoreCase(url, "child labor")
					|| StringUtils.containsIgnoreCase(url, "sust")
					|| StringUtils.containsIgnoreCase(url, "sppl")) {

				// Storing HTML--->
				/*
				 * Every html will have a unique name. So, let the name be: url
				 * + base name without characters invalid for windows as file
				 * name
				 */

				
				String htmlBaseName = DigestUtils.shaHex(url);
				String htmlFileName = folder.getAbsolutePath() + "/"
						+ htmlBaseName + ".html";
				/*
				 * The python script will convert all htmls to txts
				 */
				String txtFileName = folder.getAbsolutePath() + "/"
						+ htmlBaseName + ".txt";
				File outputTXT = new File(txtFileName);

				// Let's check whether the crawled html file exists in the
				// directory --->
				/*
				 * If the HTML file is not present in text format (previously
				 * saved by the python code), then download the html, and write
				 * it down in the log file --->
				 */
				if (!outputTXT.exists()) {

					try {
						Files.write(page.getContentData(), new File(
								htmlFileName));
						System.out.println("--- I found a new HTML: "
								+ htmlBaseName + " ---");
						logEntry += htmlBaseName + ".html" + "\n";
						urlEntry += url + "\n"; // recording urls from which
						// html pages are downloaded
					} catch (IOException iox) {
						System.out.println("Error storing HTMLs");
					}

				}// <--- downloading new html is done so as the writing a log
				// for the file.

				/*
				 * If the HTML file is already present in text format
				 * (previously saved by the python code), then download the html
				 * but don't write it down in the log file --->
				 */
				else {

					try {
						Files.write(page.getContentData(), new File(
								htmlFileName));
					} catch (IOException iox) {
						System.out.println("Error storing HTMLs");
					}

				}// <--- dealing with old htmls is done

			}// <--- checking for some terms in the htmls is done

		}// <--- dealing with htmls is done

		/*
		 * Return if there is no PDF ....
		 */
		if (!pdfPatterns.matcher(url).matches()) {
			long endVisit = System.nanoTime();
			long elapsedTime = (endVisit - startVisit) / 1000;
			visitDuration += url + " , " + elapsedTime + "\n";
			
			return;
		}

		/*
		 * ... Otherwise, continue with downloading the pdf
		 */

		/* Preparing folders and names for the pdf file */
		String pdfExtension = url.substring(url.lastIndexOf("."));
		String pdfName = FilenameUtils.getBaseName(url).replaceAll(
				"[^a-zA-Z0-9.-]", "")
				+ pdfExtension; // removing characters not allowed in Windows
		// file
		// system
		String pdfFileName = folder.getAbsolutePath() + "/" + pdfName;
		File output = new File(pdfFileName); // output file

		// if the file is new--->
		if (!output.exists()) {

			// store PDF and add an entry to the log file--->
			try {
				Files.write(page.getContentData(), new File(pdfFileName));
				System.out
				.println("--- I found a new PDF: " + pdfName + " ---");
				logEntry += pdfName + "\n"; // populate logEntry variable with
				// the newly written PDF file name
				urlEntry += url + "\n"; // recording url from which pdf file is
				// downloaded
			} catch (IOException iox) {
				System.out.println("Error storing PDFs");
			}

		}// <--- the file was new
		// <--- Done storing PDF!
		
		long endVisit = System.nanoTime();
		long elapsedTime = (endVisit - startVisit) / 1000;
		visitDuration += url + " , " + elapsedTime + "\n";

	}// end overridden visit() method

	/**
	 * Static Method to write the log file that contains newly written PDFs and
	 * HTMLs
	 */
	public static void writeLogFile() {

		File logFile = new File(folder.getAbsolutePath() + "/" + "log.txt");

		try {
			FileUtils.write(logFile, logEntry, null);
			logEntry = "";
		} catch (IOException e) {
			System.out.println("Error writing log File");
		}

		File urlFile = new File(folder.getAbsolutePath() + "/" + "url.txt");
		
		try {
			FileUtils.write(urlFile, urlEntry, null);
			urlEntry = "";
		} catch (IOException e) {
			System.out.println("Error writing URL File");
		}
		
		File durationFile = new File(folder.getAbsolutePath() + "/" + "duration.txt");
		
		try {
			FileUtils.write(durationFile, visitDuration, null);
			visitDuration = "";
		} catch (IOException e) {
			System.out.println("Error writing Duration File");
		}

	}// end method that writes newly written pdf and htmls to a log file

}// end class