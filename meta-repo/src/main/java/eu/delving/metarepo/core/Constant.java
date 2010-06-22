package eu.delving.metarepo.core;

/**
 * Constants that define how we interact with Mongo
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class Constant {

    // basics
    public static final String DATABASE_NAME = "MetaRepo";
    public static final String RECORD_COLLECTION_PREFIX = "Records_";
    public static final String DATASETS_COLLECTION = "Datasets";
    public static final String HARVEST_STEPS_COLLECTION = "HarvestSteps";

    // mongo's field
    public static final String MONGO_ID = "_id";

    // record fields
    public static final String MODIFIED = "mod";
    public static final String UNIQUE = "uniq";
    public static final String ORIGINAL = "orig";

    // data set fields
    public static final String DATASET_SPEC = "dataset_spec";
    public static final String DATASET_NAME = "dataset_name";
    public static final String DATASET_PROVIDER_NAME = "dataset_provider_name";
    public static final String DATASET_DESCRIPTION = "dataset_desciption";
    public static final String DATASET_NAMESPACES = "dataset_namespaces";
    public static final String DATASET_MAPPINGS = "mappings";
    public static final String DATASET_MAPPING_TO_ESE = "to-ese";
}
