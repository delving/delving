package eu.europeana.core.database.incoming;

import eu.europeana.core.database.domain.ImportFileState;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Handle the import file system, exposing the existing files there
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public class ImportRepositoryImpl implements ImportRepository {
    private Logger log = Logger.getLogger(getClass());
    private File root;
    private List<Folder> folders = new ArrayList<Folder>();

    public void setDataDirectory(String dataDirectory) {
        root = new File(dataDirectory);
        for (ImportFileState state : ImportFileState.values()) {
            folders.add(new Folder(root, state));
        }
    }

    public File createFile(ImportFile importFile) {
        return new File(new File(root, importFile.getState().toString().toLowerCase()), importFile.getFileName());
    }

    public ImportFile createForUpload(String fileName) {
        log.info("creating "+fileName);
        Folder folder = get(fileName);
        if (folder != null) {
            log.info("File "+fileName+" already existed in folder "+folder.state+", deleting it");
            folder.delete(fileName);
        }
        return get(ImportFileState.UPLOADING).createImportFile(fileName, false);
    }

    public List<String> getFiles(ImportFileState state) {
        return get(state).getFileNames();
    }

    public List<ImportFile> getAllFiles() {
        List<ImportFile> all = new ArrayList<ImportFile>();
        for (Folder folder: folders) {
            for (String fileName: folder.getFileNames()) {
                all.add(new ImportFile(fileName, folder.state.toString()));
            }
        }
        return all;
    }

    public ImportFile transition(ImportFile importFile, ImportFileState to) {
        return get(importFile.getState()).move(importFile.getFileName(), get(to));
    }

    public ImportFile checkStatus(String fileName) {
        for (Folder folder : folders) {
            if (folder.containsFile(fileName)) {
                return folder.createImportFile(fileName, true);
            }
        }
        return null;
    }

    public ImportFile copyToUploaded(File file) throws IOException {
        String fileName = file.getName();
        Folder folder = get(fileName);
        if (folder != null) {
            log.info("File "+fileName+" already existed in folder "+folder.state+", deleting it");
            folder.delete(fileName);
        }
        Folder uploadedFolder = get(ImportFileState.UPLOADED);
        File uploadedFile = uploadedFolder.createFile(fileName);
        log.info("going to copy "+file.getAbsolutePath()+" to "+uploadedFile.getAbsolutePath());
        FileInputStream in = new FileInputStream(file);
        FileOutputStream out = new FileOutputStream(uploadedFile);
        byte [] buffer = new byte[2048];
        int chunk;
        while ((chunk = in.read(buffer)) > 0) {
            out.write(buffer, 0, chunk);
        }
        in.close();
        out.close();
        log.info("done copying "+file.getAbsolutePath()+" to "+uploadedFile.getAbsolutePath());
        return new ImportFile(fileName, ImportFileState.UPLOADED);
    }

    private Folder get(ImportFileState state) {
        return folders.get(state.ordinal());
    }

    private Folder get(String fileName) {
        for (Folder contents : folders) {
            if (contents.containsFile(fileName)) {
                return contents;
            }
        }
        return null;
    }

    private class Folder {
        private ImportFileState state;
        private File directory;

        public Folder(File root, ImportFileState state) {
            this.directory = new File(root, state.toString().toLowerCase());
            this.state = state;
            if (!directory.exists()) {
                if (!directory.mkdirs()) {
                    log.warn("Directory doesn't exist, but cannot mkdirs for it: "+directory);
                }
            }
        }

        synchronized List<String> getFileNames() {
            List<String> fileNameList = new ArrayList<String>();
            File[] importCandidates = directory.listFiles(new FileFilter() {
                public boolean accept(File file) {
                    return file.isFile() && ImportFile.isCorrectSuffix(file.getName());
                }
            });
            for (File candidate : importCandidates) {
                fileNameList.add(candidate.getName());
            }
            return fileNameList;
        }

        boolean containsFile(String fileName) {
            return getFileNames().contains(fileName);
        }

        File createFile(String fileName) {
            return new File(directory, fileName);
        }

        ImportFile createImportFile(String fileName, boolean expectExisting) {
            boolean exists = getFileNames().contains(fileName);
            if (expectExisting != exists) {
                throw new RuntimeException("File name "+(exists?"already exists":"does not exist")+": "+fileName);
            }
            if (!exists) {
                getFileNames().add(fileName);
            }
            File file = createFile(fileName);
            ImportFile importFile = new ImportFile(fileName, state.toString());
            importFile.setLastModified(new Date(file.lastModified()));
            return importFile;
        }

        ImportFile move(String fileName, Folder target) {
            if (target.getFileNames().contains(fileName)) {
                log.info("File "+fileName+" already existed in folder "+state+", deleting it");
                target.delete(fileName);
            }
            if (!getFileNames().remove(fileName)) {
                throw new RuntimeException("File name "+fileName+" not found in folder "+state);
            }
            File from = new File(directory, fileName);
            File to = new File(target.directory, fileName);
            boolean moved = from.renameTo(to);
            if (moved) {
                target.getFileNames().add(fileName);
                log.info("Moved "+from+" to "+to);
            }
            else {
                log.error("Unable to move "+from+" to "+to);
            }
            ImportFile importFile = new ImportFile(fileName, target.state.toString());
            importFile.setLastModified(new Date(to.lastModified()));
            return importFile;
        }

        public boolean delete(String fileName) {
            if (!getFileNames().remove(fileName)) {
                throw new RuntimeException("File name not found here "+fileName);
            }
            File toDelete = new File(directory, fileName);
            boolean deleted = toDelete.delete();
            if (deleted) {
                log.info("Deleted "+toDelete);
            }
            else {
                log.error("Unable to delete "+toDelete);
            }
            return deleted;
        }

        public String toString() {
            return directory.getAbsolutePath();
        }
    }
}