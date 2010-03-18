package eu.europeana.sip.conversion;

import eu.europeana.sip.reference.TransformException;

/**
 * Transform the contents of a field while normalizing
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

@Deprecated
public interface FieldConversion {
    String transform(String string, String [] parameters) throws TransformException;
}