package org.oclc.oai.harvester.app;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * The XStream data objects associated with the configuration of the harvester
 * 
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

@XStreamAlias("harvest-config")
public class HarvestConfig {
    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final File CONFIG_FILE = new File("run/harvest-config.xml");
    private static final File OLD_CONFIG_FILE = new File("run/harvest-config-old.xml");

    @XStreamAsAttribute
    File outputDirectory;

    @XStreamImplicit
    List<Harvest> harvests;

    @XStreamAlias("harvest")
    public static class Harvest {
        @XStreamAsAttribute
        String id;
        @XStreamAsAttribute
        String output;
        String spec;
        String prefix;
        String publisher;
        Date lastHarvest;
        Boolean successful;
        String baseUrl;
        String lastToken;

        public String getLastHarvestString() {
            return FORMAT.format(lastHarvest);
        }

        public String toString() {
            return
                "id='" + id + "'\n"+
        		"\tspec='"+spec+"'\n"+
        		"\tprefix='"+prefix+"'\n"+
        		"\tlastHarvest='"+FORMAT.format(lastHarvest)+"'\n"+
        		"\tsuccessful='"+successful+"'\n"+
        		"\toutput='"+output+"'\n"+
        		"\tbaseUrl='"+baseUrl+"'\n" +
                "\tlastToken='"+lastToken+"'\n";
        }
    }

    public static HarvestConfig load() throws IOException {
        XStream stream = new XStream();
        stream.processAnnotations(HarvestConfig.class);
        File file = CONFIG_FILE.exists()?CONFIG_FILE:OLD_CONFIG_FILE;
        InputStream in = new FileInputStream(file);
        HarvestConfig harvestConfig = (HarvestConfig) stream.fromXML(in);
        in.close();
        return harvestConfig;
    }

    public static void save(HarvestConfig harvestConfig) throws IOException {
        if (OLD_CONFIG_FILE.exists() && CONFIG_FILE.exists()) {
            OLD_CONFIG_FILE.delete();
            CONFIG_FILE.renameTo(OLD_CONFIG_FILE);
        }
        XStream stream = new XStream();
        stream.processAnnotations(HarvestConfig.class);
        OutputStream out = new FileOutputStream(CONFIG_FILE);
        stream.toXML(harvestConfig,out);
        out.close();
    }

    public static void main(String[] args) throws Exception {
        HarvestConfig harvestConfig = load();
        save(harvestConfig);
    }
}
