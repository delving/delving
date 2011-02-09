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
                return "http://collectiewijzer3.delving.org/services/oai-pmh";
            }

            @Override
            public String getMetadataPrefix() {
                return "icn";
            }

            @Override
            public String getSpec() {
                return "005_01_M_NL_joods_historisch";
            }

            @Override
            public String getAccessKey() {
                return "WISSINK-8C0D4964F7AC11A26788";
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
