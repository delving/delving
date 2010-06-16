package eu.delving.metarepo.core;

/**
 * Constants that define how we interact with Mongo
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class Constant {

    // basics
    public static String DATABASE_NAME = "MetaRepo";
    public static String COLLECTION_PREFIX = "MetadataCollection";
    public static String MONGO_ID = "_id";

    // record fields
    public static String TYPE = "type";
    public static String TYPE_METADATA_RECORD = "metarec";
    public static String TYPE_MAPPING = "mapping";
    static String TYPE_SUMMARY = "summary";
    public static String MODIFIED = "mod";
    public static String UNIQUE = "uniq";
    public static String ORIGINAL = "orig";
    public static String MAPPING_TO_ESE = "to-ese";
}
