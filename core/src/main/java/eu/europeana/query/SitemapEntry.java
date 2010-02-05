/*
 * Copyright 2007 EDL FOUNDATION
 *
 * Licensed under the EUPL, Version 1.0 or - as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * you may not use this work except in compliance with the
 * Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */
package eu.europeana.query;


/**
 * Sitemap entry, model for MVC.
 * @author Borys Omelayenko
 *
 */

public class SitemapEntry extends SitemapIndexEntry {
	
	private String changefreq;
	private String priority;
	
	public String getChangefreq() {
		return changefreq;
	}
	public String getPriority() {
		return priority;
	}
	
	public SitemapEntry(String loc, String lastmod, String changefreq, String priority) {
		super(loc, lastmod);
		this.changefreq = changefreq;
		this.priority = priority;
	}
}

