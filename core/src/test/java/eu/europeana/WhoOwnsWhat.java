package eu.europeana;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Scan java files for author tags
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */
public class WhoOwnsWhat {
    private static final Pattern EMAIL = Pattern.compile("[a-zA-Z][\\w\\.-]*[a-zA-Z0-9]@[a-zA-Z0-9][\\w\\.-]*[a-zA-Z0-9]\\.[a-zA-Z][a-zA-Z\\.]*[a-zA-Z]");
    private static List<File> javaFiles = new ArrayList<File>();
    private static Map<String, List<File>> authorToSources = new TreeMap<String, List<File>>();
    private static Map<File, List<String>> sourceToAuthors = new TreeMap<File, List<String>>();

    public static void main(String[] arguments) throws IOException {
        File root = new File(".");
        if (!checkDirectory(root)) {
            root = new File("..");
            if (!checkDirectory(root)) {
                System.out.println("This class must be started with home directory set to europeana trunk");
                System.exit(1);
            }
        }
        File[] subdirs = getSubdirs(root);
        for (File subdir : subdirs) {
            File src = new File(subdir, "src");
            File main = new File(src, "main");
            File java = new File(main, "java");
            if (java.exists()) {
                collectJavaFiles(java);
            }
        }
        for (File javaFile : javaFiles) {
            collectAuthors(javaFile);
        }
        PrintStream out = System.out;
        printAuthorToSources(out);
        printSourcesToAuthors(out, 1);
        printSourcesToAuthors(out, 0);
    }

    private static void printSourcesToAuthors(PrintStream out, int authorCount) {
        int count = 0;
        for (Map.Entry<File, List<String>> entry : sourceToAuthors.entrySet()) {
            if (entry.getValue().size() == authorCount) {
                count++;
            }
        }
        out.println("Files with " + authorCount + " authors ("+count+"):");
        for (Map.Entry<File, List<String>> entry : sourceToAuthors.entrySet()) {
            if (entry.getValue().size() == authorCount) {
                out.println("     " + entry.getKey().getName());
            }
        }
    }

    private static void printAuthorToSources(PrintStream out) {
        for (Map.Entry<String, List<File>> entry : authorToSources.entrySet()) {
            out.println("By " + entry.getKey() + " ("+entry.getValue().size()+"):");
            for (File file : entry.getValue()) {
                out.println("        " + file.getName());
            }
            out.println();
        }
    }

    private static void collectAuthors(File javaFile) throws IOException {
        sourceToAuthors.put(javaFile, new ArrayList<String>());
        BufferedReader in = new BufferedReader(new FileReader(javaFile));
        String line;
        while ((line = in.readLine()) != null) {
            line = line.trim();
            if (line.startsWith("*") && line.contains("@author")) {
                Matcher matcher = EMAIL.matcher(line);
                if (matcher.find()) {
                    String email = matcher.group();
                    recordAuthorship(email, javaFile);
                }
                else {
                    throw new RuntimeException("No email address found " + javaFile.getName() + ": " + line);
                }
            }
        }
    }

    private static void recordAuthorship(String email, File javaFile) {
        List<File> authorFiles = authorToSources.get(email);
        if (authorFiles == null) {
            authorToSources.put(email, authorFiles = new ArrayList<File>());
        }
        authorFiles.add(javaFile);
        List<String> fileAuthors = sourceToAuthors.get(javaFile);
        fileAuthors.add(email);
    }

    private static void collectJavaFiles(File directory) {
        File[] files = getJavaFiles(directory);
        javaFiles.addAll(Arrays.asList(files));
        File[] subdirs = getSubdirs(directory);
        for (File subdir : subdirs) {
            collectJavaFiles(subdir);
        }
    }

    private static boolean checkDirectory(File here) {
        File[] subdirs = getSubdirs(here);
        return
                checkFor("portal", subdirs)
                        && checkFor("api", subdirs)
                        && checkFor("core", subdirs);
    }

    private static File[] getSubdirs(File root) {
        return root.listFiles(new FileFilter() {
            public boolean accept(File file) {
                return file.isDirectory();
            }
        });
    }

    private static File[] getJavaFiles(File here) {
        return here.listFiles(new FileFilter() {
            public boolean accept(File file) {
                return file.getName().endsWith(".java");
            }
        });
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
