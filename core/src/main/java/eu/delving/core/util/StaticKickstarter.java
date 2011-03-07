/*
 * Copyright 2011 DELVING BV
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
package eu.delving.core.util;

import eu.delving.core.storage.StaticRepo;
import eu.delving.core.storage.impl.StaticRepoImpl;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.List;
import java.util.Properties;

/**
 * This class puts everything from a directory into the StaticRepo to kickstart it, of course only if said has
 * not yet been done.
 *
 * @author Gerald de Jong <gerald@delving.eu>
 */

public class StaticKickstarter implements ResourceLoaderAware {
    private static final String FILE_NAME = "kickstarter";
    private Logger log = Logger.getLogger(getClass());
    private StaticRepo staticRepo;
    private boolean kicked;
    private ResourceLoader resourceLoader;

    public void setStaticRepo(StaticRepo staticRepo) {
        this.staticRepo = staticRepo;
        if (kicked) {
            log.error("Kickstarter being loaded twice");
        }
        kicked = true;
        new Thread(new Kicker()).start();
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    private class Kicker implements Runnable {

        @Override
        public void run() {
            try {
                Thread.sleep(2000);
                Resource resource = resourceLoader.getResource(String.format("classpath:%s.txt", FILE_NAME));
                List<String> files = IOUtils.readLines(resource.getInputStream());
                for (String file : files) {
                    if (file.startsWith("/")) {
                        throw new IOException("Paths are assumed to start in the root, do not prefix with '/'");
                    }
                    resource = resourceLoader.getResource(String.format("classpath:%s/%s", FILE_NAME, file));
                    ResourceType resourceType = ResourceType.find(file);
                    String repoPath = resourceType.fileToRepoPath(file);
                    switch (resourceType) {
                        case HTML:
                            StaticRepo.Page page = staticRepo.getPage(repoPath);
                            if (page.getId() == null) {
                                String content = IOUtils.toString(resource.getInputStream(), "UTF-8");
                                page.setContent(content, null);
                            }
                            else {
                                log.info(String.format("Page %s is already in the static repository, so cancelling kickstart", repoPath));
                                return;
                            }
                            break;
                        case MENU:
                            break;
                        case PNG:
                        case JPG:
                        case GIF:
                            byte [] image = IOUtils.toByteArray(resource.getInputStream());
                            staticRepo.putImage(repoPath, image);
                            break;
                        default:
                            throw new RuntimeException("Unknown resource type "+resourceType);
                    }
                    log.info(String.format("Put %s in static repo at %s", file, repoPath));
                }
            }
            catch (Exception e) {
                log.error("Unable to kickstart!", e);
            }
        }
    }

    private enum ResourceType {
        HTML(".html", ".dml"),
        MENU(".menu", ".dml"),
        PNG(".png", ".png.img"),
        JPG(".jpg", ".jpg.img"),
        GIF(".gif", ".gif.img");

        private String fileSuffix;
        private String pageSurfix;

        private ResourceType(String fileSuffix, String pageSurfix) {
            this.fileSuffix = fileSuffix;
            this.pageSurfix = pageSurfix;
        }

        String fileToRepoPath(String filePath) {
            if (!filePath.endsWith(fileSuffix)) {
                throw new RuntimeException("File suffix not recognized: "+filePath);
            }
            return filePath.substring(0, filePath.length() - fileSuffix.length()) + pageSurfix;
        }

        static ResourceType find(String filePath) {
            for (ResourceType type: values()) {
                if (filePath.endsWith(type.fileSuffix)) {
                    return type;
                }
            }
            throw new RuntimeException("File suffix not recognized: "+filePath);
        }
    }
}