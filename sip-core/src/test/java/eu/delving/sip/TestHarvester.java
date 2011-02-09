package eu.delving.sip;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * Give it a run
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class TestHarvester {

    static boolean problem;

    public static void main(String [] args) throws InterruptedException {
        Harvester harvester = new Harvester();
        Harvester.Harvest harvest = new Harvester.Harvest() {

            @Override
            public String getUrl() {
                return "http://collectie.museumrotterdam.nl/oai?verb=ListRecords&metadataPrefix=oai_dc";
            }

            @Override
            public String getMetadataPrefix() {
                return "oai_dc";
            }

            @Override
            public String getSpec() {
                return "hmr";
            }

            @Override
            public OutputStream getOutputStream() {
                try {
                    return new FileOutputStream("/tmp/Harvester.xml");
                }
                catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void success() {
                // todo: implement
            }

            @Override
            public void failure(Exception e) {
                System.err.println(Harvester.exceptionToErrorString(e));
                problem = true;
            }
        };
        harvester.perform(harvest);
        while (!harvester.getActive().isEmpty() && !problem) {
            Thread.sleep(10000);
        }
        System.out.println("finished");
        System.exit(0);
    }
}
