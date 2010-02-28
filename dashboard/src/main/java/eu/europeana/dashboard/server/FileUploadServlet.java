package eu.europeana.dashboard.server;

import eu.europeana.core.database.domain.ImportFileState;
import eu.europeana.core.database.incoming.ImportFile;
import eu.europeana.core.database.incoming.ImportRepository;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public abstract class FileUploadServlet extends HttpServlet implements Servlet {
    private Logger log = Logger.getLogger(getClass());

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/plain");
        FileItem uploadItem = getFileItem(request);
        if (uploadItem == null) {
            response.getWriter().write("NO-SCRIPT-DATA");
            log.info("Post contained no file");
            return;
        }
        String fileName = extractFileName(uploadItem.getName());
        ImportFile importFile = getRepository().createForUpload(fileName);
        log.info("created file "+importFile+", filling..");

        InputStream in = uploadItem.getInputStream();
        FileOutputStream out = new FileOutputStream(getRepository().createFile(importFile));
        byte [] buffer = new byte[8192];
        int bytesRead;
        while ((bytesRead = in.read(buffer)) > 0) {
            out.write(buffer, 0, bytesRead);
        }
        in.close();
        out.close();
        log.info("filled "+importFile);

        importFile = getRepository().transition(importFile, ImportFileState.UPLOADED);
        log.info("renamed to "+importFile);

        response.getWriter().write("okay");
    }

    private String extractFileName(String path) {
        int slash = path.lastIndexOf('/');
        int backslash = path.lastIndexOf('\\');
        if (slash >= 0) {
            return path.substring(slash+1);
        }
        else if (backslash >= 0) {
            return path.substring(backslash+1);
        }
        else {
            return path;
        }
    }

    private FileItem getFileItem(HttpServletRequest request) {
        FileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);

        try {
            List items = upload.parseRequest(request);
            for (Object item : items) {
                FileItem fileItem = (FileItem) item;
                if (!fileItem.isFormField() && "uploadFile".equals(fileItem.getFieldName())) {
                    return fileItem;
                }
            }
        }
        catch (FileUploadException e) {
            return null;
        }
        return null;
    }

    protected abstract ImportRepository getRepository();
}