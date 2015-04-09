package com.sustainalytics.ict;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

import com.google.common.io.Files;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.url.WebURL;

/**
 * @author Yasser Ganjisaffar [lastname at gmail dot com]
 */

/*
 * This class shows how you can crawl images on the web and store them in a
 * folder. This is just for demonstration purposes and doesn't scale for large
 * number of images. For crawling millions of images you would need to store
 * downloaded images in a hierarchy of folders
 */
public class PDFCrawler extends WebCrawler {
	private static final Pattern filters = Pattern.compile(".*\\.(bmp|gif|jpe?g|png|tiff?|ico|xaml|pict|rif|pptx?|ps" +
			"|mid|mp2|mp3|mp4|wav|wma|au|aiff|flac|ogg|3gp|aac|amr|au|vox" +
			"|avi|mov|mpe?g|ra?m|m4v|smil|wm?v|swf|aaf|asf|flv|mkv" +
			"|zip|rar|gz|7z|aac|ace|alz|apk|arc|arj|dmg|jar|lzip|lha|css|js|bmp|gif|jpe?g|png|tiff?)" +
			"(\\?.*)?$"); // For url Query parts ( URL?q=... )

	private static final Pattern pdfPatterns = Pattern.compile(".*(\\.(pdf?))$");

	private static File storageFolder;
	private static String crawlDomain = "";
	private static File folder;

	public static void configure(String domain, String storageFolderName) {
		PDFCrawler.crawlDomain = domain;

		storageFolder = new File(storageFolderName);
		if (!storageFolder.exists()) {
			storageFolder.mkdirs();
		}
		
		folder = new File(storageFolder.getAbsolutePath() + "/" + crawlDomain.substring(7));
		if (!folder.exists()) {
			folder.mkdirs();
		}
	}
	@Override
	public boolean shouldVisit(Page page, WebURL url) {
		//System.out.println("I am in shouldvisit");
		//String href = url.getURL().toLowerCase(); 
		//return true;
		/*
		if (!href.startsWith(crawlDomain)) {
			return false;
		}
		
		if (filters.matcher(href).matches()) {
			return false;
		}

		if (pdfPatterns.matcher(href).matches()) {
			return true;
		}
		
		return false;*/
		
		//return href.startsWith(crawlDomain) && pdfPatterns.matcher(href).matches();
		//return (href.startsWith(crawlDomain) &&  pdfPatterns.matcher(href).matches());
		//return href.startsWith(this.getMyController().getCustomData().toString()) && pdfPatterns.matcher(href).matches();
		
		String href = url.getURL().toLowerCase();
	    if (filters.matcher(href).matches()) {
	      return false;
	    }

	    if (pdfPatterns.matcher(href).matches()) {
	      return true;
	    }

	    if (href.startsWith(crawlDomain)) {
	        return true;
	      }
	    
	    return false;
	}

	@Override
	public void visit(Page page) {
		String url = page.getWebURL().getURL();
	
		if (!pdfPatterns.matcher(url).matches()) {
			System.out.println("I am in " + url);
			System.out.println("No match. Leaving.");
			return;
		}
		System.err.println("I am in " + url + " and found something of interest.");
		// get a unique name for storing this image
		String extension = url.substring(url.lastIndexOf("."));
		String hashedName = UUID.randomUUID().toString() + extension;
		//String path = page.getWebURL().getPath() + extension;

		// store image
		String filename = folder.getAbsolutePath()+ "/" + hashedName;
		//String filename = storageFolder.getAbsolutePath() + "/" + path;
		try {
			Files.write(page.getContentData(), new File(filename));
		} catch (IOException iox) {
			logger.error("Failed to write file: " + filename, iox);
		}
	}
}//end class

