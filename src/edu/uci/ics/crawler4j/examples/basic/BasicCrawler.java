/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.uci.ics.crawler4j.examples.basic;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.http.Header;

import com.jaunt.Element;
import com.jaunt.Elements;
import com.jaunt.JauntException;
import com.jaunt.ResponseException;
import com.jaunt.UserAgent;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

/**
 * @author Yasser Ganjisaffar
 */
public class BasicCrawler extends WebCrawler {
	static String output = "";

	private static final Pattern IMAGE_EXTENSIONS = Pattern.compile(".*\\.(bmp|gif|jpg|png)$");

	/**
	 * You should implement this function to specify whether the given url
	 * should be crawled or not (based on your crawling logic).
	 */
	@Override
	public boolean shouldVisit(Page referringPage, WebURL url) {

		String href = url.getURL().toLowerCase();
		//System.out.println ("I am here ");System.out.println(url);
		// Ignore the url if it has an extension that matches our defined set of image extensions.
		if (IMAGE_EXTENSIONS.matcher(href).matches()) {
			return false;
		}

		// Only accept the url if it is in the "www.ics.uci.edu" domain and protocol is "http".
		return href.startsWith("https://www.unglobalcompact.org/participant/");
	}

	/**
	 * This function is called when a page is fetched and ready to be processed
	 * by your program.
	 */
	@Override
	public void visit(Page page) {
		int docid = page.getWebURL().getDocid();
		String url = page.getWebURL().getURL();
		String domain = page.getWebURL().getDomain();
		String path = page.getWebURL().getPath();
		String subDomain = page.getWebURL().getSubDomain();
		String parentUrl = page.getWebURL().getParentUrl();
		String anchor = page.getWebURL().getAnchor();

		//System.out.println("Docid: {}" + docid);
		System.out.println("URL: {}" + url);

		UserAgent userAgent = new UserAgent();
		
		try {
			userAgent.visit(url);
			Element title = userAgent.doc.findFirst("<title>");
			output += title.innerText() + ", ";
			Element div = userAgent.doc.findEvery("<div class=copy>");  //find table element, UNPRI, also CDP but noisy
			
			Elements dd = div.findEach("<dd>");  //find non-nested td/th elements

			for(Element entry: dd){                                             //iterate through td/th's
				//System.out.println(entry.innerText());                        //print each td/th element
				output += entry.innerText().trim() + ", ";	  
				
				
			}
			output += "\n";		
		
	} catch(JauntException e){
		System.err.println(e);
	}
		
}
	
	public static void writeOutput(){
		try {
			FileUtils.write(new File("myfile.txt"), output, null);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}