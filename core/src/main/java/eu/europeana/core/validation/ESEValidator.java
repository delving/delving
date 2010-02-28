package eu.europeana.core.validation;

import eu.europeana.core.querymodel.query.BriefBeanView;
import eu.europeana.core.querymodel.query.FullBeanView;

import java.io.File;
import java.util.List;

/**
 * This is the validation interface for ESE records
 *
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Feb 21, 2010 11:29:32 AM
 */
public interface ESEValidator {

    boolean isCorrectlyIngested(List<ESEImportErrors> errorList);

    boolean isValidToXmlSchema(List<ESEImportErrors> errorList);

    List<ESEImportErrors> validateToXmlSchema(File eseImportFile);

    List<ESEImportErrors> validateESE(File eseImportFile);

    List<ESEImportErrors> importESEDataSet(File eseImportFile);

    

    BriefBeanView queryBriefResult(EuropeanaQuery europeanaQuery);

    FullBeanView queryFullResult(String europeanaUri);

    String renderAsHtml(BriefBeanView briefBeanView);

    String renderAsXml(BriefBeanView briefBeanView);

    String renderAsHtml(FullBeanView fullBeanView);

    String renderAsXml(FullBeanView fullBeanView);

}
