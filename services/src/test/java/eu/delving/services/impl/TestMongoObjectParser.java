package eu.delving.services.impl;

/**
 * Make sure the MongoObjectParser is working correctly
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class TestMongoObjectParser {
//    private static final Logger LOG = Logger.getLogger(TestMongoObjectParser.class);
//    private static final String DETAILS = "/sffDf.xml.details";
//    private static final String XML = "/sffDf.xml";
//    private static final String METADATA_PREFIX = "abm";
//    private static final String METADATA_NAMESPACE = "http://to_be_decided/abm/";
//
//    @Test
//    public void simple() throws XMLStreamException, IOException {
//        XStream xs = new XStream();
//        xs.processAnnotations(DataSetDetails.class);
//        DataSetDetails details = (DataSetDetails)xs.fromXML(getClass().getResourceAsStream(DETAILS));
//        Path recordRoot = new Path(details.getRecordRoot());
//        Path uniqueElement = new Path(details.getUniqueElement());
//        InputStream input = getClass().getResourceAsStream(XML);
//        MongoObjectParser parser = new MongoObjectParser(input, recordRoot, uniqueElement, METADATA_PREFIX, METADATA_NAMESPACE);
//        DBObject object;
//        int count = 30;
//        while ((object = parser.nextRecord()) != null) {
//            assertNotNull("Object", object);
//            assertNotNull("Metadata", object.get(METADATA_PREFIX));
//            LOG.info(object.get(MetaRepo.Record.UNIQUE));
//            if (count-- == 0) {
//                break;
//            }
//        }
//        parser.close();
//        DBObject namespaces = parser.getNamespaces();
//        LOG.info("namespaces:\n"+namespaces);
//        Assert.assertEquals("Namespace count", 4, namespaces.keySet().size());
//    }
}