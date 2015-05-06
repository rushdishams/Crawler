package com.sustainalytics.ict;

import java.io.File;
import java.io.IOException;
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
 *          ALSO, FOR HTMLS, THE SCRIPT OF ANDREI WILL CONVERT THEM TO .TXTS. SO WHEN 
 *          WE COMPARE THE CRAWLED HTMLS, WE FIRST CREATE A LOGICAL FILE WITH THE HTML'S
 *          NAME + .TXT EXTENSION TO SEE WHETHER THEY ARE PRESENT IN THE DIRECOTRY OR NOT.
 *          IF NOTHING IS FOUND, THEN THE FILE IS NEW AND WILL BE STORED AS HTML (WHICH WILL LATER BE
 *          CONVERTED INTO .TXT AGAIN WITH ANDREI'S SCRIPT
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
				
				File output = new File(htmlFileName);
				if(output.exists()){
					
					htmlBaseName = htmlBaseName + "-" + nameCounter;
					nameCounter ++;
					
					htmlFileName = folder.getAbsolutePath() + "/" + htmlBaseName + ".html";
					
				}
				/*The htmls are converted into .txt files
				 * So, we create a logical .txt extension to check whether they are present in the directory or not*/
				String textFileName = folder.getAbsolutePath() + "/" + htmlBaseName + ".txt";
				File outputTXT = new File (textFileName); 
				// if the file does not exist, it is a new file. Download and record in the log file--->
				if (!outputTXT.exists()) {

					try {
						Files.write(page.getContentData(), new File(htmlFileName));
						System.out.println("---A new file is found " + htmlFileName
								+ " ---");
						logEntry += htmlFileName + "\n"; // populate logEntry variable with the newly written PDF file name
					} catch (IOException iox) {
						System.out.println("Error storing HTMLs");
					}

				}// <--- the html is new

				// <---HTML storing done

			}// <---selection of webpage to download is done

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
				System.out.println("---A new file is found " + pdfFileName + " ---");
				logEntry += pdfFileName + "\n"; // populate logEntry variable with the newly written PDF file name
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