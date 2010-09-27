package eu.delving.services.controller;

import eu.delving.services.core.MetaRepo;
import eu.delving.services.exceptions.BadArgumentException;
import eu.delving.services.exceptions.CannotDisseminateFormatException;
import eu.europeana.sip.core.FieldEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Iterator;
import java.util.List;

/**
 * This controller is for some very basic HTML-based exploration of the datasets in the repository
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

@Controller
public class DatasetViewController {

    @Autowired
    private MetaRepo metaRepo;

    @RequestMapping("/metaview/index.html")
    public
    @ResponseBody
    String list() throws BadArgumentException {
        StringBuilder out = new StringBuilder("<h1>MetaRepo Collections:</h1><ul>\n");
        for (MetaRepo.DataSet dataSet : metaRepo.getDataSets().values()) {
            out.append(String.format(
                    "<li><a href=\"%s/%s.html\">%s in %s format</a></li>",
                    dataSet.setSpec(), dataSet.metadataFormat().prefix(), dataSet.setSpec(), dataSet.metadataFormat().prefix()
            ));
            out.append(String.format( // todo: the mappings should be scanned for these extra formats
                    "<li><a href=\"%s/icn.html\">%s in icn format</a></li>",
                    dataSet.setSpec(), dataSet.setSpec()
            ));
            out.append(String.format( // todo: the mappings should be scanned for these extra formats
                    "<li><a href=\"%s/ese.html\">%s in ese format</a></li>",
                    dataSet.setSpec(), dataSet.setSpec()
            ));
        }
        out.append("</ul>");
        return out.toString();
    }

    @RequestMapping("/metaview/formats.html")
    public
    @ResponseBody
    String formats() throws BadArgumentException {
        StringBuilder out = new StringBuilder("<h1>MetaRepo Formats:</h1><ul>\n");
        for (MetaRepo.MetadataFormat format : metaRepo.getMetadataFormats()) {
            out.append(String.format("<li>%s : %s - %s</li>", format.prefix(), format.namespace(), format.schema()));
        }
        out.append("</ul>");
        return out.toString();
    }

    @RequestMapping("/metaview/{dataSetSpec}/{prefix}.html")
    public
    @ResponseBody
    String listCollection(
            @PathVariable String dataSetSpec,
            @PathVariable String prefix
    ) throws CannotDisseminateFormatException, BadArgumentException {
        MetaRepo.DataSet dataSet = metaRepo.getDataSets().get(dataSetSpec);
        if (dataSet == null) {
            throw new RuntimeException(String.format("Dataset [%s] not found", dataSetSpec));
        }
        boolean eseStripWorkaround = "ese".equals(prefix);
        StringBuilder out = new StringBuilder(String.format("<h1>MetaRepo Collection %s in %s format</h1><ul>\n", dataSet.setSpec(), prefix));
        for (MetaRepo.Record record : dataSet.records(eseStripWorkaround ? "icn" : prefix, 0, 10, null, null)) {
            String xml = record.xml(eseStripWorkaround ? "icn" : prefix);
            if (eseStripWorkaround) {
                List<FieldEntry> entries = FieldEntry.createList(xml);
                Iterator<FieldEntry> walk = entries.iterator();
                while (walk.hasNext()) {
                    FieldEntry entry = walk.next();
                    if (entry.getTag().startsWith("icn")) {
                        walk.remove();
                    }
                }
                xml = FieldEntry.toString(entries, false);
            }
            xml = xml.replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\n", "<br>");
            out.append("<li>").append(record.identifier()).append("<br>")
                    .append(record.modified().toString()).append("<br>")
                    .append(xml).append("</li>\n");
        }
        out.append("</ul>");
        return out.toString();
    }
}
