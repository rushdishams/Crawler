package com.sustainalytics.ict;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

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
 * @version 3.3 May 06 2015
 * 
 *          This class shows how you can crawl PDFs on the web and store them in
 *          a folder. Also the program crawls and downloads html pages that
 *          contain some specific terms. 
 *          CHANGE:
 *          THE PDFS AND HTMLS WILL BE DOWNLOADED AS BEFORE BUT WHEN A NEW HTML OR
 *          PDF IS ENCOUNTERED, A LOG ENTRY IS CREATED AND RECORDED.
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
	private static File storageFolder;
	private static String crawlDomain = "";
	private static File folder;

	private static String logEntry = "";
	private static int nameCounter = 0;

	private static SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
	// -------------------------------------------------------------------------------------------------------------------
	// Method Section
	// -------------------------------------------------------------------------------------------------------------------
	/**
	 * Method to set the configuration parameters of the crawler
	 * 
	 * @param domain
	 *            is the company url to start with
	 * @param storageFolderName
	 *            is a string shows where to store the downloaded pdfs
	 */
	public static void configure(String domain, String storageFolderName,
			String companyFolderName) {

		PDFCrawler.crawlDomain = domain;
		storageFolder = new File(storageFolderName);

		if (!storageFolder.exists()) {
			storageFolder.mkdirs();
		}

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

		/*
		 * Do not crawl pages that contain the filter RegExes
		 */
		if (filters.matcher(href).matches()) {
			return false;
		}
		/*
		 * Do not crawl pages that are outside of the domain name
		 */
		if (href.startsWith(crawlDomain)) {
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
	 * Visit Method. visit a page and if PDF is found, download and store. Job
	 * done!
	 */
	@Override
	public synchronized void visit(Page page) {

		String url = page.getWebURL().getURL();

		// if the parsed data is HTML, let's check if it contains certain terms
		// of interest--->
		if (page.getParseData() instanceof HtmlParseData) {

			HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
			String text = htmlParseData.getText();

			// Conditions to match a few terms--->
			if (StringUtils.containsIgnoreCase(text, "sust")
					|| StringUtils.containsIgnoreCase(text, "csr")
					|| StringUtils.containsIgnoreCase(text, "report")
					|| StringUtils.containsIgnoreCase(text, "responsibility")
					|| StringUtils.containsIgnoreCase(text, "social")
					|| StringUtils.containsIgnoreCase(text, "society")
					|| StringUtils.containsIgnoreCase(text, "energy")
					|| StringUtils.containsIgnoreCase(text, "community")
					|| StringUtils.containsIgnoreCase(text, "human rights")
					|| StringUtils.containsIgnoreCase(text, "child labor")
					|| StringUtils.containsIgnoreCase(text, "sust")
					|| StringUtils.containsIgnoreCase(text, "sppl")) {
				// System.out.println("-- Regular Expression Matched, Storing HTML --");

				// Storing HTML--->

				String htmlBaseName = FilenameUtils.getBaseName(url).replaceAll(
						"[^a-zA-Z0-9.-]", ""); // getting rid of characters not
				// allowed in Windows file names
				String htmlFileName = folder.getAbsolutePath() + "/" + htmlBaseName
						+ ".html";
				String txtFileName = folder.getAbsolutePath() + "/" + htmlBaseName
						+ ".txt";
				
				File outputHTML = new File(htmlFileName);
				File outputTXT = new File(txtFileName);
				if(!outputHTML.exists() && !outputTXT.exists()){

					try {
						Files.write(page.getContentData(), new File(htmlFileName));
						System.out.println("--- I found a new HTML: " + htmlBaseName + " ---");
						logEntry += htmlBaseName + ".html" + "\n";
					} catch (IOException iox) {
						System.out.println("Error storing HTMLs");
					}

				}
				else{
					
					htmlBaseName = htmlBaseName + "____" + nameCounter;
					nameCounter ++;

					htmlFileName = folder.getAbsolutePath() + "/" + htmlBaseName + ".html";
					txtFileName = folder.getAbsolutePath() + "/" + htmlBaseName + ".txt";

					outputHTML = new File(htmlFileName);
					outputTXT = new File(txtFileName);

					if(!outputHTML.exists() && !outputTXT.exists()){


						try {
							Files.write(page.getContentData(), new File(htmlFileName));
							System.out.println("--- I found a new HTML: " + htmlBaseName + " ---");
							logEntry += htmlBaseName + ".html" + "\n";
						} catch (IOException iox) {
							System.out.println("Error storing HTMLs");
						}
					}

				}//end else


			}// <--- the html is new

			// <---HTML storing done

		}// <---yes, the data was HTML

		/*
		 * Return if there is no PDF
		 */
		if (!pdfPatterns.matcher(url).matches()) {

			return;

		}

		// Naming the PDF file
		String pdfExtension = url.substring(url.lastIndexOf("."));
		String pdfName = FilenameUtils.getBaseName(url).replaceAll(
				"[^a-zA-Z0-9.-]", "")
				+ pdfExtension; // removing characters not allowed in Windows file
		// system
		String pdfFileName = folder.getAbsolutePath() + "/" + pdfName;
		File output = new File(pdfFileName); // output file
		// if the file is new--->
		if (!output.exists()) {
			// store PDF--->
			try {
				Files.write(page.getContentData(), new File(pdfFileName));
				System.out.println("--- I found a new PDF: " + pdfName + " ---");
				logEntry += pdfName + "\n"; // populate logEntry variable with the newly written PDF file name
			} catch (IOException iox) {
				System.out.println("Error storing PDFs");
			}

		}// <--- the file was new

		// <--- Done storing PDF!

	}// end overridden visit() method

	/**
	 * Static Method to write the log file that contains newly written PDFs and HTMLs 
	 */
	public static void writeLogFile(){

		File logFile = new File (folder.getAbsolutePath() + "/" + "log.txt");
		try {
			FileUtils.write(logFile, logEntry, null);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}//end method that writes newly written pdf and htmls to a log file

}// end class