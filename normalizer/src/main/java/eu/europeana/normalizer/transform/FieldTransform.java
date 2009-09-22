package eu.europeana.normalizer.transform;

/**
 * Transform the contents of a field while normalizing
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public interface FieldTransform {
    String PIPE = "|";
    String [] parameterNames();
    String transform(String string, String [] parameters) throws TransformException;
}
