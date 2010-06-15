package eu.delving.metarepo;

/**
 * Constants that define how we interact with Mongo
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class MRConstants {
    static String DATABASE_NAME = "MetaRepo";
    static String COLLECTION_PREFIX = "MetadataCollection";

    // record types
    static String TYPE_ATTR = "type";
    static String TYPE_METADATA_RECORD = "metarec";
    static String TYPE_MAPPING = "mapping";
    static String TYPE_SUMMARY = "summary";

    // formats
    static String FORMAT_ORIGINAL = "orig";
    static String FORMAT_ESE = "ese";
    static String FORMAT_MAPPING = "mapping";
}
