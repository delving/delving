package eu.delving.metarepo;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Upload a zip file to the controller
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class ZipUploader {

    private static void stream(InputStream in, OutputStream out) throws IOException {
        byte [] buffer = new byte[4096];
        int length;
        while ( (length = in.read(buffer)) > 0) {
            out.write(buffer, 0, length);
        }
    }

    private static void zipToOutputStream(OutputStream outputStream) throws IOException {
        InputStream xmlInput = ZipUploader.class.getResourceAsStream("/92017_Ag_EU_TEL_a0233E.xml");
        InputStream mappingInput = ZipUploader.class.getResourceAsStream("/92017_Ag_EU_TEL_a0233E.xml.mapping");
        ZipOutputStream zos = new ZipOutputStream(outputStream);
        zos.putNextEntry(new ZipEntry("92017_Ag_EU_TEL_a0233E.xml"));
        stream(xmlInput, zos);
        zos.closeEntry();
        zos.putNextEntry(new ZipEntry("92017_Ag_EU_TEL_a0233E.xml.mapping"));
        stream(mappingInput, zos);
        zos.closeEntry();
        zos.close();
    }

    public static void main(String[] args) throws IOException {
        OutputStream zipOutput = new FileOutputStream("92017_Ag_EU_TEL_a0233E.zip");
        zipToOutputStream(zipOutput);
    }
}
