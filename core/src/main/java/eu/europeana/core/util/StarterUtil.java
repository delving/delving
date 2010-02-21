/*
 * Copyright 2007 EDL FOUNDATION
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they
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
package eu.europeana.core.util;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

/**
 * Helper methods for running various projects
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class StarterUtil {
    private static final Logger LOG = Logger.getLogger(StarterUtil.class);

    public static String getEuropeanaPath() {
        return getEuropeanaRoot().getAbsolutePath();
    }

    public static String getAITPath() {
        return getAITRoot().getAbsolutePath();
    }

    private static File getAITRoot() {
        try {
            return getAITRoot(new File(".").getCanonicalFile());
        }
        catch (IOException e) {
            throw new RuntimeException("Couldn't get canonical file", e);
        }
    }

    private static File getEuropeanaRoot() {
        try {
            return getEuropeanaRoot(new File(".").getCanonicalFile());
        }
        catch (IOException e) {
            throw new RuntimeException("Couldn't get canonical file", e);
        }
    }

    private static File getEuropeanaRoot(File here) {
        if (here == null) {
            throw new RuntimeException("Couldn't find europeana root");
        }
        else if (isEuropeanaRoot(here)) {
            LOG.info("Europeana Root: " + here.getAbsolutePath());
            return here;
        }
        else {
            LOG.info("Moving to parent " + here.getParentFile());
            return getEuropeanaRoot(here.getParentFile());
        }
    }

    private static File getAITRoot(File here) {
        if (here == null) {
            throw new RuntimeException("Couldn't find AIT root");
        }
        else if (isAITRoot(here)) {
            LOG.info("AIT Root: " + here.getAbsolutePath());
            return here;
        }
        else if (checkFor(here, "contrib")) {
            LOG.info("Found contrib: " + here.getAbsolutePath());
            return getAITRoot(new File(here, "contrib/ait/trunk"));
        }
        else {
            LOG.info("Moving to parent " + here.getParentFile());
            return getAITRoot(here.getParentFile());
        }
    }

    private static boolean isAITRoot(File here) {
        return checkFor(here, "annotation-middleware", "image-annotation-frontend");
    }

    private static boolean isEuropeanaRoot(File here) {
        return checkFor(here, "core", "api", "portal-lite");
    }

    private static boolean checkFor(File here, String... subDirectories) {
        File[] subdirs = here.listFiles(new FileFilter() {
            public boolean accept(File file) {
                return file.isDirectory();
            }
        });
        for (String subDirectory : subDirectories) {
            if (!checkFor(subDirectory, subdirs)) {
                LOG.info("No subdir " + subDirectory + " in " + here.getAbsolutePath());
                return false;
            }
        }
        return true;
    }

    private static boolean checkFor(String name, File[] subdirs) {
        for (File subdir : subdirs) {
            if (subdir.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }


}
