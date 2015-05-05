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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jaunt.Element;
import com.jaunt.UserAgent;
import com.sustainalytics.ict.PDFCrawler;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

/**
 * @author Yasser Ganjisaffar
 */
public class BasicCrawlController {
	private static final Logger logger = LoggerFactory.getLogger(BasicCrawlController.class);

	public static void main(String[] args) throws Exception {
		
		int numberOfCrawlers = 10;

		

		UserAgent userAgent = new UserAgent();
		userAgent.visit("https://www.unglobalcompact.org/participants/search");
		userAgent.doc.submit();
		boolean clickNext = true;
		String domain = userAgent.doc.getUrl();

		int page = 1;
		while(page < 4){
			System.out.println("Okay, let's go fetch some");
			System.out.println("URL : " + domain);
			
			CrawlConfig config = new CrawlConfig();
			String crawlStorageFolder = "crawler4jStorage";

			config.setCrawlStorageFolder(crawlStorageFolder);

			//config.setIncludeBinaryContentInCrawling(true);
			//My configurations-->
			config.setPolitenessDelay(1);
			config.setMaxDepthOfCrawling(2);
			config.setMaxPagesToFetch(-1);
			config.setIncludeHttpsPages(true);
			config.setMaxDownloadSize(20000000);
			//<--My configuration ends
			
			PageFetcher pageFetcher = new PageFetcher(config);
			RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
			RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
			CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);
			//controller.setCustomData(domain);
			controller.addSeed(domain);

			controller.start(BasicCrawler.class, numberOfCrawlers);

			Element next = userAgent.doc.findFirst("<a class=next_page>");
			if(!next.innerText().contains("Next"))
				break;
			else{
				
				domain = next.getAt("href").trim();
				domain = domain.replaceAll("&amp;", "&");
				System.err.println(domain);

				
			}
			page++;
			//System.out.println("let us loop again");
		}
		BasicCrawler.writeOutput();
	}
}