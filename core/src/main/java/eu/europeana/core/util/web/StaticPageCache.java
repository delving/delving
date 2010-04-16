/*
 * Copyright 2007 EDL FOUNDATION
 *
 *  Licensed under the EUPL, Version 1.0 or? as soon they
 *  will be approved by the European Commission - subsequent
 *  versions of the EUPL (the "Licence");
 *  you may not use this work except in compliance with the
 *  Licence.
 *  You may obtain a copy of the Licence at:
 *
 *  http://ec.europa.eu/idabc/eupl
 *
 *  Unless required by applicable law or agreed to in
 *  writing, software distributed under the Licence is
 *  distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied.
 *  See the Licence for the specific language governing
 *  permissions and limitations under the Licence.
 */
package eu.europeana.core.util.web;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;

import eu.europeana.definitions.domain.Language;

/**
 * This class pays attention to a file system directory and delivers pages if they are present.
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class StaticPageCache {
	private static final String DOT = ".";
	private Logger log = Logger.getLogger(getClass());
	private Map<String, Page> pageMapCache = new ConcurrentHashMap<String, Page>();
	private String staticPagePath;

	@Value("#{europeanaProperties['static.page.path']}")
	public void setStaticPagePath(String staticPagePath) {
		this.staticPagePath = staticPagePath;
	}

	private final Pattern fileNamePattern = Pattern.compile("[a-zA-Z0-9_/\\-]+\\.[a-z]+");

	public String getPage(String pageNamePrefix, String pageName, Language language) {
		// composed from servletPath and pathInfo that may be null
		// and may be called w/o language for verbatim pages
		String fileName = 
			(pageNamePrefix == null ? "" : pageNamePrefix)  
			+ (pageName == null ? "" : pageName);

		// check file name for a-z
		if (fileNamePattern.matcher(fileName).matches()) {

			// paranoid test
			if (checkForDot(fileName)) {
				String lingualFileName = fileName;
				if (language != null) {
					lingualFileName = fileName.replace(DOT, "_" + language.getCode() + DOT);
				}
				Page page = pageMap().get(lingualFileName);
				if (page == null) {
					String defautFileName = fileName.replace(DOT, "_" + Language.EN.getCode() + DOT);
					page = pageMap().get(defautFileName);
					if (page == null) {
						return null;
					}
				}
				return page.getContent();
			} 
		}
		return null;
	}

	private boolean checkForDot(String fileName) {
		return fileName.indexOf(DOT) == fileName.lastIndexOf(DOT);
	}

	public void invalidate() {
		pageMapCache.clear();
	}

	private Map<String, Page> pageMap() {
		if (pageMapCache.isEmpty()) {
			File root = new File(staticPagePath);
			if (!root.isDirectory()) {
				throw new RuntimeException(staticPagePath+" is not a directory!");
			}
			addToPageMap(root, root);
		}
		return pageMapCache;
	}

	private void addToPageMap(File root, File directory) {
		for (File file : directory.listFiles()) {
			if (file.isDirectory()) {
				addToPageMap(root, file);
			}
			else {
				String baseFileName = file.getPath().substring(root.getPath().length());
				if (checkForDot(baseFileName)) {
					pageMapCache.put(baseFileName, new Page(file));
				}
			}
		}
	}

	private class Page {
		private File file;
		private String content;
		private boolean fetched;

		private Page(File file) {
			this.file = file;
		}

		private synchronized String getContent() {
			if (!fetched) {
				try {
					BufferedReader reader = new BufferedReader(new FileReader(file));
					StringBuilder out = new StringBuilder();
					String line;
					while ((line = reader.readLine()) != null) {
						out.append(line).append('\n');
					}
					content = out.toString();
				}
				catch (Exception e) {
					log.warn("Unable to read static page "+file);
				}
				finally {
					fetched = true;
				}
			}
			return content;
		}
	}
}
