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
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * This class puts everything from a directory into the StaticRepo to kickstart it, of course only if said has
 * not yet been done.
 *
 * @author Gerald de Jong <gerald@delving.eu>
 */

public class StaticKickstarter implements ResourceLoaderAware {
    private static final String TITLE_PREFIX = "title:";
    private static final String MENU_NAME_PREFIX = "menuName:";
    private static final String MENU_PRIORITY_PREFIX = "menuPriority:";
    private Logger log = Logger.getLogger(getClass());
    private StaticRepo staticRepo;
    private boolean kicked;
    private ResourceLoader resourceLoader;

    @Autowired
    private ThemeHandler themeHandler;

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
                for (String themeName : themeHandler.getThemeNames()) {
                    String resourceName = String.format("classpath:kickstarter/%s", themeName);
                    Resource directoryResource = resourceLoader.getResource(resourceName);
                    if (!directoryResource.exists()) {
                        log.info(String.format("Resource '%s' not found", resourceName));
                        continue;
                    }
                    File directory = directoryResource.getFile();
                    if (!directory.isDirectory()) {
                        log.error(String.format("Resource '%s' not a directory", resourceName));
                        continue;
                    }
                    kickstartDirectory(directory, directory);
                }
            }
            catch (Exception e) {
                log.error("Unable to kickstart!", e);
            }
        }

        private void kickstartDirectory(File rootDirectory, File directory) throws IOException {
            log.info(String.format("Kickstarting '%s'", directory.getAbsolutePath()));
            for (File file : directory.listFiles()) {
                if (file.isDirectory()) {
                    kickstartDirectory(rootDirectory, file);
                    continue;
                }
                ResourceType resourceType = ResourceType.find(file);
                String filePath = file.getAbsolutePath().substring(rootDirectory.getAbsolutePath().length());
                String repoPath = resourceType.fileToRepoPath(rootDirectory.getName() + filePath);
                switch (resourceType) {
                    case HTML:
                        StaticRepo.Page page = staticRepo.getPage(repoPath);
                        if (page.getId() == null) {
                            List<String> lines = IOUtils.readLines(new FileInputStream(file), "UTF-8");
                            String title = "";
                            String menuName = null;
                            int menuPriority = 1;
                            Iterator<String> walk = lines.iterator();
                            while (walk.hasNext()) {
                                String line = walk.next().trim();
                                if (!line.isEmpty()) {
                                    if (line.startsWith("<")) {
                                        break;
                                    }
                                    if (line.startsWith(TITLE_PREFIX)) {
                                        title = line.substring(TITLE_PREFIX.length()).trim();
                                    }
                                    else if (line.startsWith(MENU_NAME_PREFIX)) {
                                        menuName = line.substring(MENU_NAME_PREFIX.length()).trim();
                                    }
                                    else if (line.startsWith(MENU_PRIORITY_PREFIX)) {
                                        menuPriority = Integer.parseInt(line.substring(MENU_PRIORITY_PREFIX.length()).trim());
                                    }
                                    else {
                                        log.warn("Did not understand: " + line);
                                    }
                                }
                                walk.remove();
                            }
                            StringBuilder content = new StringBuilder();
                            for (String line : lines) {
                                content.append(line).append('\n');
                            }
                            page.setContent(title, content.toString(), null);
                            if (menuName != null) {
                                page.setMenu(menuName, menuPriority);
                            }
                            log.info(String.format("Page %s created", repoPath));
                        }
                        else {
                            log.info(String.format("Page %s is already in the static repository, so not updating it", repoPath));
                        }
                        break;
                    case PNG:
                    case JPG:
                    case GIF:
                        if (staticRepo.getImage(repoPath) == null) {
                            byte[] image = FileUtils.readFileToByteArray(file);
                            staticRepo.putImage(repoPath, image);
                            log.info(String.format("Image %s created", repoPath));
                        }
                        else {
                            log.info(String.format("Image %s is already in the static repository, so not updating it", repoPath));
                        }
                        break;
                    default:
                        throw new RuntimeException("Unknown resource type " + resourceType);
                }
            }
        }
    }

    private enum ResourceType {
        HTML(".html", ".dml"),
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
                throw new RuntimeException("File suffix not recognized: " + filePath);
            }
            return filePath.substring(0, filePath.length() - fileSuffix.length()) + pageSurfix;
        }

        static ResourceType find(File file) {
            for (ResourceType type : values()) {
                if (file.getName().endsWith(type.fileSuffix)) {
                    return type;
                }
            }
            throw new RuntimeException("File suffix not recognized: " + file.getAbsolutePath());
        }
    }
}