package eu.europeana.sip.reference;

/**
 * Transform the contents of a field while normalizing
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

@Deprecated
public interface FieldTransform {
    String PIPE = "|";
    String [] parameterNames();
    String transform(String string, String [] parameters) throws TransformException;
}
